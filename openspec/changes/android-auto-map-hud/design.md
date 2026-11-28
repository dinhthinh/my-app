# Design

## Context

See `proposal.md` for motivation. Currently, `DispatcherRepository` holds a process-wide `StateFlow<DispatcherUiState>` updated by `Id6DispatchService` (foreground service running `WazeModAidlClient` in `LIVE_AIDL` mode or `MockWazeModSource` in `MOCK_SIMULATOR` mode). The phone UI (`DashboardScreen`) observes this state via `MainActivity`. Because `DispatcherRepository` and `Id6DispatchService` are decoupled from `MainActivity`, an Android Auto `CarAppService` can observe the exact same state and control the same foreground service.

## Goals / Non-Goals

**Goals:**
- Run RoadCast ID6 on Android Auto as a `CATEGORY_NAVIGATION` app using AndroidX Car App Library (`androidx.car.app:app`, `app-projected`).
- Render a clean, landscape-optimized WazeMod HUD onto the Android Auto map `Surface` using `android.graphics.Canvas` inside `visibleArea`.
- Provide a single manual **Connect WazeMod** / **Disconnect** action in `NavigationTemplate`'s `ActionStrip` that works with both touchscreens and rotary controllers.

**Non-Goals:**
- Exposing `MockSimulatorPanel`, `PacketLogPanel`, or `SettingsSheet` on Android Auto.
- Auto-connecting to WazeMod / BMW ID6 on Android Auto launch (must be manual to protect the real vehicle HUD during testing).
- Launching WazeMod's phone-side permission activity (`ACTION_REQUEST_ACCESS`) directly on the car screen.

## Decisions

1. **`NavigationTemplate` + `android.graphics.Canvas` on `Surface` over `VirtualDisplay` + Compose**:
   - *Choice*: Implement `SurfaceCallback` on `RoadCastCarScreen` and draw the HUD directly onto the `Surface` (`lockCanvas` / `unlockCanvasAndPost`) via a dedicated `CarHudRenderer` using `android.graphics.Canvas`.
   - *Rationale*: On Android Auto, `VirtualDisplay` + `Presentation` with `MotionEvent` injection requires `Action.PAN` (Car API Level 5+) for `onClick`/`onScroll`, breaks rotary controller navigation, and is unnecessary when the car UI only needs HUD display plus a Connect/Disconnect button. Drawing directly with `Canvas` works from Car API Level 1+, is lightweight, and cleanly respects `onVisibleAreaChanged(Rect)`.
   - *Alternatives considered*: `VirtualDisplay` + Compose `DashboardScreen` (rejected as over-complex and rotary-incompatible given Mock Simulator is out of scope on the car).

2. **Manual Connect / Disconnect via `ActionStrip` forcing `LIVE_AIDL`**:
   - *Choice*: Place a single toggle action (`Connect WazeMod` when `!state.running`, `Disconnect` when `state.running`) in `NavigationTemplate.Builder().setActionStrip(...)`. Pressing Connect calls `DispatcherRepository.setSourceMode(TelemetrySourceMode.LIVE_AIDL)` and `Id6DispatchService.startOrReload(carContext)`. Pressing Disconnect calls `Id6DispatchService.stop(carContext)`.
   - *Rationale*: `ActionStrip` is natively focusable via both touch and rotary knob, and `invalidate()` refreshes the button label/icon whenever `DispatcherRepository.state` changes. Forcing `LIVE_AIDL` ensures Android Auto never accidentally dispatches Mock Simulator data to the car HUD.

3. **Shared Sign-Filtering & Geometry**:
   - *Choice*: Extract or mirror the prohibition sign filtering rules (`PROHIBITION_SIGN_TYPES`, `clearNoPassing` for alert types `10` and `25`, `.distinctBy { it.type }.take(3)`) and `TrafficSignCanvas` geometry into reusable helpers so phone and car HUDs stay consistent.
   - *Rationale*: Prevents drift between what the phone displays and what the car displays.

4. **Phone-Only Permission Fallback**:
   - *Choice*: When `sourceStatus` is `PERMISSION_REQUIRED` or `REVOKED`, `CarHudRenderer` draws a prominent warning card instructing the user to open RoadCast ID6 on the phone to grant WazeMod V14+ access.
   - *Rationale*: `WazeModAidlClient.ACTION_REQUEST_ACCESS` launches a third-party phone Activity that cannot be hosted inside Android Auto.

## Risks / Trade-offs

- **Surface redraw frequency**: Redrawing on every `StateFlow` emission could cause unnecessary work if snapshots arrive rapidly → Gate redraws on `Surface` availability, non-empty `visibleArea`, and redraw only when `DispatcherUiState` fields relevant to the HUD change (or on `SurfaceCallback` lifecycle/area callbacks).
- **ActionStrip auto-hide**: Android Auto's `NavigationTemplate` automatically hides the `ActionStrip` after ~10s of inactivity and reveals it on touch/rotary input → Acceptable and standard for driving safety; document this in testing notes so users know a tap on the screen reveals the Connect/Disconnect button.
- **Target IP/Port configuration**: Target host/port cannot be edited on Android Auto → Users configure `targetHost`/`targetPort` on the phone (`SettingsSheet`) before or during driving; the car HUD displays the active `targetHost:targetPort` for verification.
