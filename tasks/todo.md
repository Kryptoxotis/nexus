# Share Link → Instant NFC Emulate

## Tasks
- [x] 1. Update `AndroidManifest.xml` — add `singleTop` launch mode + `ACTION_SEND` intent filter
- [x] 2. Update `MainActivity.kt` — handle share intent, add `_sharedUrl` state, navigate to share screen
- [x] 3. Create `SharedLinkScreen.kt` — minimal UI with NFC icon, URL display, Save/Discard buttons
- [ ] 4. Build and verify

## Review

### Changes Summary

**AndroidManifest.xml** (2 additions)
- Added `android:launchMode="singleTop"` to prevent duplicate activities when shared to repeatedly
- Added `ACTION_SEND` + `text/plain` intent filter so Nexus appears in the Android share sheet

**MainActivity.kt** (~25 lines added)
- Added `_sharedUrl` MutableStateFlow to hold the incoming shared URL
- Added `handleShareIntent()` that extracts text from `ACTION_SEND` intents and writes it to `NdefCache.writeUri()` for instant NFC emulation
- Called `handleShareIntent()` from both `onCreate` and `onNewIntent`
- Added `"share_link"` composable route with Save (calls `addCard` then navigates to wallet) and Discard (clears NdefCache then finishes activity) callbacks
- Added `LaunchedEffect(sharedUrl)` to auto-navigate to the share screen when a URL is shared

**SharedLinkScreen.kt** (NEW, ~100 lines)
- Pure presentation composable: Share icon + "NFC Ready" header + URL display box + Save/Discard buttons
- No ViewModel dependency — just callbacks
- Uses existing theme colors (NexusBackground, NexusTeal, NexusSurface, NexusTextPrimary, NexusTextSecondary)

### What was NOT changed
- No DB schema changes
- No new ViewModels or repositories
- No changes to NdefCache, PersonalCardViewModel, or NFCPassService
- Reuses existing `NdefCache.writeUri()` and `PersonalCardViewModel.addCard()` as-is
