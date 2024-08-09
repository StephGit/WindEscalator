/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import admin from "firebase-admin";
import  {cronDataFetch} from "./cron";
// import  {testCurl} from "./fetch";

export default cronDataFetch;
// export default testCurl;

// admin.initializeApp();

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

export const helloWorld = onRequest(async (request: any, response: any) => {
  logger.info("Hello logs!", {structuredData: true});
  const message = {
    notification: {
      title: 'My FCM Message',
      body: 'This is a test message.',
    },
    token: 'YOUR_DEVICE_REGISTRATION_TOKEN',
  };
//     await testCurl()
    admin.messaging().sendToDevice("val", message)
    response.send("Hello from Firebase!");
});
