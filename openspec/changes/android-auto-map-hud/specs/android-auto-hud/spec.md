# Spec Delta

## Purpose

Enables RoadCast ID6 to run on Android Auto as a registered Navigation application, rendering WazeMod traffic signs, speed, and alerts onto the vehicle display's map surface while providing manual Connect/Disconnect control.

## ADDED Requirements

### Requirement: Android Auto Navigation App Registration
The system SHALL register RoadCast ID6 as an Android Auto navigation application capable of rendering onto a host-provided map surface.

#### Scenario: Head unit discovers RoadCast ID6
- **WHEN** the Android Auto host queries installed navigation applications
- **THEN** RoadCast ID6 is listed as an available navigation app and can be launched on the vehicle display

### Requirement: Manual WazeMod Connection Control
The system SHALL provide a manual Connect/Disconnect action on the Android Auto navigation interface and SHALL NOT automatically start dispatching telemetry when the Android Auto screen opens.

#### Scenario: Initial launch with service stopped
- **WHEN** the user opens RoadCast ID6 on Android Auto while telemetry dispatching is stopped
- **THEN** the system displays an idle disconnected state on the vehicle display and offers a "Connect WazeMod" action without sending packets to the target HUD

#### Scenario: User taps Connect WazeMod
- **WHEN** the user selects the "Connect WazeMod" action on Android Auto
- **THEN** the system switches the telemetry source mode to live WazeMod, starts background telemetry dispatching, and updates the action to "Disconnect"

#### Scenario: User taps Disconnect
- **WHEN** the user selects the "Disconnect" action while telemetry dispatching is running
- **THEN** the system stops background telemetry dispatching and returns the Android Auto display to the disconnected idle state

### Requirement: Map Surface HUD Rendering
The system SHALL render the WazeMod HUD onto the Android Auto map surface within the host-provided visible area so that vehicle system rails and template overlays do not obscure HUD content.

#### Scenario: Connected with active WazeMod telemetry
- **WHEN** the system is connected and receiving WazeMod snapshots
- **THEN** the map surface displays the connection status and target host/port, speed limit sign, minimum speed sign when greater than zero, up to three distinct prohibition signs (excluding no-passing when cleared by clear-no-passing alerts), current speed (highlighted when overspeeding), average speed zone progress when active, and up to three upcoming WazeMod alerts

#### Scenario: WazeMod permission missing or revoked
- **WHEN** WazeMod access status is permission-required or revoked while running on Android Auto
- **THEN** the map surface displays a clear prompt instructing the user to open RoadCast ID6 on the phone to grant WazeMod access
