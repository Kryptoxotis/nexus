# My Nexus Card Index — Expandable Sub-Cards

## Context
When user taps their My Nexus card in ContactsScreen, instead of navigating to edit, expand to show an index of sub-cards:
- **First card**: "All Info" — activates the full business card for NFC (shares vCard with all data)
- **One card per non-empty social/field**: Instagram, X, Phone, etc. — each activatable for NFC and tappable to open locally

Each sub-card is a URL/link that can be:
1. Tapped to open the app/link on YOUR phone
2. Activated for NFC transfer to another person

## Tasks

- [ ] 1. Redesign My Nexus card in ContactsScreen — tap to expand/collapse sub-card index
- [ ] 2. Add "All Info" sub-card (first item) — activates full business card for NFC
- [ ] 3. Add individual sub-cards for each non-empty field — with brand icon + brand color
  - Parse BusinessCardData, generate a list of (icon, label, url/value) for non-empty fields
  - Phone → `tel:` intent, Email → `mailto:`, WhatsApp → `https://wa.me/`, rest → URL
- [ ] 4. Tap sub-card → open link/app locally (ACTION_VIEW intent)
- [ ] 5. NFC icon on each sub-card → activate that specific URL for NFC transfer
- [ ] 6. Add edit button so user can still get to EditCardScreen

## Files
- `ContactsScreen.kt` — main changes (expand/collapse, sub-card list, intents, NFC activation)
- `PersonalCardViewModel.kt` — may need a method to activate a card with custom content override
