package com.crw.myteacher.push

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.crw.myteacher.data.remote.ChatStompClient
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Foreground Service utrzymujący połączenie STOMP WebSocket
 * i wyświetlający powiadomienia o nowych wiadomościach.
 *
 * Subskrybuje wszystkie pokoje czatowe użytkownika i reaguje na
 * przychodzące wiadomości pokazując system notification.
 */
class PushNotificationService : Service() {

    companion object {
        private const val TAG = "PushNotificationService"
        private const val SERVICE_NOTIFICATION_ID = 9999
        private const val MAX_RECONNECT_DELAY_MS = 60_000L

        /**
         * ID pokoju czatowego, który jest aktualnie otwarty w UI.
         * Gdy ustawiony — powiadomienia z tego pokoju nie będą wyświetlane.
         */
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
        Log.d(TAG, "onCreate()")

        NotificationHelper.createNotificationChannels(this)

        // Upewnij się, że ApiClient jest zainicjalizowany
        ApiClient.init(this)
        stompClient = ChatStompClient(ApiClient.getTokenManager())

        // Uruchom jako foreground service
        ServiceCompat.startForeground(
            this,
            SERVICE_NOTIFICATION_ID,
            NotificationHelper.buildServiceNotification(this),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")

        if (!ApiClient.getTokenManager().isLoggedIn) {
            Log.w(TAG, "Użytkownik nie zalogowany — zatrzymuję serwis")
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            connectAndSubscribe()
        }

        // Restart serwisu jeśli system go zabije
        return START_STICKY
    }

    /**
     * Łączy się z WebSocket STOMP, pobiera listę pokoi i subskrybuje wiadomości.
     * W razie błędu próbuje ponownie z exponential backoff.
     */
    private suspend fun connectAndSubscribe() {
        var retryDelay = 2_000L

        while (true) {
            try {
                Log.d(TAG, "Łączenie z WebSocket...")
                stompClient.connect()

                Log.d(TAG, "Pobieranie listy pokoi czatowych...")
                val chatRooms = ApiClient.api.getMyChatRooms().body()

                if (chatRooms?.isEmpty() ?: true) {
                    Log.d(TAG, "Brak pokoi czatowych — czekam i próbuję ponownie")
                    delay(30_000)
                    continue
                }

                Log.d(TAG, "Subskrybowanie ${chatRooms.size} pokoi czatowych")

                // Subskrybuj każdy pokój w osobnym coroutine
                for (room in chatRooms) {
                    serviceScope.launch {
                        subscribeToRoom(room.id, room.name)
                    }
                }

                // Reset retry delay po udanym połączeniu
                retryDelay = 2_000L

                // Czekaj aż połączenie się zerwie
                stompClient.connectionState.collect { state ->
                    if (state == ChatStompClient.ConnectionState.DISCONNECTED ||
                        state == ChatStompClient.ConnectionState.ERROR
                    ) {
                        Log.w(TAG, "Połączenie STOMP przerwane ($state) — rekonekt")
                        throw ReconnectException()
                    }
                }
            } catch (e: ReconnectException) {
                Log.d(TAG, "Reconecting po utracie połączenia...")
            } catch (e: Exception) {
                Log.e(TAG, "Błąd połączenia: ${e.message}", e)
            }

            // Exponential backoff
            Log.d(TAG, "Ponowna próba za ${retryDelay}ms...")
            delay(retryDelay.milliseconds)
            retryDelay = (retryDelay * 2).coerceAtMost(MAX_RECONNECT_DELAY_MS)

            // Spróbuj rozłączyć przed ponownym połączeniem
            try {
                stompClient.disconnect()
            } catch (_: Exception) { }
        }
    }

    /**
     * Subskrybuje wiadomości z jednego pokoju czatowego.
     */
    private suspend fun subscribeToRoom(chatRoomId: String, roomName: String) {
        try {
            stompClient.subscribeToChatRoom(chatRoomId)
                .catch { e ->
                    Log.e(TAG, "Błąd subskrypcji pokoju $chatRoomId: ${e.message}", e)
                }
                .collect { message ->
                    handleIncomingMessage(chatRoomId, roomName, message)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Nie udało się subskrybować pokoju $chatRoomId: ${e.message}", e)
        }
    }

    /**
     * Obsługuje przychodzącą wiadomość — wyświetla powiadomienie
     * jeśli czat nie jest aktualnie otwarty.
     */
    private fun handleIncomingMessage(
        chatRoomId: String,
        roomName: String,
        message: ChatMessageResponse
    ) {
        Log.d(TAG, "Nowa wiadomość w pokoju $chatRoomId od ${message.senderId}")

        // Nie pokazuj powiadomienia jeśli ten czat jest otwarty w UI
        if (activeChatRoomId == chatRoomId) {
            Log.d(TAG, "Pokój $chatRoomId jest aktywny — pomijam powiadomienie")
            return
        }

        // Sprawdź uprawnienia do powiadomień
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Brak uprawnienia POST_NOTIFICATIONS")
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
        Log.d(TAG, "onDestroy()")
        serviceScope.launch {
            try {
                stompClient.disconnect()
            } catch (_: Exception) { }
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private class ReconnectException : Exception()
}





