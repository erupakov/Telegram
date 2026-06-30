## Telegram messenger for Android (Divo Fork)

[Telegram](https://telegram.org) is a messaging app with a focus on speed and security. It’s superfast, simple and free.
This repo contains the modified source code for the Divo Global project, built on top of the Telegram App for Android.

### Compilation Guide

**Note**: In order to support reproducible builds, this repo relies on `local.properties` and `google-services.json` which are not checked into version control. Before compiling, please make sure to set these up.

You will require **Android Studio Hedgehog (2023.1.1)**, **JDK 17**, **Android SDK 35**, and **Android NDK rev. 21.4.7075529**.

1. Clone the repository: `git clone <your-repo-url>`
2. Copy your `release.keystore` into `TMessagesProj/config/`
3. Create or edit `local.properties` in the root of the project to include your SDK/NDK paths and required keys:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ndk.dir=/path/to/Android/Sdk/ndk/21.4.7075529
   
   # Telegram API Keys (Obtain from https://my.telegram.org)
   TELEGRAM_APP_ID=your_api_id
   TELEGRAM_APP_HASH=your_api_hash
   
   # Keystore Credentials
   RELEASE_KEY_PASSWORD=...
   RELEASE_KEY_ALIAS=...
   RELEASE_STORE_PASSWORD=...

4. Go to the [Firebase Console](https://console.firebase.google.com/), download the `google-services.json` for this project, and copy it to the `TMessagesProj_App/` folder.
5. Open the project in Android Studio (note that it should be **opened**, NOT imported).
6. You are ready to compile. You can build via the IDE or using Gradle:
   ```bash
   ./gradlew :TMessagesProj_App:assembleAfatDebug
   ```

### API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api
MTproto protocol manuals: https://core.telegram.org/mtproto
Security guidelines: https://core.telegram.org/mtproto/security_guidelines

### Localization

We moved all translations to https://translations.telegram.org/en/android/. Please use it.
