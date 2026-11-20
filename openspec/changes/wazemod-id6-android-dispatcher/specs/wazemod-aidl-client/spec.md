# Spec Delta

## Purpose

Manages the Android AIDL connection to WazeMod V14+ (`IWazeModData` / `IWazeModListener`), permission lifecycle, and a built-in mock snapshot source for simulator testing.

## ADDED Requirements

### Requirement: Bind to WazeMod V14+ AIDL service
The client MUST discover and bind to the WazeMod V14+ data service via Android AIDL (`IWazeModData`) and verify `apiVersion >= 1` before registering an `IWazeModListener`.

#### Scenario: Successful AIDL connection and subscription
- **WHEN** WazeMod V14+ is installed and access permission is already granted
- **THEN** the client binds to the service, registers `IWazeModListener`, and emits incoming `Bundle` snapshots to observers

#### Scenario: Unsupported WazeMod version
- **WHEN** the bound service reports `apiVersion < 1`
- **THEN** the client unbinds immediately and reports an incompatible-version state

### Requirement: Request and recover access permission
The client MUST invoke `requestAccessIntent()` when WazeMod throws a `SecurityException` or reports ungranted access, and MUST clean up and notify observers when `onRevoked()` or `DeadObjectException` occurs.

#### Scenario: User grants access via permission prompt
- **WHEN** `start()` fails with `SecurityException` and the user completes `requestAccessIntent()` with `RESULT_OK`
- **THEN** the client retries `start()` and transitions to the connected state

#### Scenario: Access revoked by WazeMod
- **WHEN** `IWazeModListener.onRevoked()` is invoked
- **THEN** the client stops listening, transitions to `revoked` state, and emits a stop/clear signal downstream

### Requirement: Provide built-in mock snapshot source
The client MUST support switching between the live WazeMod AIDL source and an internal mock snapshot generator for bench testing against `id6-assistant-host` without WazeMod installed.

#### Scenario: Bench simulation mode enabled
- **WHEN** the user selects Mock Source mode
- **THEN** the client emits synthetic snapshots containing configurable speed, `speed_limit`, `overspeed`, `turn`, `lanes`, `avg_zone`, and `alerts[]` (`type` 1..75)
