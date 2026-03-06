# CMP Issues Fix Plan

## Issues to Fix

### 1. App Icon Missing
- [ ] Copy ic_launcher PNGs from `app/src/main/res/mipmap-*` to CMP's `composeApp/src/androidMain/res/mipmap-*`
- [ ] Add `android:icon` and `android:roundIcon` to CMP's AndroidManifest.xml
- [ ] For iOS: create AppIcon asset catalog in iosApp/

### 2. iOS Sign-In Crashes App
- [ ] iOS `PlatformAuthManager` calls `supabase.auth.signInWith(Google)` which opens a browser — but the app has no URL scheme configured to handle the OAuth callback
- [ ] Add Supabase OAuth redirect URL scheme to iOS Info.plist
- [ ] Handle the OAuth callback in the iOS app entry point

### 3. Neumorphic UI Not Consistent Everywhere
- [ ] Audit every screen's Scaffold/TopAppBar/background to use NexusBackground consistently
- [ ] Make ALL Scaffold backgrounds use NexusBackground (not default Material surface)
- [ ] Make ALL TopAppBar backgrounds use NexusBackground
- [ ] Ensure buttons, cards, and interactive elements across all screens use neuRaised
- [ ] The goal: match the reference image — dark background everywhere, raised 3D elements with highlights/shadows

## Priority Order
1. Fix #3 first (UI consistency) — biggest visual impact, shared code affects both platforms
2. Fix #1 (app icon) — quick win
3. Fix #2 (iOS sign-in) — needs testing on device
