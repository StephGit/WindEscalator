import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

const TIME_ZONE: string = 'Europe/Zurich'

// export const dailyDataFetch = functions.pubsub.schedule('0 04 * * *').onRun(async (context) => {
export const dailyDataFetch = functions.pubsub.schedule('20 14 * * *', {timeZone: TIME_ZONE}).onRun(async (context) => {
  try {
    // Firestore-Referenz erstellen
    const firestore = admin.firestore();
    const collectionRef = firestore.collection('alert');

    // get alerts which are, enabled, startime in future, nextRun still today
    const snapshot = await collectionRef
        .where('enabled', '==', true)
        .where('startTime', '>=', getCurrentTime())
        .where('nextRun', '<', getMaxTimestampToday() )
        .orderBy('resource', 'asc')
        .get(); //where enabled + startTime > now + nextRun == today, sort by WindResource

    // Daten verarbeiten
    snapshot.forEach(doc => {
      const data = doc.data();
      console.log('Dokument ID:', doc.id);
      console.log('Daten:', data);
      // Abfrage der Winddaten und notify User, Update Alert mit nextRun
      //
    });

    console.log('Daten erfolgreich abgerufen!');
    return null; // Funktion erfolgreich abgeschlossen
  } catch (error) {
    console.error('Fehler beim Abrufen von Daten:', error);
    return null; // Fehlerbehandlung
  }
});

export const getCurrentTime = () => {
  let now = new Date();
  now = setTimeZone(now);
  const hours = now.getHours().toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

function getMaxTimestampToday() {
  const today = new Date();
  today.setHours(23, 59, 59, 999); // Set to 11:59:59 PM with milliseconds
  return setTimeZone(today).getTime()
}

function setTimeZone(date: Date) {
  const options = { timeZone: TIME_ZONE }
  const formatter = new Intl.DateTimeFormat('de-DE', options);
  return formatter.format(date);
}