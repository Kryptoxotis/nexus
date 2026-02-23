# Quick Start Guide

Get your NFC Pass app running in 5 minutes!

## Prerequisites Checklist

- [ ] Android Studio installed
- [ ] Android phone with NFC (API 24+)
- [ ] USB cable to connect phone

## Step 1: Open Project (30 seconds)

```bash
# Open Android Studio
# Click: File > Open
# Navigate to: C:\Users\ogays\nfc-pass-app
# Click: OK
```

Wait for Gradle sync to complete (first time may take a few minutes).

## Step 2: Build APK (1 minute)

In Android Studio:
```
Build > Build APK
```

Or use command line:
```bash
cd C:\Users\ogays\nfc-pass-app
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Step 3: Install on Phone (30 seconds)

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

## Step 4: First Use (2 minutes)

1. **Open app** on your phone
2. **Enable NFC**: Settings > Connected devices > NFC (if not already enabled)
3. **Sign up**: Enter email and password (min 6 characters)
4. **Add a pass**:
   - Tap the **+** button
   - Pass ID: `TEST123`
   - Pass Name: `Test Pass`
   - Organization: `My Gym`
   - Tap "Add Pass"
5. **Activate pass**: Tap "Activate" on your new pass

## Step 5: Test NFC (1 minute)

### Option A: Test with Another Phone
1. Install "NFC Tools" app on second Android phone
2. Open NFC Tools > Read tab
3. Tap phones together (back-to-back)
4. Should see: `TEST123` (your pass ID)

### Option B: Test with NFC Reader
1. Hold phone near NFC reader
2. Reader should receive: `TEST123`

## Troubleshooting

**App won't build?**
- Ensure Android Studio is up to date
- File > Invalidate Caches / Restart

**NFC not working?**
- Check NFC is enabled in phone settings
- Restart app after activating pass
- Check Logcat: `adb logcat | grep NFCPass`

**Can't sign up?**
- Check you have internet connection
- Password must be at least 6 characters
- Use valid email format

## What's Next?

- Read the full [README.md](README.md) for detailed documentation
- Add real passes from your gym, office, etc.
- Test access logging in [Supabase Dashboard](https://supabase.com/dashboard)
- Set up GitHub Actions for automatic builds

## Quick Reference

**Supabase Dashboard**: https://supabase.com/dashboard/project/fxtccxljxzbbbtgcfesr

**View Logs**:
```bash
adb logcat | grep NFCPass
```

**Rebuild App**:
```bash
./gradlew clean assembleDebug
```

**Common AID**: `F0394148148100`

---

**Need Help?** Check the [README.md](README.md) troubleshooting section or [SETUP_NOTES.md](SETUP_NOTES.md) for more details.
