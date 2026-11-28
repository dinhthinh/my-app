package com.id6assistant.wazedispatcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.id6assistant.wazedispatcher.dispatch.PacketLogEntry

@Composable
fun PacketLogPanel(
    logs: List<PacketLogEntry>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "TCP :9090 Packet Log (${logs.size})",
                color = Color(0xFFF8FAFC),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = "Xóa log",
                    tint = Color(0xFF94A3B8),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        if (logs.isEmpty()) {
            Text(
                text = "Chưa có gói tin nào được gửi.",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
            )
        } else {
            logs.take(12).forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = entry.typeLabel,
                        color = if (entry.success) Color(0xFF38BDF8) else Color(0xFFF87171),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (entry.success) entry.hex else "${entry.hex} (${entry.error})",
                        color = if (entry.success) Color(0xFFCBD5E1) else Color(0xFFFCA5A5),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
