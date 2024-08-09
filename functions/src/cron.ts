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

      // get alerts which are, enabled, startime in future, nextRun still today
      const snapshot = await collectionRef
        .where('active', '==', true)
        .where('startTime', '<=', getCurrentTime())
        .where('nextRun', '<', getMaxTimestampToday())
        .orderBy('resource', 'asc')
        .get();

      // filter resources to fetch
      const resources: number[] = [
        ...new Set(snapshot.docs.map((doc) => doc.data().resource)),
      ];
      const windDataResults = await getWindData(resources);

      // Daten verarbeiten
      for (const doc of snapshot.docs) {
        const data = doc.data();
        console.log('Dokument ID:', doc.id);
        console.log('Daten:', data);
        console.log(windDataResults.get(data.resource));
        // Abfrage der Winddaten und notify User, Update Alert mit nextRun
      }

      console.log('Daten erfolgreich abgerufen!');
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

  for (const res of resources) {
    try {
      const result = await fetchResource(res);
      let windData: WindData;

      if (res === 1) {
        windData = extractScniData(result);
      } else if (res === 2) {
        windData = extractNeucData(result);
      } else {
        windData = extractWsctData(result);
      }

      windDataResults.set(res, windData);
    } catch (error) {
      console.error(
        `Error fetching or extracting data for resource ${res}:`,
        error
      );
    }
  }

  return windDataResults;
}

const getCurrentTime = () => {
  const now = new Date();

  const hours = (now.getHours() + 2).toString().padStart(2, '0'); // cheap timezone fix
  const minutes = now.getMinutes().toString().padStart(2, '0');
  console.log(`currenttime:${hours}:${minutes}`);
  return `${hours}:${minutes}`;
};

// return max timestamp
function getMaxTimestampToday() {
  const today = new Date();
  today.setHours(23, 59, 59, 999); // Set to 11:59:59 PM with milliseconds
  return today.getTime();
}

async function fetchResource(resource: number): Promise<string | null> {
  const snapshot = await firestore
    .collection('windResource')
    .where('localId', '==', resource)
    .get();
  if (snapshot.empty) {
    return null; // Resource not found
  }
  const doc = snapshot.docs[0]; // Get the first document
  const data = doc.data();
  return getData(data.url);
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
