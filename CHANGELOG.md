# Changelog

## [1.6.6](https://github.com/JoestarLabs/NativeAlpha/compare/v1.6.5...v1.6.6) (2026-11-06)


### Bug Fixes

* **deps:** update dependency androidx.compose.material3:material3 to v1.5.0-rc01 ([718a1b8](https://github.com/JoestarLabs/NativeAlpha/commit/718a1b8ad052a26553881f461019771e92e3739c))

## [1.6.5](https://github.com/JoestarLabs/NativeAlpha/compare/v1.6.4...v1.6.5) (2026-11-04)


### Features

* add built-in native PDF viewer option (issue [#61](https://github.com/JoestarLabs/NativeAlpha/issues/61)) ([6d7d033](https://github.com/JoestarLabs/NativeAlpha/commit/6d7d0334eed80f2e2dbd6e8b3689d7e399184d95))
* **ui:** add Material You monochrome icons and unify Android 12+ splash screen ([cda11f6](https://github.com/JoestarLabs/NativeAlpha/commit/cda11f6cbdb0f61314a928a30dc2da7c2fe8dd6c))
* **ui:** add multiple configurable layout options for the web app (press and hold) menu ([0880430](https://github.com/JoestarLabs/NativeAlpha/commit/0880430e679d817082d3d5ae7eb6941b4cd65606))
* **ui:** modernize release notes with M3 Expressive sheet and SoC Gradle task ([84eaec1](https://github.com/JoestarLabs/NativeAlpha/commit/84eaec1db843d5a67d3d93ef0f340a00f11d3303))


### Bug Fixes

* address review findings for PDF viewer, process isolation, and lint ([3da0fcb](https://github.com/JoestarLabs/NativeAlpha/commit/3da0fcb3f957eb3f4194b0778cb5acad47acc1bf))
* close renderer asynchronously on worker thread and strengthen PDF test verifications ([ee8feba](https://github.com/JoestarLabs/NativeAlpha/commit/ee8feba904e3f5c8462445807849c4ddd8ffd41e))
* **data:** persist HTTP override, title, and start URL updates to stored WebApp entry ([89773f7](https://github.com/JoestarLabs/NativeAlpha/commit/89773f705671eb5ebedf1e497d42f8b892ee6b8f))
* **deps:** update androidx dependencies ([d558373](https://github.com/JoestarLabs/NativeAlpha/commit/d5583736422c58f4067611d18b28d00919b21cfb))
* **deps:** update androidx dependencies ([644c0aa](https://github.com/JoestarLabs/NativeAlpha/commit/644c0aa51b1e45f5c460cd12ef2bff3869ac88a9))
* **deps:** update dependency com.google.code.gson:gson to v2.15.0 ([f047097](https://github.com/JoestarLabs/NativeAlpha/commit/f0470978a2c9a219647b042f9d7c197b1c3d3a17))
* **deps:** update dependency com.google.code.gson:gson to v2.15.0 ([17b32a4](https://github.com/JoestarLabs/NativeAlpha/commit/17b32a4ec0659ff876f56fc382df80e6a2ac2f5f))
* **deps:** update dependency org.jsoup:jsoup to v1.24.1 ([eb3d8a4](https://github.com/JoestarLabs/NativeAlpha/commit/eb3d8a422f683f18b74220cfac267b2f5f728937))
* enforce URL scheme check, cookie same-origin policy, and deserializer null handling ([fcb2416](https://github.com/JoestarLabs/NativeAlpha/commit/fcb2416dcdfaacefa4fdc5d87a2ad89b5dd55a8c))
* guard PDF page rendering against oversized bitmap allocation ([c4ae899](https://github.com/JoestarLabs/NativeAlpha/commit/c4ae899dffc21696f7c9f19d2a0ddaf7e2f32e28))
* resolve back navigation multiple-swipe regression and history loop ([9519a23](https://github.com/JoestarLabs/NativeAlpha/commit/9519a2369b03999957f3dcd41df04463f6bd32d0))
* resolve review findings for data isolation, lifecycle, localization, and tests ([06d6146](https://github.com/JoestarLabs/NativeAlpha/commit/06d6146faaf60a08d4b9c83ba6aeaeb3a49ff063))
* **resources:** restore mipmap-anydpi-v26 resource qualifier for adaptive icons ([a6fecef](https://github.com/JoestarLabs/NativeAlpha/commit/a6fecef9cfbf48f2cd01a5a84893f9c1ce9f7c72))
* sanitize adblock filter download state to prevent startup deadlock and restore adblocking ([3ac9080](https://github.com/JoestarLabs/NativeAlpha/commit/3ac9080750a83182c051f147af6bae5297dd0214))
* sanitize URL scheme matching, restrict PDF launch schemes, and omit cross-origin cookies ([8f27912](https://github.com/JoestarLabs/NativeAlpha/commit/8f279120ce563b5a03d0ab78d70bd49ca8606d88))
* scale down PDF render dimensions to fit bitmap budget while preserving aspect ratio ([1d8968c](https://github.com/JoestarLabs/NativeAlpha/commit/1d8968cd153dd427fa6cde14648830552ab69d53))
* serialize PdfRendererHelper lifecycle and restrict PDF navigation to GET ([6c40202](https://github.com/JoestarLabs/NativeAlpha/commit/6c40202402d8c5ca7dab8617662727b61f561e6d))
* **shortcuts:** offload pinned shortcut cleanup to IO dispatcher to resolve ThreadConstraint lint errors ([263e468](https://github.com/JoestarLabs/NativeAlpha/commit/263e468da991f030c082183e7ee21d0dda422933))
* **ui:** ensure ExpressiveSquigglyDivider bounds are symmetric and prevent horizontal overshoot ([d03dd1f](https://github.com/JoestarLabs/NativeAlpha/commit/d03dd1f9351dc222d658f12f1009d676bb39a215))
* **ui:** pin action bar and make release notes content scrollable with sticky footer ([0a11e0a](https://github.com/JoestarLabs/NativeAlpha/commit/0a11e0a197b54ae4d2fc25397dcb82a226892d98))

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
