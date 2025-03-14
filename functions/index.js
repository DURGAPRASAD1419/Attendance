const functions = require("firebase-functions");
const admin = require("firebase-admin");
const twilio = require("twilio");

// Twilio Credentials
const accountSid = "your_twilio_sid";
const authToken = "your_twilio_auth_token";
const client = new twilio(accountSid, authToken);

admin.initializeApp();

exports.sendAuthNotification = functions.database.ref("/users/{userId}/authStatus")
    .onUpdate(async (change, context) => {
        const userId = context.params.userId;
        const newValue = change.after.val();

        if (newValue === "Authenticated") {
            const userRef = admin.database().ref(`/users/${userId}`);
            const snapshot = await userRef.once("value");
            const userData = snapshot.val();

            if (userData && userData.phone && userData.fcmToken) {
                // Send FCM Notification
                const message = {
                    notification: {
                        title: "Fingerprint Authentication",
                        body: `Hello ${userData.name}, your fingerprint has been authenticated.`,
                    },
                    token: userData.fcmToken,
                };

                await admin.messaging().send(message);

                // Send SMS
                await client.messages.create({
                    body: `Hello ${userData.name}, you have successfully authenticated with fingerprint.`,
                    from: "your_twilio_number",
                    to: userData.phone
                });

                console.log("Notification & SMS sent to:", userData.phone);
            }
        }
    });
