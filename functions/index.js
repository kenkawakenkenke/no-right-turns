const functions = require("firebase-functions");
const segments = require("../data/minimizedSegments.json");

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.helloWorld = functions.https.onRequest((request, response) => {
    const fromSegment = request.query.from;
    const toSegment = request.query.to;

    console.log(fromSegment, "->", toSegment);

    response.send("Hello from Firebase! " + JSON.stringify(segments[fromSegment]));
});
