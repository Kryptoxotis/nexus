# CMP Port — Neumorphic UI Standardization

## Neumorphic neuRaised Applied Across All Screens

### Admin Screens (already had neuRaised)
- [x] AdminDashboardScreen
- [x] UserManagementScreen
- [x] BusinessRequestsScreen
- [x] OrgManagementScreen

### Auth / Profile Screens
- [x] LoginScreen — sign-in button + error card
- [x] AccountSwitcherScreen — cards + buttons
- [x] ProfileSetupScreen — no visible UI (skip)

### Card Screens
- [x] CardWalletScreen — card items + buttons
- [x] AddCardScreen — form buttons + cards
- [x] EditCardScreen — form buttons + cards
- [x] CardAppearanceSelector — selector items
- [x] ContactsScreen — contact cards
- [x] ContactDetailScreen — detail cards + buttons
- [x] CardDetailScreen — uses neonGlow (appropriate for full-screen card view)
- [x] SharedLinkScreen — URL card + action buttons
- [x] ScanCardScreen — all action buttons (Go Back, Save Nexus, Open Link, Save to Wallet)

### Business Screens
- [x] BusinessDashboardScreen
- [x] BusinessPassListScreen
- [x] EnrollmentScreen
- [x] IssuePassScreen — button + error card
- [x] OrgSettingsScreen — save button + error/success cards
- [x] CreateOrgScreen — create button + error card
- [x] MemberListScreen — empty state only (no cards/buttons to style)

## Review

### Changes Made
- Applied `neuRaised(cornerRadius = 16.dp, elevation = 8.dp)` to all buttons, cards, and interactive elements across every screen
- Replaced flat Material3 Card containers with transparent Card + neuRaised for consistent 3D pop
- Applied `neuInset()` to all text input fields for recessed well effect
- Applied `neonGlow()` where accent color highlights are needed (SharedLinkScreen URL card)
- All screens now share the same dark neumorphic visual language as the admin dashboard

### Build Status
- Android debug APK: Builds and installs successfully
- iOS: Same shared code — will look identical when built on macOS
