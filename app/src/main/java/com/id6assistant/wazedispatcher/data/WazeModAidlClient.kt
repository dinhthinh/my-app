package com.id6assistant.wazedispatcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import com.wazemod.api.IWazeModData
import com.wazemod.api.IWazeModListener

enum class WazeSourceStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    PERMISSION_REQUIRED,
    REVOKED,
    UNSUPPORTED_VERSION,
    NOT_INSTALLED,
}

class WazeModAidlClient(
    private val context: Context,
    private val onSnapshot: (WazeModSnapshot) -> Unit,
    private val onStatus: (WazeSourceStatus, String?) -> Unit,
) {
    private var api: IWazeModData? = null
    private var bound = false

    private val listener = object : IWazeModListener.Stub() {
        override fun onUpdate(snapshot: Bundle?) {
            onSnapshot(WazeModBundleParser.parse(snapshot))
        }

        override fun onRevoked() {
            stop()
            onStatus(WazeSourceStatus.REVOKED, "Quyền truy cập WazeMod đã bị thu hồi")
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val service = IWazeModData.Stub.asInterface(binder)
            try {
                if (service.apiVersion < 1) {
                    stop()
                    onStatus(WazeSourceStatus.UNSUPPORTED_VERSION, "Cần WazeMod phiên bản V14+")
                    return
                }
                api = service
                service.snapshot?.let { onSnapshot(WazeModBundleParser.parse(it)) }
                service.registerListener(listener)
                onStatus(WazeSourceStatus.CONNECTED, name.packageName)
            } catch (_: SecurityException) {
                stop()
                onStatus(WazeSourceStatus.PERMISSION_REQUIRED, "Cần cấp quyền truy cập WazeMod")
            } catch (e: RemoteException) {
                api = null
                onStatus(WazeSourceStatus.DISCONNECTED, e.message)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            api = null
            onStatus(WazeSourceStatus.DISCONNECTED, "WazeMod service đã ngắt kết nối")
        }
    }

    fun findHosts(): List<ServiceInfo> =
        context.packageManager
            .queryIntentServices(Intent(ACTION_BIND), 0)
            .map { it.serviceInfo }

    fun createAccessRequestIntent(targetPackage: String? = null): Intent =
        Intent(ACTION_REQUEST_ACCESS).apply {
            if (!targetPackage.isNullOrBlank()) {
                setPackage(targetPackage)
            }
        }

    fun start(targetPackage: String? = null): Boolean {
        val hosts = findHosts()
        if (hosts.isEmpty()) {
            onStatus(WazeSourceStatus.NOT_INSTALLED, "Không tìm thấy WazeMod V14+ trên thiết bị")
            return false
        }
        val chosen = hosts.firstOrNull { it.packageName == targetPackage } ?: hosts.first()
        val intent = Intent(ACTION_BIND).apply {
            component = ComponentName(chosen.packageName, chosen.name)
        }
        onStatus(WazeSourceStatus.CONNECTING, chosen.packageName)
        bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            onStatus(WazeSourceStatus.DISCONNECTED, "Không thể bind tới ${chosen.packageName}")
        }
        return bound
    }

    fun stop() {
        try {
            api?.unregisterListener(listener)
        } catch (_: RemoteException) {
        }
        api = null
        if (bound) {
            try {
                context.unbindService(connection)
            } catch (_: IllegalArgumentException) {
            }
            bound = false
        }
    }

    companion object {
        const val ACTION_BIND = "com.wazemod.api.action.BIND"
        const val ACTION_REQUEST_ACCESS = "com.wazemod.api.action.REQUEST_ACCESS"
    }
}
