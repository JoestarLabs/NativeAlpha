# Changelog

## [1.6.5](https://github.com/JoestarLabs/NativeAlpha/compare/v1.6.4...v1.6.5) (2026-10-28)


### Bug Fixes

* **deps:** update androidx dependencies ([d558373](https://github.com/JoestarLabs/NativeAlpha/commit/d5583736422c58f4067611d18b28d00919b21cfb))
* **deps:** update androidx dependencies ([644c0aa](https://github.com/JoestarLabs/NativeAlpha/commit/644c0aa51b1e45f5c460cd12ef2bff3869ac88a9))
* **deps:** update dependency com.google.code.gson:gson to v2.15.0 ([f047097](https://github.com/JoestarLabs/NativeAlpha/commit/f0470978a2c9a219647b042f9d7c197b1c3d3a17))
* **deps:** update dependency com.google.code.gson:gson to v2.15.0 ([17b32a4](https://github.com/JoestarLabs/NativeAlpha/commit/17b32a4ec0659ff876f56fc382df80e6a2ac2f5f))

## [1.6.4](https://github.com/JoestarLabs/NativeAlpha/compare/v1.6.3...v1.6.4) (2026-10-13)


### ⚠ BREAKING CHANGES

* Transition versionCode calculation from Unix timestamp to version-derived integer. In-place updates from GitHub v1.6.3 APK will require backup, uninstall, and restore.

### Features

* modernize release metadata, dynamic versionCode calculation, and backup test suite ([afa1387](https://github.com/JoestarLabs/NativeAlpha/commit/afa1387f2eaa76a42bbc896d259964a63914c4a4))


### Bug Fixes

* resolve sandbox NPE crash and enable flavor feature parity ([9358d31](https://github.com/JoestarLabs/NativeAlpha/commit/9358d311fc7e9db851c4e0a809461616f625c1df))
* resolve sandbox NPE crash and enable flavor feature parity ([188300c](https://github.com/JoestarLabs/NativeAlpha/commit/188300ce513f9ac58e3be8c67cfb025a67a6cdf7)), closes [#47](https://github.com/JoestarLabs/NativeAlpha/issues/47)

## [1.6.3](https://github.com/JoestarLabs/NativeAlpha/compare/NativeAlpha-v1.6.2...NativeAlpha-v1.6.3) (2026-10-09)


### Bug Fixes

* **deps:** update dependency androidx.compose.material3:material3 to v1.5.0-beta01 ([5894f07](https://github.com/JoestarLabs/NativeAlpha/commit/5894f07e1cf9913b571a59d46632318f0d8d1546))
* **deps:** update dependency androidx.compose.material3:material3 to v1.5.0-beta01 ([cbad7f9](https://github.com/JoestarLabs/NativeAlpha/commit/cbad7f926b3dae28649f011c4eda034b543301fa))
* resolve favicon loading for websites with standard dimensions on shortcut creation ([7e23424](https://github.com/JoestarLabs/NativeAlpha/commit/7e23424bb6559f002c8a2f2175da6c8da077fffe))
* resolve favicon loading for websites with standard dimensions on… ([b0af677](https://github.com/JoestarLabs/NativeAlpha/commit/b0af6771823ac671d51be4186778f99f6cda1e21))

## [1.6.2](https://github.com/JoestarLabs/NativeAlpha/compare/NativeAlpha-v1.6.1...NativeAlpha-v1.6.2) (2026-10-05)


### Bug Fixes

* fix adblock crash, improve adblock initialization safety and add robust unit tests ([4bac466](https://github.com/JoestarLabs/NativeAlpha/commit/4bac46634b8bbbcafdb1391cef24b4dd55bbf865))
* fix adblock crash, improve adblock initialization safety and add… ([d829054](https://github.com/JoestarLabs/NativeAlpha/commit/d829054016edfda9cfb2035a7efb8590015289bd))

## [1.6.1](https://github.com/JoestarLabs/NativeAlpha/compare/NativeAlpha-v1.6.0...NativeAlpha-v1.6.1) (2026-10-03)


### Bug Fixes

* enable R8 minification and resource shrinking for release builds ([9a271d1](https://github.com/JoestarLabs/NativeAlpha/commit/9a271d13f789c47bd6f46ed9e52ad93bd439b340))

## [1.6.0](https://github.com/JoestarLabs/NativeAlpha/compare/NativeAlpha-v1.5.2...NativeAlpha-v1.6.0) (2026-10-03)


### Features

* **about:** clarify fork authorship and extract dependencies screen to activity ([350233a](https://github.com/JoestarLabs/NativeAlpha/commit/350233a667e6b5beec6b54ab0bc6d7b53294d24b))
* **about:** modernize about screen with Jetpack Compose and Material 3 Expressive ([ec4059c](https://github.com/JoestarLabs/NativeAlpha/commit/ec4059c5b5d7c373759518f9ebe7bc0244b23091))
* **about:** modernize about screen with Jetpack Compose and Material 3 Expressive ([8516e72](https://github.com/JoestarLabs/NativeAlpha/commit/8516e729b889467fc55da63c1253911ebb274940))
* add app icon to WebAppListScreen top app bar title ([c9d3ac0](https://github.com/JoestarLabs/NativeAlpha/commit/c9d3ac0a418021d7beeb1649ebda00412cbe91ea))
* modernize main screen web app list with Material 3 Expressive and Compose ([7ce932d](https://github.com/JoestarLabs/NativeAlpha/commit/7ce932dbb2078559bdf8df4cd1c579d782f596cc))
* modernize main screen web app list with Material 3 Expressive and Compose ([c3ae703](https://github.com/JoestarLabs/NativeAlpha/commit/c3ae7031bda78709a80473d9722305950238ffa8))
* modernize ShortcutDialogFragment with Compose M3 and update docs ([eb2e086](https://github.com/JoestarLabs/NativeAlpha/commit/eb2e086030c39779ccf18fa496b48664ecd67bc4))
* **ui:** add Material You dynamic theme toggle in settings ([d4a42bb](https://github.com/JoestarLabs/NativeAlpha/commit/d4a42bb02685c4c8fc39a7a0203fe798abc7e9e5))
* **ui:** modernize Adblock & WebApp settings in Compose with predictive back gesture ([b3766b4](https://github.com/JoestarLabs/NativeAlpha/commit/b3766b417fd60422470ff0f6fc646a1233c776e9))
* **ui:** redesign settings screen with Material 3 Expressive and Jetpack Compose ([da925d5](https://github.com/JoestarLabs/NativeAlpha/commit/da925d5ea115cbdc56f2b513f452d05c3103efc6))
* **webapp:** add task title/icon in recents and robust icon fallback ([01e8054](https://github.com/JoestarLabs/NativeAlpha/commit/01e8054842f73e9859278f5f14a584a417550a01))


### Bug Fixes

* **adblock:** support 16 KB page sizes and eliminate warning suppressions ([5e4516a](https://github.com/JoestarLabs/NativeAlpha/commit/5e4516a56897a1bf7c42d769577ed3181b8dfcc3))
* **ci:** auto-bump app/build.gradle version with release-please ([2e12915](https://github.com/JoestarLabs/NativeAlpha/commit/2e12915bdb4081188bf460b43b583512d1031fc2))
* **ci:** specify target-branch main for release-please workflow ([f8ebb68](https://github.com/JoestarLabs/NativeAlpha/commit/f8ebb687523535edcd3a7383917c9471cf9e1078))
* **deps:** update androidx dependencies ([6f49ba1](https://github.com/JoestarLabs/NativeAlpha/commit/6f49ba1b24e9e6c94ed3120d8f379c3e918c8d74))
* **deps:** update androidx dependencies ([f971513](https://github.com/JoestarLabs/NativeAlpha/commit/f971513aadef4037e266e375423467b097873959))
* **i18n:** add missing translations for About screen strings in de/es/it ([1cff7b3](https://github.com/JoestarLabs/NativeAlpha/commit/1cff7b3eb07922ec9f258f2e389e2c4b1b208044))
* **i18n:** replace hardcoded strings with localized string resources ([56b90e6](https://github.com/JoestarLabs/NativeAlpha/commit/56b90e6217c188af778c54747abcc483a4002d78))
* **lint:** resolve LocalContextGetResourceValueCall Compose lint errors ([bd40e8b](https://github.com/JoestarLabs/NativeAlpha/commit/bd40e8b1a960dbf30b30ecf8e81c7217e649db17))
* **lint:** use stringResource in ShortcutDialogContent to resolve LocalContextGetResourceValueCall ([7e3e3e0](https://github.com/JoestarLabs/NativeAlpha/commit/7e3e3e09e95d88dd1f36a21b1c9090e81a1ee154))
* **recents:** ensure dynamic task icon compatibility across all Android versions ([1f5a8ca](https://github.com/JoestarLabs/NativeAlpha/commit/1f5a8ca23eb96420bda27c68cd84c76c6fdcb2a1))
* **recents:** log task description errors and refresh label on onResume ([28b7a32](https://github.com/JoestarLabs/NativeAlpha/commit/28b7a324739999a6c523cf63a8d97d0611a3813b))
* **recents:** use TaskDescription.Builder.setLabel() on API 33+ for correct web app title in Recent Apps ([f262740](https://github.com/JoestarLabs/NativeAlpha/commit/f262740783edacd0938452588af75bee11ae3561))
* resolve predictive back transition, list reordering jitter, and reactive icon updates ([ab0e859](https://github.com/JoestarLabs/NativeAlpha/commit/ab0e8597a0c77be7276fb201e9907da84a018a7f))
* **ui:** fix toggle state updates and recomposition across settings screens ([55d3bba](https://github.com/JoestarLabs/NativeAlpha/commit/55d3bba5aae7da8c6b764646d2a31257ac2380d8))
* **ui:** prevent subtitle cut-off and restore all webapp settings toggles ([4252ddb](https://github.com/JoestarLabs/NativeAlpha/commit/4252ddb3a89df10f8bc6146b2a9ed85ec8c77b07))
