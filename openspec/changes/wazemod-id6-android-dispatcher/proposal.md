# Proposal

## Why

The `n20_f34` (`id6-assistant`) daemon on BMW NBT Evo ID6 receives real-time Speed Limit Info (`0x01`) and Turn-by-Turn Navigation (`0x04`) over TCP port `9090`, and now ships an official Android Kotlin SDK (`sdk/android/Id6ProtocolCodec.kt`). Because WazeMod V14+ exposes live navigation, speed limits, average-speed zones, and `alerts[]` (`type` 1..75) exclusively via an Android AIDL service (`IWazeModData`), we need a native Android Kotlin companion app in this repository to bridge WazeMod telemetry to `n20_f34` while also supporting bench simulation against `id6-assistant-host`.

## What Changes

- **BREAKING**: Replace the existing placeholder Create React App (`package.json`, `src/`, `build/`) with a native Android (Kotlin + Jetpack Compose + Gradle) project.
- Integrate `Id6ProtocolCodec.kt` (`com.id6assistant.sdk`) and `Id6ProtocolCodecTest.kt` from `n20_f34/sdk/android/` as the wire protocol encoder.
- Implement a WazeMod V14+ AIDL client (`IWazeModData` / `IWazeModListener`) with access-request handling, lifecycle recovery, and a built-in mock telemetry generator for bench/simulator testing.
- Implement a `WazeModToId6Mapper` and TCP `:9090` foreground-service dispatcher with deduplication, 5 Hz (`200ms`) throttling, and automatic NAV clear on stop/revocation.
- Build a Jetpack Compose companion HUD/dashboard showing live speed, speed limits, WazeMod `alerts[].type` (1..75) with distance/jam details, average-speed zone (`avg_zone`) progress, turn/lane preview, target IP/port configuration, and real-time packet logs.

## Capabilities

### New Capabilities
- `wazemod-aidl-client`: Android AIDL binding to WazeMod V14+ (`IWazeModData`), permission flow (`requestAccessIntent`), lifecycle/revocation handling, and a switchable mock snapshot source for simulator testing.
- `id6-telemetry-dispatcher`: Canonical mapping from WazeMod snapshots (including `alerts[]` types 8, 9, 10, 22, 25 and turn/lane data) to `Id6ProtocolCodec` binary frames (`0x01` SLI, `0x04` NAV), plus TCP `:9090` delivery via Foreground Service with deduplication and 5 Hz throttling.
- `companion-hud-ui`: Jetpack Compose mobile HUD and diagnostics UI displaying WazeMod telemetry, all `alerts[].type` (1..75), average-speed zone status, target head-unit/simulator endpoint settings, and live hex packet logs.

### Modified Capabilities
<!-- None: this is a greenfield Android replacement of the placeholder web app. -->

## Impact

- **Repository (`my-app`)**: Replaces React web files with an Android Gradle project (`app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`).
- **External Dependency (`n20_f34`)**: Consumes `sdk/android/Id6ProtocolCodec.kt` and `sdk/android/Id6ProtocolCodecTest.kt` (no changes required to `n20_f34` C++ daemon).
- **External API (WazeMod V14+)**: Binds to WazeMod Android AIDL (`IWazeModData` / `IWazeModListener`) and declares `<queries>` in `AndroidManifest.xml`.
- **Developer Environment**: Requires JDK 17, Android SDK (Platform 34/35, Build-Tools, Platform-Tools `adb`), and Gradle wrapper; macOS `n20_f34` host simulator (`id6-assistant-host` + `tools/live_web_viewer.py`) already has `clang++` and `python3` available.
