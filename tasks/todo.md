# Version Display, NFC Fix, and Card Image Upload

## Feature 1: Show App Version
- [x] Add version text to AccountSwitcherScreen.kt
- [x] Update build.gradle.kts versionName to use Gradle property for CI
- [x] Update android-build.yml to inject build number

## Feature 2: Fix NFC Reader Mode (HCE-Only)
- [x] Add enableReaderMode/disableReaderMode to MainActivity.kt

## Feature 3: Image Upload for Cards
- [x] Add `imageUrl` field to PersonalCard domain model
- [x] Add `imageUrl` field to PersonalCardEntity (Room entity)
- [x] Add `image_url` field to PersonalCardDto
- [x] Bump database version 4 → 5 in NexusDatabase.kt
- [x] Update PersonalCardRepository: addCard(), mappers, sync methods
- [x] Update PersonalCardViewModel: addCard() with imageUrl param
- [x] Add Coil dependency to build.gradle.kts
- [x] Update AddCardScreen: image picker + preview + upload
- [x] Update CardWalletScreen: show card image in ActiveCardHero and CardItem
- [x] Update CardDetailScreen: show card image in header

## Review

### Summary of Changes

**11 files modified:**

| File | Changes |
|------|---------|
| `AccountSwitcherScreen.kt` | Added version text at bottom showing `BuildConfig.VERSION_NAME (VERSION_CODE)` |
| `build.gradle.kts` | `versionCode`/`versionName` now read from `-PbuildNumber` Gradle property; added Coil dependency |
| `android-build.yml` | Passes `github.run_number` as `-PbuildNumber` to Gradle |
| `MainActivity.kt` | Added `nfcAdapter` field, `onResume()` with `enableReaderMode()` (no-op callback), `onPause()` with `disableReaderMode()` |
| `PersonalCard.kt` | Added `val imageUrl: String? = null` |
| `PersonalCardEntity.kt` | Added `imageUrl` column + updated `toDomain()`/`fromDomain()` mappers |
| `PersonalCardDto.kt` | Added `@SerialName("image_url") val imageUrl: String? = null` |
| `NexusDatabase.kt` | Bumped version 4 → 5 (destructive migration) |
| `PersonalCardRepository.kt` | `addCard()` accepts `imageUrl`, all push/sync methods include `imageUrl` |
| `PersonalCardViewModel.kt` | `addCard()` accepts and forwards `imageUrl` |
| `AddCardScreen.kt` | Image picker + preview for all card types, uploads image before creating card |
| `CardWalletScreen.kt` | `ActiveCardHero` shows image background with scrim; `CardItem` shows thumbnail |
| `CardDetailScreen.kt` | Header shows image background with scrim |

### Notes
- Supabase `personal_cards` table needs `image_url TEXT` column added manually
- Existing cards without images work normally (all `imageUrl` fields default to `null`)
- Local DB uses destructive migration — existing local data will be wiped on update (acceptable for current dev stage)
