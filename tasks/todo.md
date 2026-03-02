# Code Review Round 6 — 12-Specialist Deep Review

## CRITICAL
- [x] 1. AdminRepository.kt — Fix `rejectRequest` race condition (add select + row verify)
- [x] 2. Add AdminRepository validation unit tests (via AdminViewModelTest reload tests)

## HIGH
- [x] 3. UrlUtils.kt — Fix invisible char bypass (strip from entire string, not just leading)
- [x] 4. UrlUtils.kt — Fix `ensureHttps` normalization consistency
- [x] 5. NfcReader.kt — Sanitize plain text NFC records (strip control chars)
- [x] 6. NfcReader.kt — Expand URI body control char stripping
- [x] 7. AdminRepository.kt — Normalize email in `createUser`
- [x] 8. AdminViewModel.kt — Add loading state to all `load*()` functions
- [x] 9. AdminViewModel.kt — Fix auto-dismiss timing (longer for errors)
- [x] 10. CardColors.kt — Fix Forest Green for WCAG AA contrast (#388E3C -> #2E7D32)
- [x] 11. CardPreview.kt — Fix subtitle alpha for WCAG contrast (0.75/0.85 -> 0.87)

## MEDIUM
- [x] 12. AdminViewModel.kt — Remove unused TAG constant
- [x] 13. AdminRepository.kt — Remove unused `truncateForDisplay`
- [x] 14. NeuModifiers.kt — Fix `neuInset` fallback to draw background instead of invisible
- [x] 15. CardAppearanceSelector.kt — Fix accessibility (adaptive border/check colors, remove double-announce)
- [x] 16. Color.kt — Brighten NexusTextSecondary for WCAG contrast (#94A3B8 -> #A0AEC0)
- [x] 17. android-build.yml — Fix secrets file (> not >>), add cleanup step, add timeouts, add wrapper validation
- [x] 18. proguard-rules.pro — Narrow keep rules (service.** -> specific classes, ktor narrowed)
- [x] 19. QUICKSTART.md — Add secrets.properties setup step

## TESTS
- [x] 20. UrlUtilsTest — Add fullwidth unicode normalization test
- [x] 21. UrlUtilsTest — Add mid-string invisible chars test
- [x] 22. NfcReaderTest — Add text record with javascript URI test
- [x] 23. NfcReaderTest — Add null byte stripping test
- [x] 24. NfcReaderTest — Add control char stripping test
- [x] 25. AdminViewModelTest — Add reload-after-mutation tests (approve, reject, createUser)
- [x] 26. AdminViewModelTest — Add loading state lifecycle test
- [x] 27. AdminViewModelTest — Add auto-dismiss timing test (6s errors vs 3s success)

## Review

**27/27 issues fixed across 12 source files + 3 test files.**

### Security fixes:
- **UrlUtils invisible char bypass** — Regex now strips invisible/control chars from entire string, not just leading. Prevents `j\u200Bavascript:` bypass.
- **UrlUtils ensureHttps** — Scheme comparison now uses same NFKC-normalized+cleaned value, preventing invisible chars from evading scheme detection.
- **NFC text record sanitization** — Plain text NFC records now strip control chars and enforce length limit.
- **NFC URI body** — Expanded from null-byte-only stripping to all control chars (0x00-0x1F, 0x7F).
- **Email normalization** — `createUser` now trims+lowercases email before validation and RPC call. Added 254-char length limit.
- **rejectRequest race condition** — Now verifies the update matched a row via `select()` + `decodeList`, matching the pattern in `approveRequest`. Returns specific error for concurrent admin race.

### Accessibility fixes:
- **CardAppearanceSelector** — Border and checkmark colors now adapt (black on light swatches, white on dark). Removed redundant selected/not-selected text from contentDescription (stateDescription handles it).
- **CardPreview subtitle** — Alpha increased from 0.75/0.85 to 0.87 for WCAG AA compliance.
- **Forest Green** — Darkened from #388E3C to #2E7D32 for ~4.8:1 white text contrast ratio.
- **NexusTextSecondary** — Brightened from #94A3B8 to #A0AEC0 for ~4.7:1 on NexusSurfaceVariant.

### Error handling fixes:
- **AdminViewModel load functions** — All 4 load functions now set Loading state before the network call, with Idle on success.
- **Auto-dismiss** — Error messages now persist for 6 seconds (was 3s, same as success). Success messages still auto-dismiss at 3s.
- **neuInset fallback** — On bitmap allocation failure, now draws NexusDeep background instead of rendering invisible.

### Code quality:
- **Removed dead code** — `truncateForDisplay()` in AdminRepository, `TAG` constant in AdminViewModel.

### DevOps:
- **CI secrets** — First line uses `>` (overwrite) instead of `>>` (append). Added `rm -f secrets.properties` cleanup step with `if: always()`.
- **CI timeouts** — Build job: 20 min, Release job: 5 min.
- **Gradle wrapper validation** — Added `gradle/actions/wrapper-validation@v4` step.
- **Release labeling** — Marked as `prerelease: true` for debug builds.
- **ProGuard** — Narrowed `service.**` to specific classes (NFCPassService, NfcReader). Narrowed ktor from `io.ktor.client.**` to engine/call/plugins.

### Documentation:
- **QUICKSTART.md** — Added Step 2 (Configure Secrets) with instructions to create `secrets.properties` and get values from Supabase dashboard.

### New tests (10 test cases):
- `UrlUtilsTest`: fullwidth NFKC normalization, mid-string invisible chars, ensureHttps with invisible chars
- `NfcReaderTest`: text record with javascript URI, null byte stripping, control char stripping
- `AdminViewModelTest`: approve/reject/createUser reload-after-mutation, loading state lifecycle, error auto-dismiss timing (6s)
