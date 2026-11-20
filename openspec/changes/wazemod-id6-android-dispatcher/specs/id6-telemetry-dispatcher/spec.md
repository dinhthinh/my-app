# Spec Delta

## Purpose

Maps WazeMod telemetry snapshots (including `alerts[]`, speed limits, turns, lanes, and street names) into `Id6ProtocolCodec` binary frames and dispatches them over TCP port `9090` from an Android Foreground Service.

## ADDED Requirements

### Requirement: Map WazeMod snapshot and alerts to SLI frame
The dispatcher MUST encode WazeMod `speed_limit`, `speed_min`, `overspeed`, and relevant `alerts[].type` entries into a 5-byte `0x01` SLI packet using `Id6ProtocolCodec.encodeSliPacket`.

#### Scenario: No-passing and speed-limit alerts mapped to SLI
- **WHEN** a snapshot contains an alert with `type = 9` (No-Passing) and `speed_limit = 60`
- **THEN** the encoded SLI packet sets `limitKmh = 60` and `noPassing = true`

#### Scenario: Speed-limit change alert fallback
- **WHEN** `speed_limit` is `0` and an alert with `type = 8` provides `value = 50`
- **THEN** the mapper uses `50` as `limitKmh` unless a `type = 22` (end of speed restriction) clears it

### Requirement: Map WazeMod navigation state to NAV frame
When `navigating = true`, the dispatcher MUST map `turn` (0..20) to `Id6Bits.ID6_NAV_MANEUVER_*`, forward `turn_distance_m`, `roundabout_exit`, `lanes[]` (`directions`, `recommended`), and `next_street` (falling back to `current_street`) into a `0x04` NAV packet via `Id6ProtocolCodec.encodeNavPacket`.

#### Scenario: Active turn-by-turn navigation
- **WHEN** `navigating` is `true` with `turn = 2` (Left), `turn_distance_m = 250`, and `next_street = "Lê Lợi"`
- **THEN** the dispatcher produces a `0x04` packet with `ID6_NAV_MANEUVER_LEFT`, `distanceM = 250`, and ASCII street `"Le Loi"`

#### Scenario: Navigation ended or access revoked
- **WHEN** `navigating` transitions from `true` to `false` or WazeMod access is revoked
- **THEN** the dispatcher immediately sends one `0x04` NAV packet with `active = false` to clear the 6WA/HUD panel

### Requirement: Dispatch frames over TCP 9090 with deduplication and throttling
The dispatcher MUST send encoded binary frames via short-lived TCP connections (`800ms` connect timeout) inside an Android Foreground Service, deduplicating identical consecutive SLI packets and throttling `0x04` NAV packets to at most 5 Hz (`200ms`).

#### Scenario: Identical SLI packets deduplicated
- **WHEN** two consecutive snapshots produce the exact same 5-byte SLI payload
- **THEN** the dispatcher skips sending the duplicate SLI TCP packet

#### Scenario: High-frequency navigation updates throttled
- **WHEN** WazeMod emits snapshots faster than every `200ms`
- **THEN** `0x04` NAV packets are dispatched at most once per `200ms` while state transitions (`active = false`) bypass throttling
