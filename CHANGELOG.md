# Changelog

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
