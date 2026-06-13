# SMS Forwarder — Setup Guide

Forwards every incoming SMS from your **Redmi 13** to your **JioBharat feature phone** via regular SMS (no internet needed).

---

## ⚡ Option A: Build APK via GitHub (No Android Studio needed — Recommended)

This is the easiest way. GitHub builds the APK for you in ~3 minutes for free.

### Steps
1. Go to [https://github.com](https://github.com) → Sign up (free) or log in.
2. Click **"New repository"** → name it `SmsForwarder` → click **Create repository**.
3. Upload all the files from the `SmsForwarder/` folder (drag & drop them on GitHub, maintaining folder structure), OR use GitHub Desktop app.
4. Once uploaded, GitHub automatically runs the build (you can see progress under the **Actions** tab).
5. When the build turns **green ✅**, click on it → scroll down to **Artifacts** → download **SmsForwarder-debug.zip**.
6. Unzip it — you'll find `app-debug.apk` inside. Copy it to your Redmi 13 and install it.

> The `.github/workflows/build.yml` file already included in this project handles everything automatically.

---

## Option B: Build the APK with Android Studio

### Prerequisites
- [Android Studio](https://developer.android.com/studio) installed on your PC/Mac
- JDK 17 (bundled with Android Studio)

### Steps

1. Open Android Studio → **File → Open** → select the `SmsForwarder` folder.
2. Wait for Gradle sync to finish (first time downloads ~200 MB).
3. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. The APK will be at:
   ```
   SmsForwarder/app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 1. Build the APK (summary)

### Prerequisites
- [Android Studio](https://developer.android.com/studio) installed on your PC/Mac
- JDK 17 (bundled with Android Studio)

### Steps
1. Open Android Studio → **File → Open** → select the `SmsForwarder` folder.
2. Wait for Gradle sync to finish (first time downloads ~200 MB).
3. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. The APK will be at:
   ```
   SmsForwarder/app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 2. Install on Redmi 13

### Enable sideloading
1. On the Redmi 13: **Settings → About phone** → tap **MIUI version** 7 times to enable Developer Options.
2. **Settings → Additional settings → Developer options** → turn on **USB debugging** and **Install via USB**.
3. Connect phone to PC with USB cable.
4. In Android Studio terminal (or any terminal):
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   Or simply copy the APK to your phone and open it in **Files** to install.

### If "Install unknown apps" prompt appears
Settings → Apps → Special app access → Install unknown apps → **Files** → Allow.

---

## 3. Configure the App

1. Open **SMS Forwarder** on the Redmi 13.
2. Grant all three permissions when prompted: **Receive SMS, Read SMS, Send SMS**.
3. Enter the JioBharat phone number with country code, e.g. `+919876543210`.
4. Tap **Save Number**.
5. Toggle **Enable Forwarding** ON.
6. Status should show: ✅ Forwarding ON → +91xxxxxxxxxx

---

## 4. Disable MIUI Battery Optimisation (CRITICAL)

MIUI kills background apps very aggressively. Without this step, forwarding **will stop** when your screen turns off.

**Option A (easiest):** Tap **"Disable Battery Optimisation"** inside the app — it takes you directly to the setting. Select **No restrictions**.

**Option B (manual path):**
- Settings → Battery & performance → App battery saver → SMS Forwarder → **No restrictions**

**Option C — Auto-start (also recommended):**
- Settings → Apps → Manage apps → SMS Forwarder → **Autostart** → Enable

---

## 5. Test It

1. Ask someone to send an SMS to your Redmi 13 (or send one from another phone).
2. Within seconds, the JioBharat phone should receive a forwarded SMS in the format:
   ```
   [FWD] From: +91XXXXXXXXXX
   Your original message here
   ```
3. OTPs, bank alerts, and all other SMS will be forwarded the same way.

---

## Project File Structure

```
SmsForwarder/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          ← Permissions + receiver registration
│   │   ├── java/com/smsforwarder/
│   │   │   ├── MainActivity.java        ← UI: enter number, toggle, battery opt
│   │   │   ├── SmsReceiver.java         ← Core: catches & forwards every SMS
│   │   │   ├── BootReceiver.java        ← Re-activates after phone restart
│   │   │   └── PrefsHelper.java         ← Saves target number & enabled state
│   │   └── res/
│   │       ├── layout/activity_main.xml
│   │       ├── drawable/edittext_border.xml
│   │       └── values/strings.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
└── settings.gradle
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| Forwarding stops when screen off | Disable battery optimisation + enable Autostart (Step 4) |
| "Permission denied" on install | Enable "Install unknown apps" for Files app |
| SMS not arriving on JioBharat | Check if the number is correct with country code (+91) |
| App crashes on launch | Re-grant all 3 SMS permissions in Settings → Apps → SMS Forwarder → Permissions |
| Long OTPs get split into 2 texts | Normal — SmsManager splits at 160 chars. JioBharat will show both parts. |
