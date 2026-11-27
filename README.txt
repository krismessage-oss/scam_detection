SMS SHIELD - ANDROID TEST PROJECT

This is an experimental Android Studio starter project.
It includes:
- Python rule-based message classifier
- WebView dashboard (HTML/CSS/JavaScript)
- Kotlin SMS inbox scanner
- Classification metadata stored locally
- Bulk deletion guarded to Spam/Scam classifications only

IMPORTANT LIMITATIONS:
- Not a production-ready/default-SMS replacement yet.
- The Python detector is a keyword prototype, not a trained or calibrated AI model.
- "Legitimate" cannot be inferred reliably from absence of suspicious words. The current
  prototype deliberately sends most uncertain messages to Review.
- Incoming SMS receiver is a scaffold only; it logs delivery but does not yet persist
  incoming messages or notify the user. Do not rely on it for live SMS protection.
- MMS and RCS are not implemented. RCS is not exposed through the standard SMS provider.
- Deletion from the SMS provider is permanent. Back up messages and test only with
  messages you are willing to lose.
- Android SMS permissions/default-app behavior varies by Android version and distribution policy.

REQUIREMENTS:
- Android Studio
- JDK 17
- Android SDK Platform 35
- Internet for first Gradle sync
- Android phone or emulator

RUN:
1. Extract the ZIP and open the SMS_Shield folder in Android Studio.
2. Wait for Gradle sync. Install SDK 35 if prompted.
3. Connect a test phone with USB debugging enabled.
4. Build and run the app.
5. Tap "Set as default SMS app" and accept if Android allows the role request.
6. Tap "Scan existing messages".
7. Review every classification. Only select test Spam/Scam messages for deletion.

This starter has not been compiled or tested on a physical device. It is supplied as
source code for Android Studio, not as an installable APK.
