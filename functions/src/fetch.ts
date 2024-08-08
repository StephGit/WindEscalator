import * as functions from "firebase-functions";
import fetch from "node-fetch";

export const myFunction = functions.https.onRequest(async (req, res) => {
  try {
    const response = await fetch("https://www.google.com");
    const html = await response.text();
    res.send(html);
  } catch (error) {
    console.error("Fehler beim Abrufen der Website:", error);
    res.status(500).send("Fehler beim Abrufen der Website.");
  }
});
