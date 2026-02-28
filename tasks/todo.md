# Received Business Cards (Contacts Collection)

## Tasks

### Backend
- [x] 1. Supabase migration: create `received_cards` table with RLS policies
- [x] 2. Room entity: `ReceivedCardEntity`
- [x] 3. Room DAO: `ReceivedCardDao`
- [x] 4. Update `NexusDatabase` — add entity + DAO, bump version 6 → 7
- [x] 5. DTO: `ReceivedCardDto` for Supabase serialization
- [x] 6. Repository: `ReceivedCardRepository` — save, observe, sync, delete

### Scan Flow
- [x] 7. Update `ScanCardScreen` — detect vCard, parse fields, show "Save Contact" button

### UI
- [x] 8. New `ContactsScreen` — list of received business cards
- [x] 9. New `ContactDetailScreen` — full detail view with tappable fields (phone/email/links)
- [x] 10. ViewModel: `ReceivedCardViewModel`

### Navigation
- [x] 11. `MainActivity` — wire up repository, viewmodel, routes, sync
- [x] 12. `CardWalletScreen` — "Contacts" (people icon) button in top bar

## Review

### New files
| File | Purpose |
|------|---------|
| `ReceivedCardEntity.kt` | Room entity for received_cards table |
| `ReceivedCardDao.kt` | Room DAO — insert, observe, delete |
| `ReceivedCardDto.kt` | Supabase serialization DTO |
| `ReceivedCardRepository.kt` | Save/sync/delete contacts with Room + Supabase |
| `ReceivedCardViewModel.kt` | Exposes contacts flow, save/delete methods |
| `ContactsScreen.kt` | List view of all received business cards |
| `ContactDetailScreen.kt` | Full detail view with tappable phone/email/links |

### Modified files
| File | What changed |
|------|-------------|
| `NexusDatabase.kt` | Added ReceivedCardEntity, ReceivedCardDao, version 7 |
| `ScanCardScreen.kt` | Detects vCard, shows parsed name/title, "Save Contact" button |
| `CardWalletScreen.kt` | Added Contacts button (people icon) in top bar |
| `MainActivity.kt` | Wired repository, viewmodel, routes, sync on login/resume |

### Supabase
- `received_cards` table with RLS (users can only read/insert/delete their own)
