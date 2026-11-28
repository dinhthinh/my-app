# Proposal

## Why

Driving with RoadCast ID6 currently requires looking at or interacting with the phone screen to connect WazeMod telemetry and monitor traffic signs, speed limits, and alerts. Adding Android Auto support registered as a Navigation app surfaces the WazeMod HUD and manual Connect/Disconnect control directly on the vehicle head unit.

## What Changes

- Register RoadCast ID6 as an Android Auto Navigation app (`androidx.car.app.category.NAVIGATION`) with `NAVIGATION_TEMPLATES` and `ACCESS_SURFACE` permissions and `automotive_app_desc.xml`.
- Add `RoadCastCarAppService`, `RoadCastCarSession`, and `RoadCastCarScreen` using `NavigationTemplate` and `SurfaceCallback` to render the HUD onto the car map `Surface` within `visibleArea`.
- Render WazeMod connection status (`host:port` and `sourceStatus`), speed limit / minimum speed / up to 3 prohibition signs (reusing the `DashboardScreen` filtering logic), current speed (with overspeed highlight), average speed zone progress, top WazeMod alerts, and a phone-permission prompt when WazeMod access is `PERMISSION_REQUIRED` or `REVOKED`.
- Add a manual **Connect WazeMod** / **Disconnect** button in `NavigationTemplate`'s `ActionStrip` that sets `TelemetrySourceMode.LIVE_AIDL` and starts/stops `Id6DispatchService`, without auto-connecting on launch so the real vehicle HUD is protected during testing.

## Capabilities

### New Capabilities
- `android-auto-hud`: Android Auto Navigation registration, map `Surface` Canvas HUD rendering for WazeMod telemetry and traffic signs, and manual `Connect WazeMod` / `Disconnect` control via `ActionStrip`.

### Modified Capabilities
<!-- None: no existing specs in openspec/specs/. -->

## Impact

- Dependencies: `gradle/libs.versions.toml`, `app/build.gradle.kts` (`androidx.car.app:app`, `androidx.car.app:app-projected`)
- Manifest & resources: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/automotive_app_desc.xml`
- Car UI package: new `com.id6assistant.wazedispatcher.car` classes (`RoadCastCarAppService`, `RoadCastCarSession`, `RoadCastCarScreen`, Canvas HUD renderer)
- Existing components reused: `DispatcherRepository`, `Id6DispatchService`, `WazeAlertCatalog`, and `TrafficSignCanvas` sign-drawing rules
