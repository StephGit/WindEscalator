import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const TIME_ZONE = "Europe/Zurich";
const firestore = admin.firestore();

// runs 05:00 to 22:00 all 10mins
export const cronDataFetch = functions.pubsub
  .schedule("0,10,20,30,40,50 5-21 * * *").timeZone(TIME_ZONE).onRun(async (context) => {
    try {
      const collectionRef = firestore.collection("alert");

      // get alerts which are, enabled, startime in future, nextRun still today
      const snapshot = await collectionRef
        .where("active", "==", true)
        .where("startTime", "<=", getCurrentTime())
        .where("nextRun", "<", getMaxTimestampToday() )
        .orderBy("resource", "asc")
        .get();

      // Daten verarbeiten
      snapshot.forEach((doc) => {
        const data = doc.data();
        console.log("Dokument ID:", doc.id);
        console.log("Daten:", data);
      // Abfrage der Winddaten und notify User, Update Alert mit nextRun
        fetchResource(data.resource)
      });

      console.log("Daten erfolgreich abgerufen!");
      return null; // Funktion erfolgreich abgeschlossen
    } catch (error) {
      console.error("Fehler beim Abrufen von Daten:", error);
      return null; // Fehlerbehandlung
    }
  });

const getCurrentTime = () => {
  const now = new Date();

  const hours = (now.getHours() + 2).toString().padStart(2, "0"); // cheap timezone fix
  const minutes = now.getMinutes().toString().padStart(2, "0");
  console.log(`currenttime:${hours}:${minutes}`)
  return `${hours}:${minutes}`;
};

// return max timestamp
function getMaxTimestampToday() {
  const today = new Date();
  today.setHours(23, 59, 59, 999); // Set to 11:59:59 PM with milliseconds
  return today.getTime();
}

async function fetchResource(resource: String) {
  const fetch = await import("node-fetch"); // Dynamic import
  const snapshot = await firestore.collection("windResource").where("localId", "==", resource).get();
  snapshot.forEach((doc) => {
    const data = doc.data();
    console.log(data.url);
    const windData = getData(data.url, fetch); // Pass fetch to getData
    console.log(windData);
  });
}

async function getData(url: string, fetch: any) { // Accept fetch as a parameter
  try {
    const response = await fetch.default(url); // Use fetch.default
    return await response.text();
  } catch (error) {
    console.error("Fehler beim Abrufen der Website:", error);
    return null;
  }
}