import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
const fetch = require('node-fetch');
import {
  extractNeucData,
  extractScniData,
  extractWsctData,
  WindData,
} from './winddata';

admin.initializeApp();

const TIME_ZONE = 'Europe/Zurich';
const firestore = admin.firestore();

// runs 05:00 to 22:00 all 10mins
export const cronDataFetch = functions.pubsub
  .schedule('0,10,20,30,40,50 5-21 * * *')
  .timeZone(TIME_ZONE)
  .onRun(async (context) => {
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
      // no alerts active so no continuation...
      if (snapshot.docs.length === 0) {
        return null;
      }

      // filter resources to fetch
      const resources: number[] = [
        ...new Set(snapshot.docs.map((doc) => doc.data().resource)),
      ];
      const windDataResults = await getWindData(resources);

      // Daten verarbeiten
      for (const doc of snapshot.docs) {
        const data = doc.data();
        // Abfrage der Winddaten und notify User, Update Alert mit nextRun
        // TODO compare result via alert threshold
        // filter duplicated alert origins - don't send messages twice
        const uid = data.userId;
        const messageData = {
          alertId: doc.id,
          windData: JSON.stringify(windDataResults.get(data.resource)),
        };
        sendFCMMessage(uid, messageData)
          .then((result) => {
            console.info('Message sent:', result);
          })
          .catch((error) => {
            console.error('Error sending message:', error);
          });
      }

      console.info('Daten erfolgreich abgerufen!');
      return null; // Funktion erfolgreich abgeschlossen
    } catch (error) {
      console.error('Fehler beim Abrufen von Daten:', error);
      return null; // Fehlerbehandlung
    }
  });

async function getWindData(
  resources: number[]
): Promise<Map<number, WindData>> {
  const windDataResults = new Map<number, WindData>();
  // read resources as batch
  const results = await fetchResources(resources);

  for (const data of results) {
    try {
      const result = await getData(data.url);
      let windData: WindData;

      if (data.localId === 1) {
        windData = extractScniData(result);
      } else if (data.localId === 2) {
        windData = extractNeucData(result);
      } else {
        windData = extractWsctData(result);
      }

      windDataResults.set(data.localId, windData);
    } catch (error) {
      console.error(
        `Error fetching or extracting data for resource ${data.displayName}:`,
        error
      );
    }
  }

  return windDataResults;
}

export const sendFCMMessage = async (
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
        const response = await admin.messaging().send(messagePayload);
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
  const now = new Date();
  // cheap timezone fix
  const hours = (now.getHours() + 2).toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');

  return `${hours}:${minutes}`;
};

// return max timestamp
function getMaxTimestampToday() {
  const today = new Date();
  today.setHours(23, 59, 59, 999); // Set to 11:59:59 PM with milliseconds
  return today.getTime();
}

async function fetchResources(
  resources: number[]
): Promise<admin.firestore.DocumentData[]> {
  const windResourceCollection = firestore.collection('windResource');
  const snapshot = await windResourceCollection
    .where('localId', 'in', resources)
    .get();

  if (snapshot.empty) {
    return []; // No resources found
  }

  return snapshot.docs.map((doc) => doc.data());
}

async function getData(url: string): Promise<string> {
  try {
    const response = await fetch(url);
    const data = await response.text();
    return data;
  } catch (error) {
    console.error('Fehler beim Abrufen der Website:', error);
    return '';
  }
}
