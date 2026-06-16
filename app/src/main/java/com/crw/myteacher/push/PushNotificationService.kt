package com.crw.myteacher.push

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.ChatStompClient
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PushNotificationService : Service() {

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 9999
        private const val MAX_RECONNECT_DELAY_MS = 60_000L

        @Volatile
        var activeChatRoomId: String? = null

        fun start(context: Context) {
            val intent = Intent(context, PushNotificationService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PushNotificationService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var stompClient: ChatStompClient

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        ApiClient.init(this)
        stompClient = ChatStompClient(ApiClient.getTokenManager())

        ServiceCompat.startForeground(
            this,
            SERVICE_NOTIFICATION_ID,
            NotificationHelper.buildServiceNotification(this),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!ApiClient.getTokenManager().isLoggedIn) {
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch { connectAndSubscribe() }
        return START_STICKY
    }

    private suspend fun connectAndSubscribe() {
        var retryDelay = 2_000L

        while (true) {
            try {
                stompClient.connect()

                val chatRooms = ApiClient.api.getMyChatRooms().body()

                if (chatRooms.isNullOrEmpty()) {
                    delay(30_000)
                    continue
                }

                for (room in chatRooms) {
                    serviceScope.launch { subscribeToRoom(room.id, room.name) }
                }

                retryDelay = 2_000L

                stompClient.connectionState.collect { state ->
                    if (state == ChatStompClient.ConnectionState.DISCONNECTED ||
                        state == ChatStompClient.ConnectionState.ERROR
                    ) {
                        throw ReconnectException()
                    }
                }
            } catch (_: ReconnectException) {
            } catch (_: Exception) {
            }

            delay(retryDelay.milliseconds)
            retryDelay = (retryDelay * 2).coerceAtMost(MAX_RECONNECT_DELAY_MS)

            try {
                stompClient.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun subscribeToRoom(chatRoomId: String, roomName: String) {
        try {
            stompClient.subscribeToChatRoom(chatRoomId)
                .catch { }
                .collect { message -> handleIncomingMessage(chatRoomId, roomName, message) }
        } catch (_: Exception) {
        }
    }

    private fun handleIncomingMessage(
        chatRoomId: String,
        roomName: String,
        message: ChatMessageResponse
    ) {
        if (activeChatRoomId == chatRoomId) return

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationHelper.showMessageNotification(
            context = this,
            chatRoomId = chatRoomId,
            senderName = roomName,
            messageContent = message.content
        )
    }

    override fun onDestroy() {
        serviceScope.launch {
            try {
                stompClient.disconnect()
            } catch (_: Exception) {
            }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private class ReconnectException : Exception()
}
