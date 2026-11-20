package com.id6assistant.wazedispatcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    currentHost: String,
    currentPort: Int,
    onSave: (String, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var host by remember(currentHost) { mutableStateOf(currentHost) }
    var portText by remember(currentPort) { mutableStateOf(currentPort.toString()) }

    val presets = listOf(
        "CarPlay uap0" to "172.16.224.1",
        "Wi-Fi Direct wfd0" to "172.16.222.1",
        "Emulator Host" to "10.0.2.2",
        "OBD2 ENET" to "169.254.199.119",
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Cấu hình đầu nhận ID6 (TCP :9090)",
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                presets.forEach { (label, ip) ->
                    FilterChip(
                        selected = host == ip,
                        onClick = { host = ip },
                        label = { Text("$label ($ip)") },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host IP") },
                    singleLine = true,
                    modifier = Modifier.weight(2f),
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter(Char::isDigit) },
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val parsedPort = portText.toIntOrNull()?.coerceIn(1, 65535) ?: 9090
                    onSave(host.ifBlank { "172.16.224.1" }, parsedPort)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Áp dụng Endpoint")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
