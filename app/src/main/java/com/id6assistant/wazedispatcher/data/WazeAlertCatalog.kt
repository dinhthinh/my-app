package com.id6assistant.wazedispatcher.data

enum class AlertCategory {
    CAMERA,
    POLICE,
    TRAFFIC,
    HAZARD,
    RESTRICTION,
    WEATHER,
    GENERAL,
}

data class AlertDescriptor(
    val type: Int,
    val label: String,
    val category: AlertCategory,
    val badge: String,
)

object WazeAlertCatalog {
    private val catalog: Map<Int, AlertDescriptor> = mapOf(
        1 to AlertDescriptor(1, "Cảnh sát", AlertCategory.POLICE, "POL"),
        2 to AlertDescriptor(2, "Camera tốc độ", AlertCategory.CAMERA, "SPD"),
        3 to AlertDescriptor(3, "Camera đèn đỏ", AlertCategory.CAMERA, "RLC"),
        4 to AlertDescriptor(4, "Nguy hiểm", AlertCategory.HAZARD, "HAZ"),
        5 to AlertDescriptor(5, "Tai nạn", AlertCategory.HAZARD, "ACC"),
        6 to AlertDescriptor(6, "Kẹt xe", AlertCategory.TRAFFIC, "JAM"),
        7 to AlertDescriptor(7, "Đường đóng", AlertCategory.TRAFFIC, "CLS"),
        8 to AlertDescriptor(8, "Thay đổi giới hạn", AlertCategory.RESTRICTION, "LIM"),
        9 to AlertDescriptor(9, "Cấm vượt", AlertCategory.RESTRICTION, "NPI"),
        10 to AlertDescriptor(10, "Hết cấm vượt", AlertCategory.RESTRICTION, "OK"),
        11 to AlertDescriptor(11, "Đường sắt", AlertCategory.HAZARD, "RR"),
        12 to AlertDescriptor(12, "Trạm thu phí", AlertCategory.GENERAL, "TOL"),
        13 to AlertDescriptor(13, "Xe dừng", AlertCategory.HAZARD, "STP"),
        14 to AlertDescriptor(14, "Công trình", AlertCategory.HAZARD, "WRK"),
        15 to AlertDescriptor(15, "Ổ gà", AlertCategory.HAZARD, "POT"),
        16 to AlertDescriptor(16, "Thời tiết", AlertCategory.WEATHER, "WTH"),
        17 to AlertDescriptor(17, "Làn bị chặn", AlertCategory.TRAFFIC, "BLK"),
        18 to AlertDescriptor(18, "Đường nguy hiểm", AlertCategory.HAZARD, "DNG"),
        19 to AlertDescriptor(19, "Lối ra cao tốc", AlertCategory.GENERAL, "EXT"),
        20 to AlertDescriptor(20, "Trạm dừng nghỉ cao tốc", AlertCategory.GENERAL, "RST"),
        21 to AlertDescriptor(21, "Trạm dừng nghỉ", AlertCategory.GENERAL, "RST"),
        22 to AlertDescriptor(22, "Hết hạn chế tốc độ", AlertCategory.RESTRICTION, "END"),
        23 to AlertDescriptor(23, "Bắt đầu khu dân cư", AlertCategory.RESTRICTION, "URB"),
        24 to AlertDescriptor(24, "Hết khu dân cư", AlertCategory.RESTRICTION, "URB"),
        25 to AlertDescriptor(25, "Hết mọi lệnh cấm", AlertCategory.RESTRICTION, "CLR"),
        26 to AlertDescriptor(26, "Cấm ô tô", AlertCategory.RESTRICTION, "NO4"),
        27 to AlertDescriptor(27, "Cấm xe máy", AlertCategory.RESTRICTION, "NO2"),
        28 to AlertDescriptor(28, "Cấm rẽ trái", AlertCategory.RESTRICTION, "NLT"),
        29 to AlertDescriptor(29, "Cấm rẽ phải", AlertCategory.RESTRICTION, "NRT"),
        30 to AlertDescriptor(30, "Cấm quay đầu", AlertCategory.RESTRICTION, "NUT"),
        31 to AlertDescriptor(31, "Cấm đi thẳng", AlertCategory.RESTRICTION, "NST"),
        32 to AlertDescriptor(32, "Bắt buộc đi thẳng", AlertCategory.RESTRICTION, "MST"),
        33 to AlertDescriptor(33, "Bắt buộc rẽ phải", AlertCategory.RESTRICTION, "MRT"),
        34 to AlertDescriptor(34, "Bắt buộc rẽ trái", AlertCategory.RESTRICTION, "MLT"),
        35 to AlertDescriptor(35, "Làn ô tô", AlertCategory.GENERAL, "CAR"),
        36 to AlertDescriptor(36, "Làn xe máy", AlertCategory.GENERAL, "MOTO"),
        37 to AlertDescriptor(37, "Đường một chiều", AlertCategory.RESTRICTION, "1WY"),
        38 to AlertDescriptor(38, "Đường cấm", AlertCategory.RESTRICTION, "PRO"),
        39 to AlertDescriptor(39, "Cấm rẽ kết hợp", AlertCategory.RESTRICTION, "NTR"),
        40 to AlertDescriptor(40, "Camera phạt nguội (điện thoại)", AlertCategory.CAMERA, "CAM"),
        41 to AlertDescriptor(41, "Camera giả", AlertCategory.CAMERA, "DMY"),
        42 to AlertDescriptor(42, "Camera dây an toàn", AlertCategory.CAMERA, "SBT"),
        43 to AlertDescriptor(43, "Camera khoảng cách", AlertCategory.CAMERA, "DST"),
        44 to AlertDescriptor(44, "Camera làn xe buýt", AlertCategory.CAMERA, "BUS"),
        45 to AlertDescriptor(45, "Camera tiếng ồn", AlertCategory.CAMERA, "NSE"),
        46 to AlertDescriptor(46, "Camera biển dừng", AlertCategory.CAMERA, "STP"),
        47 to AlertDescriptor(47, "Động vật", AlertCategory.HAZARD, "ANM"),
        48 to AlertDescriptor(48, "Vật cản trên đường", AlertCategory.HAZARD, "OBJ"),
        49 to AlertDescriptor(49, "Động vật chết trên đường", AlertCategory.HAZARD, "RDK"),
        50 to AlertDescriptor(50, "Ngập", AlertCategory.WEATHER, "FLD"),
        51 to AlertDescriptor(51, "Sương mù", AlertCategory.WEATHER, "FOG"),
        52 to AlertDescriptor(52, "Mưa đá", AlertCategory.WEATHER, "HAL"),
        53 to AlertDescriptor(53, "Tuyết", AlertCategory.WEATHER, "SNW"),
        54 to AlertDescriptor(54, "Băng", AlertCategory.WEATHER, "ICE"),
        55 to AlertDescriptor(55, "Đường trơn", AlertCategory.HAZARD, "SLP"),
        56 to AlertDescriptor(56, "Gờ giảm tốc", AlertCategory.HAZARD, "BMP"),
        57 to AlertDescriptor(57, "Khu vực trường học", AlertCategory.HAZARD, "SCH"),
        58 to AlertDescriptor(58, "Nhập làn", AlertCategory.HAZARD, "MRG"),
        59 to AlertDescriptor(59, "Khúc cua nguy hiểm", AlertCategory.HAZARD, "CRV"),
        60 to AlertDescriptor(60, "Ngã ba", AlertCategory.HAZARD, "JNC"),
        61 to AlertDescriptor(61, "Đèn giao thông hỏng", AlertCategory.HAZARD, "TLF"),
        62 to AlertDescriptor(62, "Người đi xe đạp", AlertCategory.HAZARD, "CYC"),
        63 to AlertDescriptor(63, "Xe ưu tiên", AlertCategory.HAZARD, "EMG"),
        64 to AlertDescriptor(64, "An toàn cá nhân", AlertCategory.HAZARD, "SAF"),
        65 to AlertDescriptor(65, "Cấm đi thẳng và rẽ phải", AlertCategory.RESTRICTION, "NSR"),
        66 to AlertDescriptor(66, "Cấm rẽ trái và quay đầu", AlertCategory.RESTRICTION, "NLU"),
        67 to AlertDescriptor(67, "Cấm đi thẳng và rẽ trái", AlertCategory.RESTRICTION, "NSL"),
        68 to AlertDescriptor(68, "Cấm rẽ trái và rẽ phải", AlertCategory.RESTRICTION, "NLR"),
        69 to AlertDescriptor(69, "Ô tô cấm rẽ trái và quay đầu", AlertCategory.RESTRICTION, "CLU"),
        70 to AlertDescriptor(70, "Ô tô cấm rẽ phải và quay đầu", AlertCategory.RESTRICTION, "CRU"),
        71 to AlertDescriptor(71, "Cấm rẽ phải và quay đầu", AlertCategory.RESTRICTION, "NRU"),
        72 to AlertDescriptor(72, "Ô tô cấm rẽ trái", AlertCategory.RESTRICTION, "CLT"),
        73 to AlertDescriptor(73, "Ô tô cấm rẽ phải", AlertCategory.RESTRICTION, "CRT"),
        74 to AlertDescriptor(74, "Ô tô cấm quay đầu", AlertCategory.RESTRICTION, "CUT"),
        75 to AlertDescriptor(75, "Đèn giao thông phía trước", AlertCategory.GENERAL, "TLT"),
    )

    fun describe(type: Int): AlertDescriptor =
        catalog[type] ?: AlertDescriptor(
            type = type,
            label = "Cảnh báo #$type",
            category = AlertCategory.GENERAL,
            badge = "ALT",
        )

    fun formatDetail(alert: WazeAlert): String {
        val desc = describe(alert.type)
        val suffix = when (alert.type) {
            6 -> {
                val parts = buildList {
                    if (alert.jamSeverity > 0) add("Mức ${alert.jamSeverity}")
                    if (alert.jamDelayMin > 0) add("+${alert.jamDelayMin} phút")
                }
                if (parts.isNotEmpty()) " (${parts.joinToString(" · ")})" else ""
            }
            8 -> if (alert.value > 0) " (${alert.value} km/h)" else ""
            else -> ""
        }
        val dist = if (alert.distanceM > 0) " · ${alert.distanceM} m" else ""
        return "${desc.label}$suffix$dist"
    }

    fun allKnownTypes(): Set<Int> = catalog.keys
}
