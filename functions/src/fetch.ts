const fetch = require('node-fetch');

import {extractScniData} from "./winddata";

export const testCurl = async () => {
  try {
    const response = await fetch("https://pmcjoder.ch/webcam/neuhaus/wetterstation/daily.txt");
    const data = await response.text();
    console.log(extractScniData(data))
  } catch (error) {
    console.error("Fehler beim Abrufen der Website:", error);

  }
};
