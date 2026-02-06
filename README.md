# Nexus

Android NFC pass wallet with Google authentication, multi-account switching, and Supabase sync.

## Features

- **NFC HCE Emulation** - Emulate NFC passes (Type 4 Tag, NDEF) for contactless access
- **Google Sign-In** - Authenticate via Google with Supabase backend
- **Multi-Account** - Switch between multiple Google accounts without re-authentication
- **Offline-First** - Room DB is the source of truth; syncs with Supabase when online
- **Business/Personal Roles** - Choose your account type for different experiences
- **Cross-Platform Sync** - Passes sync between mobile and the [Nexus Web Dashboard](https://github.com/Kryptoxotis/nexus-web)

## Tech Stack

- Kotlin + Jetpack Compose
- Room Database (offline storage)
- Supabase (auth, database, realtime)
- Android NFC HCE API
- Google Credential Manager

## Setup

1. Clone this repo
2. Copy `secrets.properties.template` to `secrets.properties`
3. Fill in your Supabase URL, Anon Key, and Google Web Client ID
4. Open in Android Studio and run

## Web Dashboard

See [nexus-web](https://github.com/Kryptoxotis/nexus-web) for the companion web app.
