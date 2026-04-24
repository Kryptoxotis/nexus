# Nexus

**Your digital pass wallet.**

> ⚠️ Description placeholder — full vision statement coming soon.

Nexus replaces physical cards. Share your digital business card with a single NFC tap or QR scan. Businesses use Nexus to manage memberships, run check-ins, and track access — no physical cards, no third-party readers required.

Built for Android. Web app in development. Play Store release coming soon.

---

## What Nexus Does

**For individuals:**
- Create personal digital cards (contact info, social links, custom data)
- Share via NFC tap — the other phone doesn't need the app installed
- QR code fallback for iOS and non-NFC devices
- Store multiple cards in one wallet, switch between them instantly

**For businesses:**
- Issue digital membership passes to customers
- Manage members, track check-ins, and view access logs
- Invite-based enrollment with PIN or open enrollment modes
- Admin portal with full user and organization management

---

## Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Auth**: Google Sign-In via Credential Manager + Supabase Auth
- **Backend**: Supabase (self-hosted) — PostgreSQL, PostgREST, Realtime
- **NFC**: Android HCE (Host Card Emulation) — custom AID, no payment emulation

---

## Getting Started

### Requirements

- Android device with NFC HCE support (API 26+)
- JDK 17
- A Supabase instance (cloud or self-hosted)

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/Kryptoxotis/nexus.git
   cd nexus
   ```

2. Copy the secrets template:
   ```bash
   cp secrets.properties.template secrets.properties
   ```

3. Fill in `secrets.properties`:
   ```properties
   SUPABASE_URL=https://your-supabase-url.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   GOOGLE_WEB_CLIENT_ID=your-google-web-client-id.apps.googleusercontent.com
   ```

4. Apply the database migrations in `/supabase/migrations/` to your Supabase project in order.

5. Build and install:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Google Sign-In Setup

1. Create a Web OAuth client in Google Cloud Console
2. Create an Android OAuth client with your app's package name and debug SHA-1
3. Add the web client ID to `secrets.properties` as `GOOGLE_WEB_CLIENT_ID`

---

## Project Structure

```
nexus/
├── app/                        # Pure Android app (main)
│   └── src/main/java/com/kryptoxotis/nexus/
│       ├── data/               # Supabase client, DTOs, repositories
│       ├── domain/             # Models, business logic
│       ├── presentation/       # Compose screens, ViewModels, theme
│       └── service/            # NFC HCE service
├── multiplatform/              # Kotlin Multiplatform (Android + iOS, experimental)
├── supabase/
│   └── migrations/             # SQL migrations (apply in order)
└── secrets.properties.template
```

---

## Database Schema

All tables live in the `nexus` schema. See `/supabase/migrations/` for the full schema with RLS policies.

**Core tables:** `profiles`, `personal_cards`, `organizations`, `business_passes`, `business_requests`, `enrollment_pins`, `access_logs`, `file_storage_links`

---

## Screenshots

*Coming soon.*

---

## Play Store

*Coming soon.*

---

## Security

- Row Level Security enforced on all tables via Supabase
- No secrets committed to version control
- Release builds use ProGuard minification and resource shrinking
- NFC uses a custom AID — no payment card emulation
- Google Sign-In only — no password storage

---

## License

MIT License. See `LICENSE` for details.
