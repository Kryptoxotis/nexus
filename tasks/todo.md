# CMP Port — Compilation Fixes

## Issues Fixed

### Critical: DI / Database Wiring
- [x] 1. AppModule creates NexusDatabase + local data sources and passes them to repositories
- [x] 2. PersonalCardRepository gets PersonalCardLocalDataSource via AppModule.init()
- [x] 3. ReceivedCardRepository gets ReceivedCardLocalDataSource via AppModule.init()
- [x] 4. BusinessPassRepository gets BusinessPassLocalDataSource via AppModule.init()
- [x] 5. DatabaseDriverFactory.android gets Context from MainActivity
- [x] 6. Removed unused AdminRepository import from AppModule

### Critical: Platform Fixes
- [x] 7. Fixed UrlLauncher.android.kt — uses application context via UrlLauncherContext singleton
- [x] 8. NfcManager.android already has Context param — wired separately
- [x] 9. Supabase URL/key set in MainActivity.onCreate() and iOS initApp()

### Build Infrastructure
- [x] 10. Added Gradle wrapper (gradlew, gradle-wrapper.jar, gradle-wrapper.properties)
- [x] 11. Copied local.properties from parent project for Android SDK path

### Compilation Fixes
- [x] 12. kotlinx-datetime 0.6.1 → 0.7.1 (pulled by Supabase BOM), `Clock`/`Instant` moved to kotlin.time
- [x] 13. Added @OptIn(ExperimentalTime::class) to all repositories + BusinessPass
- [x] 14. Fixed AuthManager signInWith — IDToken from builtin package
- [x] 15. Fixed AccountSwitcherScreen — nullable displayName
- [x] 16. Added kotlin.native.ignoreDisabledTargets + disabled hierarchy template warnings

### iOS Setup
- [ ] 17. Create Xcode project (xcodeproj) — manual step, requires macOS

### Final
- [x] 18. ✅ `assembleDebug` BUILD SUCCESSFUL — 23MB debug APK at composeApp/build/outputs/apk/debug/

## Review

### What Was Fixed
- **DI wiring**: AppModule.init(DatabaseDriverFactory) creates the SQLDelight NexusDatabase and passes it through local data sources to repositories
- **Platform context**: Android UrlLauncher uses `UrlLauncherContext.appContext` set in MainActivity; DatabaseDriverFactory receives Context
- **kotlinx-datetime 0.7.x migration**: `Clock` and `Instant` moved from `kotlinx.datetime` to `kotlin.time` stdlib, requires `@OptIn(ExperimentalTime::class)`
- **Supabase Auth**: `IDToken` provider is in `io.github.jan.supabase.auth.providers.builtin`, not `providers`
- **iOS entry point**: `initApp()` called in Swift to set Supabase credentials + init database before UI loads

### Build Status
- **Android debug APK**: ✅ Builds successfully
- **Android release APK**: Needs signing config
- **iOS**: Needs macOS with Xcode to build (iosMain targets disabled on Windows)
