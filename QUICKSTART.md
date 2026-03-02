# Nexus - Quick Start Guide

Get your Nexus NFC business card app running in 5 minutes!

## Prerequisites Checklist

- [ ] Android Studio installed
- [ ] Android phone with NFC (API 26+)
- [ ] USB cable to connect phone

## Step 1: Open Project (30 seconds)

```bash
# Open Android Studio
# Click: File > Open
# Navigate to the cloned nexus project folder
# Click: OK
```

Wait for Gradle sync to complete (first time may take a few minutes).

## Step 2: Configure Secrets (1 minute)

Create a `secrets.properties` file in the project root:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
GOOGLE_WEB_CLIENT_ID=your-google-client-id
```

Get these values from the [Supabase Dashboard](https://supabase.com/dashboard) > Settings > API.
Without this file, the app will build but fail to connect to the backend.

## Step 3: Build APK (1 minute)

In Android Studio:
```
Build > Build APK
```

Or use command line:
```bash
cd <path-to-project>/nexus
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Step 4: Install on Phone (30 seconds)

### Via Android Studio:
1. Connect phone via USB
2. Enable USB debugging on phone
3. Click "Run" button (green play icon)

### Via ADB:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Via File Transfer:
1. Copy APK to phone
2. Open APK on phone
3. Allow "Install unknown apps"

## Step 5: First Use (2 minutes)

1. **Open app** on your phone
2. **Enable NFC**: Settings > Connected devices > NFC (if not already enabled)
3. **Sign up**: Enter email and password (min 6 characters)
4. **Create a card**:
   - Tap the **+** button
   - Enter your name, title, and company
   - Add contact links (email, phone, website)
   - Choose a card color and shape
   - Tap "Save"
5. **Activate card**: Tap "Activate" on your new card

## Step 6: Test NFC (1 minute)

### Option A: Test with Another Phone
1. Install "NFC Tools" app on second Android phone
2. Open NFC Tools > Read tab
3. Tap phones together (back-to-back)
4. Should receive your Nexus profile URL

### Option B: Test with NFC Reader
1. Hold phone near NFC reader
2. Reader should receive your card data

## Troubleshooting

**App won't build?**
- Ensure Android Studio is up to date
- File > Invalidate Caches / Restart

**NFC not working?**
- Check NFC is enabled in phone settings
- Restart app after activating card
- Check Logcat: `adb logcat | grep "Nexus:"`

**Can't sign up?**
- Check you have internet connection
- Password must be at least 6 characters
- Use valid email format

## What's Next?

- Read the full [README.md](README.md) for detailed documentation
- Create and share your digital business cards
- Test access logging in [Supabase Dashboard](https://supabase.com/dashboard)
- Set up GitHub Actions for automatic builds

## Quick Reference

**View Logs**:
```bash
adb logcat | grep "Nexus:"
```

**Rebuild App**:
```bash
./gradlew clean assembleDebug
```

**NDEF AID**: `D2760000850101`

---

**Need Help?** Check the [README.md](README.md) troubleshooting section or [SETUP_NOTES.md](SETUP_NOTES.md) for more details.
