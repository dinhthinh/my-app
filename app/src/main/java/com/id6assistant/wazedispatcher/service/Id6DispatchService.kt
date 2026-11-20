package com.id6assistant.wazedispatcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.id6assistant.wazedispatcher.MainActivity
import com.id6assistant.wazedispatcher.data.MockWazeModSource
import com.id6assistant.wazedispatcher.data.WazeModAidlClient
import com.id6assistant.wazedispatcher.data.WazeModSnapshot
import com.id6assistant.wazedispatcher.data.WazeSourceStatus
import com.id6assistant.wazedispatcher.dispatch.Id6TcpDispatcher
import com.id6assistant.wazedispatcher.dispatch.WazeModToId6Mapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class Id6DispatchService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tcpDispatcher = Id6TcpDispatcher()
    private var aidlClient: WazeModAidlClient? = null
    private var streamJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        when (intent?.action) {
            ACTION_STOP -> {
                stopDispatching()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_RELOAD -> {
                restartSource()
                return START_STICKY
            }
            else -> {
                restartSource()
                return START_STICKY
            }
        }
    }

    private fun startForegroundCompat() {
        val notification = buildNotification("Đang chạy nền · Sẵn sàng gửi dữ liệu tới ID6")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun restartSource() {
        streamJob?.cancel()
        aidlClient?.stop()
        aidlClient = null
        tcpDispatcher.resetState()

        val currentState = DispatcherRepository.state.value
        tcpDispatcher.host = currentState.targetHost
        tcpDispatcher.port = currentState.targetPort
        DispatcherRepository.setRunning(true)

        when (currentState.sourceMode) {
            TelemetrySourceMode.MOCK_SIMULATOR -> {
                DispatcherRepository.updateStatus(WazeSourceStatus.CONNECTED, "Mock Simulator đang chạy")
                val mockSource = MockWazeModSource { DispatcherRepository.state.value.mockConfig }
                streamJob = serviceScope.launch {
                    mockSource.stream(500L).collect { snapshot ->
                        handleSnapshot(snapshot)
                    }
                }
            }
            TelemetrySourceMode.LIVE_AIDL -> {
                val client = WazeModAidlClient(
                    context = this,
                    onSnapshot = { snapshot ->
                        serviceScope.launch { handleSnapshot(snapshot) }
                    },
                    onStatus = { status, message ->
                        DispatcherRepository.updateStatus(status, message)
                        if (status == WazeSourceStatus.REVOKED) {
                            serviceScope.launch {
                                val clearLog = tcpDispatcher.clearNav()
                                DispatcherRepository.appendLogs(listOf(clearLog))
                            }
                        }
                    },
                )
                aidlClient = client
                client.start()
            }
        }
    }

    private suspend fun handleSnapshot(snapshot: WazeModSnapshot) {
        val currentState = DispatcherRepository.state.value
        tcpDispatcher.host = currentState.targetHost
        tcpDispatcher.port = currentState.targetPort

        DispatcherRepository.updateSnapshot(snapshot)
        val mapped = WazeModToId6Mapper.map(snapshot)
        val logs = tcpDispatcher.dispatch(mapped)
        DispatcherRepository.appendLogs(logs)
    }

    private fun stopDispatching() {
        streamJob?.cancel()
        streamJob = null
        aidlClient?.stop()
        aidlClient = null
        DispatcherRepository.setRunning(false)
        DispatcherRepository.updateStatus(WazeSourceStatus.DISCONNECTED, "Đã dừng")
        serviceScope.launch {
            val clearLog = tcpDispatcher.clearNav()
            DispatcherRepository.appendLogs(listOf(clearLog))
        }
    }

    override fun onDestroy() {
        streamJob?.cancel()
        aidlClient?.stop()
        DispatcherRepository.setRunning(false)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ID6 Telemetry Dispatcher",
                NotificationManager.IMPORTANCE_LOW,
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RoadCast ID6")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "id6_dispatch_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.id6assistant.wazedispatcher.START"
        const val ACTION_STOP = "com.id6assistant.wazedispatcher.STOP"
        const val ACTION_RELOAD = "com.id6assistant.wazedispatcher.RELOAD"

        fun startOrReload(context: Context) {
            val intent = Intent(context, Id6DispatchService::class.java).apply {
                action = ACTION_RELOAD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, Id6DispatchService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
