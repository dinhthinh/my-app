# Spec Delta

## Purpose

Provides a Jetpack Compose companion HUD and diagnostics interface displaying live speed, speed limits, WazeMod `alerts[].type` (1..75), average-speed zone status, turn/lane preview, endpoint settings, and packet logs.

## ADDED Requirements

### Requirement: Display WazeMod alerts catalog (types 1..75) and average-speed zone
The UI MUST render up to 3 nearest `alerts[]` from each snapshot with human-readable Vietnamese labels, distance in meters, and type-specific metadata (`jam_severity` and `jam_delay_min` for `type = 6`, `value` for `type = 8`), plus `avg_zone` progress (`avg_remaining_m`, `avg_recommended_speed`, `avg_percent`), and MUST fall back to a generic alert icon/label for unknown future codes.

#### Scenario: Traffic jam and speed camera alerts rendered
- **WHEN** a snapshot includes `alerts = [{type: 2, distance_m: 320}, {type: 6, distance_m: 500, jam_severity: 3, jam_delay_min: 7}]`
- **THEN** the HUD displays "Camera tốc độ · 320 m" and "Kẹt xe (Mức 3 · +7 phút) · 500 m"

#### Scenario: Unknown alert code fallback
- **WHEN** a snapshot includes an alert with an unrecognized `type` (e.g., `type = 99`)
- **THEN** the HUD renders a generic warning badge with the raw code and distance without crashing

### Requirement: Configure target endpoint and inspect live packet dispatch
The UI MUST allow selecting preset targets (`172.16.224.1` CarPlay `uap0`, `172.16.222.1` Wi-Fi Direct `wfd0`, `10.0.2.2` Android Emulator host) or entering a custom IP/port, toggling Live AIDL vs Mock mode, and viewing recent dispatched hex frames (`0x01`, `0x04`) and connection errors.

#### Scenario: Switch target to macOS simulator via emulator host
- **WHEN** the user selects `10.0.2.2:9090` and enables Mock mode
- **THEN** the dispatcher sends encoded SLI and NAV packets to `10.0.2.2:9090` and appends their hex representations to the live packet log
