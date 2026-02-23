# NFC Pass - Digital Access Control via NFC

A proof-of-concept Android application that emulates physical access cards using NFC Host Card Emulation (HCE). Users can store multiple passes (gym memberships, office badges, etc.) and activate one for NFC-based access control.

## Features

- **User Authentication**: Secure sign-up and login with Supabase Auth
- **Pass Management**: Add, activate, and delete multiple passes
- **NFC Emulation**: Emulate passes via NFC HCE (no physical card needed)
- **Offline Support**: Local database caching with Room for offline access
- **Cloud Sync**: Automatic synchronization with Supabase backend
- **Access Logging**: Track when and where passes are used

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Offline-First**: Room database for local caching
- **Jetpack Compose**: Modern declarative UI
- **Kotlin Coroutines**: Async operations and Flow for reactive data
- **Supabase Backend**: Authentication, PostgreSQL database, and realtime sync

## Prerequisites

- Android Studio (latest version)
- Android device with NFC HCE support (API 24+)
- Supabase account (free tier works)
- JDK 17

## Project Structure

```
nfc-pass-app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/nfcpass/
│   │       │   ├── data/
│   │       │   │   ├── local/           # Room database
│   │       │   │   ├── remote/          # Supabase client
│   │       │   │   └── repository/      # Repository pattern
│   │       │   ├── domain/              # Business models
│   │       │   ├── presentation/        # UI (Compose screens, ViewModels)
│   │       │   ├── service/             # NFC HCE service
│   │       │   └── MainActivity.kt
│   │       ├── res/                     # Resources
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── .github/workflows/                   # CI/CD
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd nfc-pass-app
```

### 2. Set Up Supabase

1. Create a new project at [supabase.com](https://supabase.com)
2. Get your project URL and anon key from Settings > API
3. Run the following SQL in the Supabase SQL Editor:

```sql
-- Create passes table
CREATE TABLE passes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    pass_id TEXT NOT NULL,
    pass_name TEXT NOT NULL,
    organization TEXT NOT NULL,
    is_active BOOLEAN DEFAULT false,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

-- Create access_logs table
CREATE TABLE access_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pass_id UUID NOT NULL REFERENCES passes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    location TEXT,
    reader_id TEXT,
    access_granted BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT now()
);

-- Create indexes
CREATE INDEX idx_passes_user_id ON passes(user_id);
CREATE INDEX idx_passes_is_active ON passes(is_active);
CREATE INDEX idx_access_logs_pass_id ON access_logs(pass_id);
CREATE INDEX idx_access_logs_user_id ON access_logs(user_id);

-- Enable Row Level Security
ALTER TABLE passes ENABLE ROW LEVEL SECURITY;
ALTER TABLE access_logs ENABLE ROW LEVEL SECURITY;

-- RLS Policies for passes table
CREATE POLICY "Users can view their own passes"
    ON passes FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own passes"
    ON passes FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own passes"
    ON passes FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own passes"
    ON passes FOR DELETE
    USING (auth.uid() = user_id);

-- RLS Policies for access_logs table
CREATE POLICY "Users can view their own access logs"
    ON access_logs FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own access logs"
    ON access_logs FOR INSERT
    WITH CHECK (auth.uid() = user_id);
```

### 3. Configure the App

1. Copy the secrets template:
   ```bash
   cp secrets.properties.template secrets.properties
   ```

2. Edit `secrets.properties` with your Supabase credentials:
   ```properties
   SUPABASE_URL=https://your-project-ref.supabase.co
   SUPABASE_ANON_KEY=your-anon-key-here
   ```

3. **IMPORTANT**: Never commit `secrets.properties` to Git!

### 4. Build the App

#### Option A: Android Studio
1. Open the project in Android Studio
2. Sync Gradle (File > Sync Project with Gradle Files)
3. Build > Build APK
4. Install via USB: Connect phone and click Run

#### Option B: Command Line
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 5. Configure GitHub Actions (Optional)

To enable automatic APK builds on GitHub:

1. Go to your GitHub repository
2. Navigate to Settings > Secrets and variables > Actions
3. Add the following secrets:
   - `SUPABASE_URL`: Your Supabase project URL
   - `SUPABASE_ANON_KEY`: Your Supabase anon key

Now, every push to `main` or `develop` will automatically build an APK. Download it from the Actions tab > Artifacts.

## How to Use

### Adding a Pass

1. Sign up or log in to the app
2. Tap the **+** button
3. Enter pass details:
   - **Pass ID**: Unique identifier (e.g., card number)
   - **Pass Name**: Friendly name (e.g., "Gym Membership")
   - **Organization**: Issuer (e.g., "Gold's Gym")
   - **Expiry Date** (optional): YYYY-MM-DD format
4. Tap **Add Pass**

### Activating a Pass

1. From the pass list, tap **Activate** on the desired pass
2. Only one pass can be active at a time
3. The active pass will be shown at the top with a green indicator

### Using NFC

1. Ensure NFC is enabled on your device (Settings > Connected devices > Connection preferences > NFC)
2. Activate a pass in the app
3. Hold your phone near an NFC reader
4. The reader will receive your pass ID

## Testing NFC Emulation

Since you need an NFC reader to test, here are some options:

### Option 1: Use Another Android Phone
1. Install **NFC Tools** app on a second Android phone
2. Open NFC Tools and go to **Read** tab
3. Tap the two phones together (back-to-back)
4. The pass ID should appear in NFC Tools

### Option 2: Use a Physical NFC Reader
1. Connect an ACR122U or similar USB NFC reader to your PC
2. Use software like **NFC Tools PC** to read the emulated card
3. The AID is `F0394148148100`

### APDU Commands

The app responds to these APDU commands:

1. **SELECT AID**: `00 A4 04 00 06 F0394148148100`
   - Response: `90 00` (success)

2. **GET DATA**: `00 CA 00 00`
   - Response: `<pass_id> 90 00`

## Troubleshooting

### NFC Not Working

1. **Check NFC is enabled**: Settings > Connected devices > NFC
2. **Check HCE support**: Your device must support HCE (Android 4.4+)
3. **Check logs**: Use `adb logcat | grep NFCPass` to see NFC events
4. **Restart app**: Kill and relaunch the app after activating a pass

### Build Errors

1. **Missing secrets.properties**: Copy from `secrets.properties.template`
2. **Gradle sync failed**: File > Invalidate Caches / Restart
3. **Kotlin version mismatch**: Update Kotlin plugin in Android Studio

### Sync Issues

1. **Check internet connection**: App requires internet to sync
2. **Check Supabase status**: Visit [status.supabase.com](https://status.supabase.com)
3. **Check RLS policies**: Ensure policies are created in Supabase
4. **View logs**: Check Logcat for "NFCPass:PassRepository" messages

## Known Limitations

- **One active pass**: Only one pass can be active at a time
- **No iOS support**: HCE is Android-only (iOS uses Apple Wallet)
- **Reader compatibility**: Not all NFC readers support HCE emulation
- **No biometric lock**: Pass activation doesn't require authentication (future enhancement)

## Security Considerations

1. **RLS Policies**: Supabase enforces user-level data isolation
2. **HTTPS Only**: All API calls use HTTPS
3. **Local Storage**: Room database is encrypted (future: enable SQLCipher)
4. **ProGuard**: Release builds obfuscate code
5. **No secrets in code**: Credentials loaded from secrets.properties

## Future Enhancements

### Phase 2
- QR code scanning to add passes
- Push notifications (pass expiring, access denied)
- Pass sharing between users
- Biometric authentication before pass activation

### Phase 3
- Admin web dashboard for businesses
- Analytics (usage statistics, popular times)
- Multi-organization support
- Offline validation mode (for readers without internet)

### Phase 4
- Bluetooth/WiFi fallback
- Geofencing (auto-activate pass at location)
- Payment integration
- White-label solutions

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit with descriptive messages
4. Add tests for new functionality
5. Submit a pull request

## License

MIT License - See LICENSE file for details

## Support

For issues or questions:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the troubleshooting section above

## Technical Details

### NFC HCE Implementation

The app uses Android's Host Card Emulation API to emulate an NFC card:

- **AID**: F0394148148100 (custom Application ID)
- **Service**: `NFCPassService` extends `HostApduService`
- **Commands**: SELECT and GET DATA APDU commands
- **Response**: Active pass ID as ASCII bytes + status word (9000)

See `app/src/main/java/com/nfcpass/service/NFCPassService.kt` for implementation details.

### Database Schema

**passes**:
- `id` (UUID): Primary key
- `user_id` (UUID): References auth.users
- `pass_id` (TEXT): The emulated pass identifier
- `pass_name` (TEXT): User-friendly name
- `organization` (TEXT): Issuer organization
- `is_active` (BOOLEAN): Whether this pass is active for NFC
- `expiry_date` (TIMESTAMP): Optional expiration
- `created_at` / `updated_at`: Timestamps

**access_logs**:
- `id` (UUID): Primary key
- `pass_id` (UUID): References passes
- `user_id` (UUID): References auth.users
- `location` (TEXT): Optional location
- `reader_id` (TEXT): Optional reader identifier
- `access_granted` (BOOLEAN): Access result
- `created_at`: Timestamp

## Credits

Built with:
- [Supabase](https://supabase.com) - Backend
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI
- [Room](https://developer.android.com/training/data-storage/room) - Local database
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async operations

---

**Disclaimer**: This is a proof-of-concept application for educational purposes. For production use, implement additional security measures such as biometric authentication, certificate pinning, and hardware-backed key storage.
