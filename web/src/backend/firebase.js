import firebase from "firebase/app";
import "firebase/analytics";

if (typeof window !== 'undefined') {
    console.log("start clientside firebase!!");
    const firebaseConfig = {
        apiKey: "AIzaSyCZwI7mJgw3AuANVJAjxzODtwNzB5GosvI",
        authDomain: "no-right-turns.firebaseapp.com",
        projectId: "no-right-turns",
        storageBucket: "no-right-turns.appspot.com",
        messagingSenderId: "473217543328",
        appId: "1:473217543328:web:048c68aa47664ae643edc5",
        measurementId: "G-2XKBQV8GTZ"
    };

    if (firebase.apps.length === 0) {
        firebase.initializeApp(firebaseConfig);
        firebase.analytics();
    }
}
export default firebase;
