package com.crw.myteacher.data.remote

import android.util.Log
import com.crw.myteacher.BuildConfig
import com.crw.myteacher.data.remote.dto.ChatMessageRequest
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.util.concurrent.TimeUnit

/**
 * Klient STOMP WebSocket do komunikacji z chatem.
 *
 * Backend Spring STOMP:
 * - WebSocket endpoint: /ws
 * - Send destination: /app/chat.sendMessage/{chatRoomId}
 * - Subscribe destination: /topic/chatroom/{chatRoomId}
 * - Auth: Bearer token w STOMP CONNECT header
 */
class ChatStompClient(
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "ChatStompClient"

        // Spring WebSocket endpoint
        private val WS_URL = BuildConfig.API_BASE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .trimEnd('/') + "/ws"

        // STOMP destinations
        private const val SEND_DESTINATION = "/app/chat.sendMessage"
        private const val SUBSCRIBE_DESTINATION = "/topic/room"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // no timeout for WebSocket
        .build()

    private var stompSession: StompSession? = null
    private var scope: CoroutineScope? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    /**
     * Nawiązuje połączenie STOMP WebSocket z serwerem.
     * Token Bearer jest wysyłany jako STOMP CONNECT header.
     */
    suspend fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            Log.d(TAG, "connect() — already connected")
            return
        }

        val token = tokenManager.accessToken
        if (token.isNullOrBlank()) {
            Log.e(TAG, "connect() — no token available, cannot connect")
            _connectionState.value = ConnectionState.ERROR
            return
        }

        _connectionState.value = ConnectionState.CONNECTING
        Log.d(TAG, "connect() — connecting to $WS_URL")

        try {
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

            val wsClient = OkHttpWebSocketClient(okHttpClient)
            val stompClient = StompClient(wsClient)

            stompSession = stompClient.connect(
                url = WS_URL,
                customStompConnectHeaders = mapOf("Authorization" to "Bearer $token")
            )
            _connectionState.value = ConnectionState.CONNECTED
            Log.d(TAG, "connect() — CONNECTED successfully")
        } catch (e: Exception) {
            Log.e(TAG, "connect() — FAILED: ${e.message}", e)
            _connectionState.value = ConnectionState.ERROR
            stompSession = null
        }
    }

    /**
     * Subskrybuje wiadomości w danym pokoju czatowym.
     * Zwraca Flow<ChatMessageResponse> z nowo przychodzącymi wiadomościami.
     *
     * @param chatRoomId ID pokoju czatowego
     * @return Flow emitujący nowe wiadomości w czasie rzeczywistym
     */
    suspend fun subscribeToChatRoom(chatRoomId: String): Flow<ChatMessageResponse> {
        val session = stompSession
            ?: throw IllegalStateException("Not connected. Call connect() first.")

        val destination = "$SUBSCRIBE_DESTINATION/$chatRoomId"
        Log.d(TAG, "subscribeToChatRoom() — subscribing to $destination")

        return session.subscribeText(destination).map { frame ->
            Log.d(TAG, "subscribeToChatRoom() — received message: ${frame.take(100)}...")
            json.decodeFromString<ChatMessageResponse>(frame)
        }
    }

    /**
     * Wysyła wiadomość do pokoju czatowego.
     *
     * @param chatRoomId ID pokoju czatowego
     * @param content Treść wiadomości
     * @param attachmentId Opcjonalny ID załącznika
     */
    suspend fun sendMessage(
        chatRoomId: String,
        content: String,
        attachmentId: String? = null
    ) {
        val session = stompSession
            ?: throw IllegalStateException("Not connected. Call connect() first.")

        val destination = "$SEND_DESTINATION/$chatRoomId"
        val request = ChatMessageRequest(
            content = content,
            attachmentId = attachmentId
        )
        val payload = json.encodeToString(ChatMessageRequest.serializer(), request)

        Log.d(TAG, "sendMessage() — sending to $destination: $payload")
        session.sendText(destination, payload)
        Log.d(TAG, "sendMessage() — sent successfully")
    }

    /**
     * Rozłącza sesję STOMP WebSocket.
     */
    suspend fun disconnect() {
        Log.d(TAG, "disconnect() — disconnecting...")
        try {
            stompSession?.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "disconnect() — error during disconnect: ${e.message}")
        } finally {
            stompSession = null
            scope?.cancel()
            scope = null
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.d(TAG, "disconnect() — DISCONNECTED")
        }
    }

    /**
     * Sprawdza czy klient jest połączony.
     */
    val isConnected: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTED
}



