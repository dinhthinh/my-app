# Tasks

## 1. Android Auto Dependencies & Manifest Registration

- [x] 1.1 Add `androidx.car.app:app` and `androidx.car.app:app-projected` to `gradle/libs.versions.toml` and `app/build.gradle.kts`, and verify Gradle sync/compile succeeds (`./gradlew :app:compileDebugKotlin`).
- [x] 1.2 Create `app/src/main/res/xml/automotive_app_desc.xml` declaring `<uses name="template" />`, and update `app/src/main/AndroidManifest.xml` with `NAVIGATION_TEMPLATES`, `ACCESS_SURFACE`, `com.google.android.gms.car.application` metadata, `androidx.car.app.minCarApiLevel` metadata, and the exported `RoadCastCarAppService` (`androidx.car.app.category.NAVIGATION`); verify manifest merge succeeds (`./gradlew :app:processDebugMainManifest`).

## 2. Shared Sign Filtering & Car Surface HUD Renderer

- [x] 2.1 Extract shared WazeMod prohibition sign filtering logic (`PROHIBITION_SIGN_TYPES`, `clearNoPassing` handling, max 3 distinct signs) into a reusable helper (e.g., in `com.id6assistant.wazedispatcher.data` or `ui`) used by both `DashboardScreen` and the car HUD, and add unit tests verifying filtering rules (`./gradlew :app:testDebugUnitTest`).
- [x] 2.2 Implement `CarHudRenderer` using `android.graphics.Canvas` to render within `visibleArea`: disconnected idle prompt, `PERMISSION_REQUIRED`/`REVOKED` phone-prompt banner, or connected HUD (WazeMod status + `host:port`, speed limit sign, min speed sign when > 0, up to 3 prohibition signs, current speed with overspeed color, average speed zone progress, and top 3 WazeMod alerts); verify compilation and add unit tests for HUD view-model/state formatting (`./gradlew :app:testDebugUnitTest`).

## 3. CarAppService, Session, & NavigationTemplate Screen

- [x] 3.1 Implement `RoadCastCarAppService`, `RoadCastCarSession`, and `RoadCastCarScreen` using `NavigationTemplate` + `SurfaceCallback` (`onSurfaceAvailable`, `onSurfaceDestroyed`, `onVisibleAreaChanged`), observing `DispatcherRepository.state` without auto-connecting on launch.
- [x] 3.2 Add the manual `Connect WazeMod` / `Disconnect` toggle action to `NavigationTemplate`'s `ActionStrip` (setting `TelemetrySourceMode.LIVE_AIDL` and calling `Id6DispatchService.startOrReload` on Connect, or `Id6DispatchService.stop` on Disconnect) and verify `invalidate()` updates the action label/state when `DispatcherRepository.state.running` changes.

## 4. End-to-End Build & Verification

- [x] 4.1 Run full debug build and unit test suite (`./gradlew :app:assembleDebug :app:testDebugUnitTest`) and verify all tests pass with zero compilation or manifest errors.
