# Tasks

## 1. Mobile & Simulator Development Environment Setup

- [x] 1.1 Install JDK 17 (e.g., Temurin 17) and Android SDK command-line tools (`platform-tools`, `platforms;android-35`, `build-tools;35.0.0`, `emulator`, and an `arm64-v8a` Google APIs system image for Apple Silicon) and verify `java -version`, `adb --version`, and `emulator -list-avds` succeed
- [x] 1.2 Verify `n20_f34` host simulator runs cleanly by executing `make -C /Users/ngocttk/ProjectCode/n20_f34/tests check` and confirming `tools/live_web_viewer.py` starts without errors

## 2. Android Project Scaffolding & Protocol SDK Integration

- [x] 2.1 Remove placeholder React web files (`package.json`, `package-lock.json`, `src/`, `build/`) and scaffold a Gradle Kotlin DSL Android project (`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`) with Compose + Coroutines dependencies and verify `./gradlew tasks` succeeds
- [x] 2.2 Copy `Id6ProtocolCodec.kt` and `Id6ProtocolCodecTest.kt` from `n20_f34/sdk/android/` into `app/src/main/java/com/id6assistant/sdk/` and `app/src/test/java/com/id6assistant/sdk/` and verify `./gradlew testDebugUnitTest` passes

## 3. WazeMod V14+ AIDL Client & Mock Source

- [x] 3.1 Define `IWazeModData.aidl` and `IWazeModListener.aidl` under `app/src/main/aidl/`, configure `<queries>` and foreground service permissions in `AndroidManifest.xml`, and verify AIDL Java/Kotlin stubs compile via `./gradlew compileDebugKotlin`
- [x] 3.2 Implement `WazeModSnapshot` data models, `WazeModBundleParser` (parsing `speed`, `speed_limit`, `speed_min`, `overspeed`, `turn`, `then_turn`, `turn_distance_m`, `roundabout_exit`, `lanes`, `current_street`, `next_street`, `eta_*`, `avg_*`, and `alerts[]`), and unit tests verifying Bundle/payload parsing
- [x] 3.3 Implement `WazeModAidlClient` (`ServiceConnection`, `apiVersion >= 1` check, `requestAccessIntent()` recovery on `SecurityException`, `onRevoked()` / `DeadObjectException` cleanup) and `MockWazeModSource` (emitting configurable snapshots with `alerts[].type` 1..75 and `avg_zone`), verified with unit tests

## 4. Telemetry Mapper & TCP `:9090` Foreground Dispatcher

- [x] 4.1 Implement `WazeModToId6Mapper` mapping `turn` (`0..20`) to `Id6Bits.ID6_NAV_MANEUVER_*`, `lanes[]` bitmasks, street names, and `alerts[]` (`type` `8`, `9`, `10`, `22`, `25`) to `SliInput` / `NavInput`, and verify with unit tests covering all maneuver codes and alert edge cases
- [x] 4.2 Implement `Id6TcpDispatcher` (short-lived TCP `:9090` socket with `800ms` connect timeout, SLI deduplication, `0x04` 5 Hz / `200ms` throttle, immediate `active = false` clear on stop/revocation) and `Id6DispatchService` (`ForegroundService`), verified with unit tests against a local `ServerSocket`

## 5. Companion HUD UI & Alerts Catalog

- [x] 5.1 Implement `WazeAlertCatalog` covering all WazeMod `alerts[].type` codes `1..75` (Vietnamese labels, category, icon/badge, `jam_severity`/`jam_delay_min` for `type = 6`, `value` for `type = 8`) plus generic fallback for unknown codes, verified with unit tests across `1..75` and unknown codes
- [x] 5.2 Build Jetpack Compose screens (`DashboardScreen`, `SettingsSheet`, `PacketLogPanel`) displaying live speed/limit, up to 3 nearest alerts, `avg_zone` progress, turn/lane preview, endpoint presets (`172.16.224.1`, `172.16.222.1`, `10.0.2.2`, custom), Live AIDL vs Mock toggle, and hex packet logs, verified by `./gradlew assembleDebug`

## 6. End-to-End Simulator Verification

- [x] 6.1 Launch `n20_f34/tools/live_web_viewer.py` on macOS and run the Android app on Android Emulator targeting `10.0.2.2:9090` in Mock mode, verifying `0x01` SLI and `0x04` NAV frames update the Live Web Simulator `6WA/HUD` and `CID` panels
