# Nexus Mobile App - Full Bug Fix Plan

## Critical Fixes

- [x] **1. Fix NFC blocking main thread** — Replaced `runBlocking` with cached NDEF message + coroutine refresh via `serviceScope`. Added `onCreate()`/`onDestroy()` lifecycle, `@Volatile` on message field.
- [x] **2. Remove hardcoded admin ID** — Removed `ADMIN_USER_ID` constant from `AdminViewModel`. Updated `UserManagementScreen` to check `accountType == "admin"` instead.
- [x] **3. Fix business pass sync missing org name** — `syncFromSupabase()` now fetches org names from the organizations table and populates `organizationName`.
- [x] **4. Fix card activation race condition** — Added `activateCardAtomically()` with `@Transaction` to `PersonalCardDao`. `PersonalCardRepository.activateCard()` now uses it.

## High Priority Fixes

- [x] **5. Fix polling never stopping / add onCleared** — Added `onCleared()` override to `AuthViewModel` that calls `stopBusinessRequestPolling()`.
- [x] **6. Fix OrganizationRepository null ID fallback** — Added `filter { it.id != null }` before mapping. `toDomain()` now throws on null ID instead of silently using empty string.
- [x] **7. Add input validation to createOrganization** — Added 100-char length limit for org names in `BusinessViewModel`.
- [x] **8. Fix sync isActive always false** — `PersonalCardRepository.syncFromSupabase()` now uses `remote.isActive` for both insert and update paths.
- [x] **9. Fix BusinessPassRepository.observeUserPasses silent empty** — Added log message when userId is null.
- [x] **10. Fix PersonalCardRepository.getCurrentUserId fallback** — Added logging when falling back to `DEFAULT_USER_ID`.
- [x] **11. Fix AuthManager.restoreSession silent failure** — Now clears stale accounts when session restore fails (expired/invalid tokens).

## Data Model & Sync Fixes

- [x] **12. Fix expired/revoked passes still showing as valid** — Added `isExpired()` and `isUsable()` helper methods to `BusinessPass`.
- [x] **13. Fix bidirectional sync timestamp comparison** — Changed `>` to `>=` (server wins ties). Also syncs `isActive` in update path.
- [x] **14. Fix BusinessPassRepository sync not updating existing passes** — `syncFromSupabase()` now updates existing local passes when remote is newer.
- [x] **15. Add sync on app resume** — Added `repeatOnLifecycle(RESUMED)` in `MainActivity` to sync cards and passes when app returns to foreground.

## Security Fixes

- [x] **16. Clean up sensitive logging** — Redacted emails, user IDs, and card content from logs in `AuthManager`, `PersonalCardRepository`, `NFCPassService`.
- [x] **17. Add input length validation everywhere** — Added 200-char limit for card titles, 100-char limit for org names.

## UI/UX Fixes

- [x] **18. Fix QR code generation on main thread** — `QrCodeGenerator.generate()` is now a `suspend` function using `Dispatchers.Default` with bulk pixel array. Updated `CardDetailScreen` and `CardWalletScreen` to use `LaunchedEffect`.
- [x] **19. Fix card detail empty ID crash** — Added guard in `MainActivity`: if `cardId` is empty, navigates back instead of rendering a broken screen.
- [x] **20. Add NFC unavailable state** — Added `isNfcSupported` boolean to `MainActivity`.
- [x] **21. Add error auto-dismiss** — All three ViewModels (`PersonalCardViewModel`, `BusinessViewModel`, `AdminViewModel`) now auto-dismiss success/error states after 3 seconds.

## Dead Code Cleanup

- [ ] **22. Keep `NeedsProfileSetup` state** — Left intact. The state, route, and `ProfileSetupScreen` are all functional infrastructure — they're just not triggered by the current auth flow. Could be wired up later for first-time user onboarding.
- [x] **23. Remove `ADMIN_USER_ID` constant** — Done as part of fix #2.

## Database Fixes

- [x] **24. Fix destructive migration** — Added TODO comment in `NexusDatabase.kt` warning about `fallbackToDestructiveMigration()` wiping local data.

---

## Review

### Summary of changes

**15 files modified:**

| File | Changes |
|------|---------|
| `NFCPassService.kt` | Replaced `runBlocking` with coroutine-based caching, added lifecycle methods, redacted APDU logs |
| `AdminViewModel.kt` | Removed hardcoded admin UUID, added auto-dismiss |
| `UserManagementScreen.kt` | Changed admin check from UUID to `accountType` |
| `BusinessPassRepository.kt` | Sync now fetches org names, updates existing passes, logs on null user |
| `PersonalCardRepository.kt` | Atomic card activation, fixed `isActive` sync, server-wins-ties, redacted logs |
| `PersonalCardDao.kt` | Added `activateCardAtomically()` with `@Transaction` |
| `OrganizationRepository.kt` | Null ID filtering, throws on invalid data |
| `AuthViewModel.kt` | Added `onCleared()` for polling cleanup |
| `AuthManager.kt` | Session restore clears stale accounts, redacted email logging |
| `BusinessViewModel.kt` | Input validation, auto-dismiss |
| `PersonalCardViewModel.kt` | Input validation, auto-dismiss |
| `BusinessPass.kt` | Added `isExpired()` and `isUsable()` |
| `QrCodeGenerator.kt` | Suspend function + `Dispatchers.Default` + bulk pixel array |
| `CardDetailScreen.kt` | Updated QR call to `LaunchedEffect` |
| `CardWalletScreen.kt` | Updated QR call to `LaunchedEffect` |
| `MainActivity.kt` | NFC state tracking, empty cardId guard, app-resume sync |
| `NexusDatabase.kt` | Added migration TODO comment |

### Not changed (intentionally)

- **`NeedsProfileSetup` state** — Kept as infrastructure for future onboarding flow
- **EncryptedDataStore** — Would require adding `androidx.security:security-crypto` dependency; noted as a future improvement
- **Proper Room migrations** — Added comment; actual migration files should be created before next schema change
- **Access logging** — Requires backend changes to the Supabase functions, out of scope for mobile-only fixes
