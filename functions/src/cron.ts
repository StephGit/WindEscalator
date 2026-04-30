import {onSchedule} from 'firebase-functions/v2/scheduler';
import {initializeApp} from 'firebase-admin/app';
import {DocumentData, getFirestore} from 'firebase-admin/firestore';
import {getMessaging} from 'firebase-admin/messaging';
import {DateTime} from 'luxon';

import {
  extractNeucData,
  extractScniData,
  extractWsctData,
  extractBrieData,
  WindData,
} from './winddata';

initializeApp();

const TIME_ZONE = 'Europe/Zurich';
const firestore = getFirestore();

const options = {
  schedule: '0,10,20,30,40,50 5-21 * * *',
  timeZone: TIME_ZONE,
  region: 'europe-west6',
};

// runs 05:00 to 22:00 all 10mins
export const cronDataFetch = onSchedule(options, async (event) => {
  try {
    const collectionRef = firestore.collection('alert');
    const time = getCurrentTime();
    const maxTimestamp = getMaxTimestampToday();

    console.info(`Current query time: ${time}`);
    // get alerts which are, enabled, startime in future
    // and nextRun still today, endtime not yet
    const snapshot = await collectionRef
      .where('active', '==', true)
      .where('startTime', '<=', time)
      .where('nextRun', '<', maxTimestamp)
      .where('endTime', '>=', time)
      .orderBy('resource', 'asc')
      .get();
    // Always fetch all wind resources to keep data up-to-date
    const windDataResults = await getWindData();

    // no alerts active so no continuation...
    if (snapshot.docs.length === 0) {
      return null;
    }

    // Daten verarbeiten
    for (const doc of snapshot.docs) {
      const data = doc.data();
      const windData = windDataResults.get(data.resource);

      // only send message on exceeding threshold
      if (windData && windData.force >= data.windForceKts) {
        const uid = data.userId;
        const messageData = {
          alertId: doc.id,
          windData: JSON.stringify(windData),
        };

        sendFCMMessage(uid, messageData)
          .then((result) => {
            console.info('Message sent:', result);
          })
          .catch((error) => {
            console.error('Error sending message:', error);
          });
      }
    }

    console.info('Daten erfolgreich abgerufen!');
    return null; // Funktion erfolgreich abgeschlossen
  } catch (error) {
    console.error('Fehler beim Abrufen von Daten:', error);
    return null; // Fehlerbehandlung
  }
});

async function getWindData(): Promise<Map<number, WindData>> {
  const windDataResults = new Map<number, WindData>();
  // read all resources
  const results = await fetchAllResources();

  for (const {docId, data} of results) {
    let dataAvailable = false;
    try {
      const result = await getData(data.url);

      let windData: WindData;

      if (data.localId === 1) {
        windData = extractScniData(result);
      } else if (data.localId === 2) {
        windData = extractNeucData(result);
      } else if (data.localId === 3 || data.localId === 4) {
        windData = extractWsctData(result);
      } else if (data.localId === 5) {
        windData = extractBrieData(result);
      }

      dataAvailable =
        !isNaN(windData.force) &&
        windData.force >= 0 &&
        windData.direction !== '';
      console.info(
        `Resource ${data.displayName}: force=${windData.force}, gust=${windData.gust}, dir=${windData.direction}, time=${windData.time}, available=${dataAvailable}`
      );
      windDataResults.set(data.localId, windData);
    } catch (error) {
      console.error(
        `Error fetching or extracting data for resource ${data.displayName}:`,
        error
      );
    } finally {
      // Update windResource document with data availability status and latest reading
      const updateData: Record<string, unknown> = {
        online: dataAvailable,
        lastChecked: Date.now(),
      };
      if (dataAvailable && windDataResults.has(data.localId)) {
        const wd = windDataResults.get(data.localId)!;
        updateData.latestForce = wd.force;
        updateData.latestGust = wd.gust;
        updateData.latestDirection = wd.direction;
        updateData.latestTime = wd.time;
      }
      await firestore.collection('windResource').doc(docId).update(updateData);
    }
  }
  return windDataResults;
}

const sendFCMMessage = async (
  uid: string,
  message: {alertId: string; windData: string}
) => {
  try {
    // Get the FCM token from Firestore
    const docRef = firestore.collection('users').doc(uid);
    const doc = await docRef.get();

    if (doc.exists) {
      // Assuming the token is stored under 'fcmToken' field
      const userToken = doc.data()?.fcmToken;

      if (userToken) {
        // Construct the message payload
        const messagePayload = {
          data: message,
          token: userToken,
        };

        // Send the message using the Admin SDK
        const response = await getMessaging().send(messagePayload);
        console.info('Successfully sent message:', response);
        return {success: true};
      } else {
        console.error('FCM token not found for user:', uid);
        return {success: false, error: 'FCM token not found'};
      }
    } else {
      console.error('User not found:', uid);
      return {success: false, error: 'User not found'};
    }
  } catch (error) {
    console.error('Error sending message:', error);
    return {success: false, error: 'Error sending message'};
  }
};

const getCurrentTime = () => {
  const now = DateTime.now().setZone('Europe/Zurich');
  return now.toFormat('HH:mm');
};

// return max timestamp
function getMaxTimestampToday() {
  const today = new Date();
  today.setHours(23, 59, 59, 999); // Set to 11:59:59 PM with milliseconds
  return today.getTime();
}

async function fetchAllResources(): Promise<
  {docId: string; data: DocumentData}[]
> {
  const windResourceCollection = firestore.collection('windResource');
  const snapshot = await windResourceCollection.get();

  if (snapshot.empty) {
    return []; // No resources found
  }

  return snapshot.docs.map((doc) => ({docId: doc.id, data: doc.data()}));
}

async function getData(url: string): Promise<string> {
  const response = await fetch(url, {
    headers: {
      Accept: 'text/xml, application/xml, text/html, application/json, */*',
      'User-Agent': 'WindEscalator/1.0',
    },
  });
  if (!response.ok) {
    throw new Error(
      `HTTP ${response.status} ${response.statusText} for ${url}`
    );
  }
  return await response.text();
}
