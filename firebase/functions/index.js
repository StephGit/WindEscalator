const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

var request = require('request');

exports.scheduledFunctionCrontab = functions.pubsub.schedule('*/5 * * * *')
  .timeZone('Switzerland/Geneva') 
  .onRun((context) => {
    request('https://somewindurl.ch', function (error, response, body) {
        if (!error && response.statusCode == 200) {
            // extract and save to db
        }
    })
  return null;
});
