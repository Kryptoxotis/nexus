# Merge Business Request + Org, Org Browsing, NFC Auto-Enroll

## Part 1: Merge business request + org creation
- [x] 1. AccountSwitcherScreen.kt — Add `description` and `enrollmentMode` fields to business request dialog; encode as JSON in `message`
- [x] 2. AdminViewModel.kt — Change `approveRequest` to accept full `BusinessRequestDto`; after upgrading account, also create org
- [x] 3. BusinessRequestsScreen.kt — Parse message JSON to show org details; pass full request to onApprove
- [x] 4. BusinessDashboardScreen.kt — Remove "Create Organization" button, show info message instead
- [x] 5. MainActivity.kt — Remove `onNavigateToCreateOrg` from BusinessDashboardScreen call

## Part 2: Org browsing with search
- [x] 6. EnrollmentScreen.kt — Add search bar to filter organizations by name/type

## Part 3: NFC auto-enroll
- [x] 7. PersonalCard.kt — Add `organizationId` field to BusinessCardData with vCard/JSON support
- [x] 8. ScanCardScreen.kt — Parse org ID from vCard, show "Join" button
- [x] 9. AddCardScreen.kt + MainActivity.kt — Wire `organizationId` into business card creation

## Review

All 9 changes completed. Build successful.

### Summary of changes

**AccountSwitcherScreen.kt** — Added `description` and `enrollmentMode` fields to the business request dialog. Description replaces the old message field for describing the business. Enrollment mode uses filter chips (Open/PIN/Closed). All org details are JSON-encoded into the `message` column: `{"userMessage":"...","description":"...","enrollmentMode":"open"}`.

**AdminViewModel.kt** — Changed `approveRequest(requestId, userId)` to `approveRequest(request: BusinessRequestDto)`. After upgrading the user to business, it now also inserts into `organizations` table using the request's `businessName`, `businessType`, and parsed JSON fields (description, enrollmentMode). Falls back to defaults for old-style plain text messages.

**BusinessRequestsScreen.kt** — Updated `onApprove` to pass full `request` object. Updated RequestCard to parse the JSON message and display description and enrollment mode separately, with fallback to plain text for old requests.

**BusinessDashboardScreen.kt** — Removed `onNavigateToCreateOrg` parameter. Changed "No Organization Yet" block from a create prompt to an informational message saying "Organization not found" with instructions to contact admin.

**MainActivity.kt** — Removed `onNavigateToCreateOrg` from BusinessDashboardScreen call. Passed `businessViewModel` to ScanCardScreen. Passed `businessViewModel.myOrganization?.id` as `organizationId` to AddCardScreen.

**EnrollmentScreen.kt** — Added a search bar (`OutlinedTextField` with search icon) above the org list. Organizations filter client-side by name or type matching the query. Empty states distinguish "no results" vs "no orgs available".

**PersonalCard.kt** — Added `organizationId: String = ""` to BusinessCardData. `toVCard()` emits `X-NEXUS-ORG:{id}`. `toJson()`/`fromJson()` serialize/deserialize the field.

**ScanCardScreen.kt** — Added `businessViewModel` parameter. Added `organizationId` to `ParsedVCard`. `parseVCard()` parses `X-NEXUS-ORG:` lines. After scan, if org ID present, shows "Join {company}" button that calls `businessViewModel.enrollInOrganization()`.

**AddCardScreen.kt** — Added optional `organizationId` parameter. When saving a BUSINESS_CARD, injects the org ID into `BusinessCardData` so it appears in the vCard via NFC.
