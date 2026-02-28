# Restructure Contacts & Move Upgrade to Account

## Tasks

- [x] 1. CardWalletScreen.kt — Remove NFC icon + upgrade card
  - Remove `onNavigateToScanCard` parameter
  - Remove NFC icon button from top bar (3 icons remain: People, Badge, Account)
  - Remove "Upgrade to Business" card from LazyColumn
  - Remove `showBusinessDialog` state and AlertDialog
  - Keep pending business request banner

- [x] 2. ContactsScreen.kt — Restructure as contacts hub
  - Add `personalCardViewModel`, `onNavigateToScanCard`, `onNavigateToCreateMyCard` parameters
  - Collect user's cards, find BUSINESS_CARD ("My Card")
  - Add NFC FAB for scanning
  - LazyColumn: "My Card" section (show card or create prompt) + "Contacts" section

- [x] 3. AccountSwitcherScreen.kt — Add upgrade to business
  - Add `businessRequest` and `accountType` state from authViewModel
  - Add `showBusinessDialog` state
  - Add "Upgrade to Business" card between account list and "Add Account"
  - Add business request AlertDialog

- [x] 4. MainActivity.kt — Update navigation wiring
  - Remove `onNavigateToScanCard` from CardWalletScreen call
  - Pass `personalCardViewModel`, `onNavigateToScanCard`, `onNavigateToCreateMyCard` to ContactsScreen

## Review

All 4 changes completed. Build successful.

### Summary of changes

**CardWalletScreen.kt** — Removed the `onNavigateToScanCard` param, the NFC icon from the top bar (now 3 icons: People, Badge, Account), the "Upgrade to Business" card, and the business dialog. Kept the pending request banner. Cleaned up the unused `AccountType` import.

**ContactsScreen.kt** — Restructured as a contacts hub. Added params for `personalCardViewModel`, `onNavigateToScanCard`, `onNavigateToCreateMyCard`, and `onNavigateToEditCard`. Shows a "My Card" section at top — if a BUSINESS_CARD exists, shows it with name/subtitle/NFC icon (tappable to edit); if not, shows a "Create My Card" prompt that navigates to AddCardScreen. Below that, a "Contacts" section with the received cards list or empty state text. NFC FAB added for scanning.

**AccountSwitcherScreen.kt** — Added `accountType`, `businessRequest` state collection, and `showBusinessDialog` state. Inserted the "Upgrade to Business" card (identical UI from wallet) between the account list and "Add Account" button. Added the business request AlertDialog (identical dialog from wallet).

**MainActivity.kt** — Removed `onNavigateToScanCard` from CardWalletScreen call. Added `personalCardViewModel`, `onNavigateToScanCard`, `onNavigateToCreateMyCard`, and `onNavigateToEditCard` to ContactsScreen call.
