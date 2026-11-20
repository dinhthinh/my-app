# RoadCast ID6

Ứng dụng Android (Kotlin + Jetpack Compose) đóng vai trò cầu nối telemetry giữa **WazeMod (V14+)** (hoặc **Mock Simulator** tích hợp) và hệ thống hiển thị **BMW ID6 / 6WA / HUD** qua giao thức nhị phân TCP (mặc định cổng `9090`).

> [!IMPORTANT]
> **Mục đích sử dụng cá nhân & phi thương mại (Personal & Non-Commercial Use Only)**
> Dự án này được phát triển phục vụ mục đích nghiên cứu, học tập và sử dụng cá nhân. Không sử dụng cho mục đích thương mại, phân phối lại có thu phí hoặc triển khai sản phẩm thương mại dưới bất kỳ hình thức nào.

---

## Tính năng chính

- **Hai nguồn dữ liệu đầu vào**:
  - **WazeMod AIDL (`LIVE_AIDL`)**: Kết nối trực tiếp tới WazeMod V14+ thông qua AIDL (`com.wazemod.api.action.BIND` / `com.wazemod.api.action.REQUEST_ACCESS`), nhận tốc độ, giới hạn tốc độ, cảnh báo (camera, cấm vượt, kẹt xe, ...) và thông tin dẫn đường Turn-by-Turn theo thời gian thực.
  - **Mock Simulator (`MOCK_SIMULATOR`)**: Bộ giả lập tích hợp (đồng bộ tính năng với giao diện Web Simulator) cho phép cấu hình và gửi trực tiếp từng gói tin qua TCP mà không cần kết nối WazeMod:
    - **Traffic Sign Simulator (`0x01` • 5 bytes)**: Max/Min speed (`1..250`, `0 = blank/hide`), Overspeed Blink, No-Passing Sign.
    - **ADAS Lane & Distance Simulator (`0x02` • 3 bytes)**: Lane Active, Left/Right Lane Warn, Distance Warn, khoảng cách xe phía trước (`1..250 m`).
    - **Combined 7-Byte Frame (`0x03` • 7 bytes)**: Hiển thị chi tiết từng byte (`HDR`, `CAN_B0..B2`, `MIN_KMH`, `FLAGS`, `DIST_M`), hỗ trợ **Auto-send** ngay khi thay đổi thông số.
    - **Turn-by-Turn Navigation Simulator (`0x04`)**: Chọn Maneuver, Distance, Roundabout Exit, Angle, Street (tự động chuẩn hóa ASCII an toàn cho 6WA), xem trước hex frame và gửi/xóa dẫn đường (`Send` / `Clear 6WA/HUD NAV`).
- **Foreground Service (`Id6DispatchService`)**: Chạy nền ổn định để stream dữ liệu liên tục tới đích TCP cấu hình trong phần Settings.
- **TCP Packet Log (`PacketLogPanel`)**: Ghi nhận trực tiếp các gói tin đã gửi kèm mã hex và trạng thái thành công/lỗi.

---

## Giao thức đóng gói (`Id6ProtocolCodec`)

Mã hóa tại `app/src/main/java/com/id6assistant/sdk/Id6ProtocolCodec.kt` và gửi qua TCP port `9090` (`Id6TcpDispatcher`):

| Header | Tên gói | Độ dài | Cấu trúc payload |
| --- | --- | --- | --- |
| `0x01` | SLI (Traffic Sign) | 5 bytes | `[0x01, can_b0, can_b1, can_b2, min_kmh]` |
| `0x02` | ADAS Lane & Distance | 3 bytes | `[0x02, adas_flags, dist_m]` |
| `0x03` | Combined SLI + Lane | 7 bytes | `[0x03, can_b0, can_b1, can_b2, min_kmh, adas_flags, dist_m]` |
| `0x04` | Turn-by-Turn NAV | 12..60 bytes | `[0x04, flags, maneuver, exit, angle(2B), dist(4B), lane_count, lanes..., street_len, street_ascii...]` |

---

## Yêu cầu môi trường & Cấu trúc chính

- **Android SDK**: `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`, Java `17`
- **Các module/thư mục chính**:
  - `app/src/main/java/com/id6assistant/sdk/Id6ProtocolCodec.kt`: Codec nhị phân cho các frame `0x01`–`0x04`.
  - `app/src/main/java/com/id6assistant/wazedispatcher/data/`: Parser Bundle AIDL, `WazeModAidlClient`, `MockWazeModSource`, `MockSimulatorState`.
  - `app/src/main/java/com/id6assistant/wazedispatcher/dispatch/`: `WazeModToId6Mapper`, `Id6TcpDispatcher`.
  - `app/src/main/java/com/id6assistant/wazedispatcher/service/`: `DispatcherRepository`, `Id6DispatchService`.
  - `app/src/main/java/com/id6assistant/wazedispatcher/ui/`: `DashboardScreen`, `MockSimulatorPanel`, `PacketLogPanel`, `SettingsSheet`.

---

## Hướng dẫn Build, Test & Chạy Dev

### 1. Chạy bằng VS Code / IDE Tasks (`.vscode/tasks.json`)
Mở Command Palette (`Cmd+Shift+P`) → **Tasks: Run Task**:

- **`start-dev`** *(Default Build — `Cmd+Shift+B`)*: Build debug, cài lên emulator/thiết bị và khởi động lại app.
- **`start-dev: test + reload`**: Chạy unit test debug trước khi build, cài và reload app.
- **`build: debug`**: `./gradlew assembleDebug`
- **`build: production`**: `./gradlew assembleRelease`
- **`build: test`**: `./gradlew test`
- **`adb: reload app`**: Dừng tiến trình cũ và mở lại `MainActivity`.
- **`adb: logcat`**: Xem log trực tiếp của app qua ADB.
- **`clean`**: `./gradlew clean`

### 2. Chạy bằng dòng lệnh (CLI)

```bash
# Chạy unit test
./gradlew test

# Build debug APK và cài + reload trên thiết bị/emulator đang kết nối
./gradlew installDebug && adb shell am force-stop com.id6assistant.wazedispatcher && adb shell am start -n com.id6assistant.wazedispatcher/.MainActivity

# Build release APK
./gradlew assembleRelease
```

### 3. Kết nối Target TCP (`:9090`)
- **Android Emulator**: Dùng mặc định `10.0.2.2:9090` để trỏ về `localhost:9090` trên máy tính.
- **Thiết bị Android thật**:
  - Nếu simulator chạy trên máy tính qua cùng Wi-Fi: đổi Host trong Settings thành IP LAN của máy tính.
  - Nếu kết nối qua cáp USB: chạy `adb reverse tcp:9090 tcp:9090` và đặt Host trong app là `127.0.0.1`.
  - Nếu kết nối trực tiếp tới hệ thống ID6: nhập IP của thiết bị ID6 (ví dụ `172.16.224.1`).

---

## Tuyên bố miễn trừ trách nhiệm (Disclaimer)

1. **Mục đích cá nhân, phi thương mại**: Mã nguồn và ứng dụng này được tạo ra chỉ nhằm mục đích thử nghiệm kỹ thuật, học tập và sử dụng cá nhân. Nghiêm cấm sử dụng cho mục đích thương mại hoặc kinh doanh dưới mọi hình thức.
2. **An toàn giao thông**: Không thao tác, cấu hình ứng dụng hoặc nhìn vào màn hình thiết bị khi đang điều khiển phương tiện tham gia giao thông. Người lái xe phải luôn tuân thủ luật giao thông đường bộ, biển báo thực tế và giữ toàn quyền kiểm soát phương tiện. Dữ liệu tốc độ, biển báo hoặc dẫn đường hiển thị chỉ mang tính tham khảo và có thể sai lệch hoặc trễ.
3. **Không liên kết thương hiệu**: Dự án này là sản phẩm độc lập, **không** liên kết, tài trợ hay được chứng nhận bởi BMW, Waze hoặc Google. Mọi nhãn hiệu (nếu được nhắc đến) đều thuộc quyền sở hữu của chủ sở hữu tương ứng.
4. **Cung cấp nguyên trạng ("AS IS")**: Phần mềm được cung cấp nguyên trạng, không có bất kỳ bảo đảm nào (dù rõ ràng hay ngụ ý) về độ chính xác, tính ổn định hoặc sự phù hợp cho một mục đích cụ thể. Tác giả và người đóng góp không chịu trách nhiệm đối với bất kỳ thiệt hại phần cứng, phần mềm, sự cố phương tiện, vi phạm giao thông hoặc rủi ro nào phát sinh từ việc cài đặt hoặc sử dụng phần mềm này. Người dùng tự chịu hoàn toàn trách nhiệm khi sử dụng.
