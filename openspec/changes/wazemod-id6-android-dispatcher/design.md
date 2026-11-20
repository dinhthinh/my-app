# Design

## Context

See `proposal.md` for motivation. The `my-app` workspace currently holds a placeholder Create React App (`package.json`, `src/App.js`). Meanwhile, `n20_f34` (`id6-assistant`) now provides a generated Kotlin wire codec in `sdk/android/Id6ProtocolCodec.kt` (`package com.id6assistant.sdk`) and unit tests in `sdk/android/Id6ProtocolCodecTest.kt`, and WazeMod V14+ exposes live telemetry on Android via AIDL (`IWazeModData` / `IWazeModListener`). On this macOS dev machine, `clang++` and `python3` are present for `n20_f34`'s host simulator (`id6-assistant-host` + `tools/live_web_viewer.py`), while JDK 17 and Android SDK/CLI tools still need to be installed before building/running the Android app.

## Goals / Non-Goals

**Goals:**
- Build a standalone native Android (Kotlin + Jetpack Compose) project in `my-app` using Gradle Kotlin DSL.
- Reuse `Id6ProtocolCodec.kt` verbatim from `n20_f34/sdk/android/` so wire framing stays bit-exact with `include/id6_protocol_codec.h`.
- Provide a resilient `ForegroundService` that binds WazeMod AIDL (or a Mock Source for bench testing) and dispatches `0x01` SLI and `0x04` NAV frames to TCP `:9090`.
- Support the full WazeMod V14+ `alerts[].type` catalog (`1..75`) and `avg_zone` fields on the mobile HUD while mapping `type` `8`, `9`, `10`, `22`, `25` to `SliInput`.

**Non-Goals:**
- Modifying `n20_f34` C++ daemon or adding a new `0x05` wire packet type in `id6_protocol_codec.h`.
- Supporting iOS (WazeMod V14+ AIDL is Android-only).
- Implementing ADAS `0x02` LANE sensing (WazeMod does not emit forward-vehicle distance or lane-departure warnings; `0x02` remains optional/manual in mock controls if desired).

## Decisions

### 1. Native Android (Kotlin + Jetpack Compose) over React Native
- **Choice**: Pure Android Kotlin + Jetpack Compose.
- **Rationale**: WazeMod's `IWazeModData` API is Android AIDL, and `n20_f34` now ships `Id6ProtocolCodec.kt` (`package com.id6assistant.sdk`). Pure Kotlin eliminates the JS bridge, `react-native-tcp-socket` native linking issues, and simplifies `ForegroundService` + `ServiceConnection` lifecycle management.
- **Alternative considered**: React Native using `sdk/react-native/id6_protocol_codec.ts` — rejected because it still requires writing an Android Kotlin Native Module for AIDL while adding Metro/RN build complexity.

### 2. AIDL Stub Strategy (`IWazeModData.aidl` + `IWazeModListener.aidl`)
- **Choice**: Define the WazeMod V14+ AIDL interfaces (`IWazeModData`, `IWazeModListener`) in `app/src/main/aidl/` matching WazeMod's documented contract (`apiVersion`, `requestAccessIntent()`, `start(listener)`, `stop()`, `onUpdate(Bundle)`, `onRevoked()`), and declare `<queries>` for WazeMod package discovery on Android 11+.
- **Rationale**: Standard Android Binder IPC without external AAR dependencies; works cleanly against WazeMod V14+ once installed on a physical Android device.

### 3. Short-lived TCP `:9090` Dispatch with Foreground Service
- **Choice**: Run telemetry binding and TCP dispatch inside a `ForegroundService` (`connectedDevice` / `dataSync` type) using a single-threaded/confined coroutine dispatcher (`Dispatchers.IO`), opening a short-lived `Socket` per frame with `800ms` connect timeout as specified in `n20_f34/docs/protocol_spec.md`.
- **Rationale**: Matches `id6-assistant`'s non-blocking `accept -> read -> close` model, survives backgrounding/screen-off while driving, and prevents socket-pool stalls on Wi-Fi CarPlay (`uap0` `172.16.224.1:9090`).

### 4. WazeMod `alerts[]` & `turn` Mapping Rules
- **Choice**:
  - `turn` (`0..20`) maps 1:1 to `Id6Bits.ID6_NAV_MANEUVER_*` (with `roundaboutAngleDeg` `-90`, `90`, `180`, `360` for turns `11`, `12`, `19`, `20`) per `n20_f34/docs/mobile_nav_sdk.md`.
  - `alerts[]` maps `type = 9` -> `noPassing = true`; `type = 10` or `25` -> `noPassing = false`; `type = 8` (`value`) -> fallback `limitKmh` when `speed_limit == 0`; `type = 22` -> clear fallback.
  - All `1..75` alert types are mapped in an exhaustive Kotlin lookup table (`WazeAlertCatalog`) with Vietnamese labels, severity/category grouping, and a generic fallback for unknown codes > 75.

## Risks / Trade-offs

- **WazeMod AIDL package/action name on device** → Exact `<queries>` package names or service intent action may vary across WazeMod V14+ builds; mitigate by querying `PackageManager.queryIntentServices` with configurable action/package overrides in Settings and providing Mock Mode for bench verification.
- **No Java/Android SDK currently installed on macOS host** → Android Gradle build/emulator cannot run until JDK 17 + Android SDK (`platform-tools`, `platforms;android-35`, `build-tools`) are installed; mitigate by including explicit environment installation & verification tasks first in `tasks.md`.
- **Emulator-to-Host networking** → Android Emulator reaches macOS `id6-assistant-host` at `10.0.2.2:9090` (not `127.0.0.1`); mitigate by providing `10.0.2.2:9090` as a built-in endpoint preset in the UI.
