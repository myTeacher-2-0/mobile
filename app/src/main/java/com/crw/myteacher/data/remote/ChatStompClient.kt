package com.crw.myteacher.data.remote

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

class ChatStompClient(
    private val tokenManager: TokenManager
) {
    companion object {
        private val WS_URL = BuildConfig.API_BASE_URL
            .replace("https://", "wss://api.")
            .replace("http://", "ws://api.")
            .trimEnd('/') + "/ws"

        private const val SEND_DESTINATION = "/app/chat.sendMessage"
        private const val SUBSCRIBE_DESTINATION = "/topic/room"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
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

    suspend fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        val token = tokenManager.accessToken
        if (token.isNullOrBlank()) {
            _connectionState.value = ConnectionState.ERROR
            throw IllegalStateException("No auth token available, cannot connect WebSocket")
        }

        _connectionState.value = ConnectionState.CONNECTING

        try {
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

            val wsClient = OkHttpWebSocketClient(okHttpClient)
            val stompClient = StompClient(wsClient)

            stompSession = stompClient.connect(
                url = WS_URL,
                customStompConnectHeaders = mapOf(
                    "Authorization" to "Bearer $token",
                    "X-Client-Platform" to "android"
                ),
            )
            _connectionState.value = ConnectionState.CONNECTED
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            stompSession = null
            throw e
        }
    }

    suspend fun subscribeToChatRoom(chatRoomId: String): Flow<ChatMessageResponse> {
        val session = stompSession
            ?: throw IllegalStateException("Not connected. Call connect() first.")

        val destination = "$SUBSCRIBE_DESTINATION/$chatRoomId"

        return session.subscribeText(destination).map { frame ->
            json.decodeFromString<ChatMessageResponse>(frame)
        }
    }

    suspend fun sendMessage(
        chatRoomId: String,
        content: String,
        attachmentId: String? = null
    ) {
        val session = stompSession
            ?: throw IllegalStateException("Not connected. Call connect() first.")

        val destination = "$SEND_DESTINATION/$chatRoomId"
        val request = ChatMessageRequest(content = content, attachmentId = attachmentId)
        val payload = json.encodeToString(ChatMessageRequest.serializer(), request)

        session.sendText(destination, payload)
    }

    suspend fun disconnect() {
        try {
            stompSession?.disconnect()
        } catch (_: Exception) {
        } finally {
            stompSession = null
            scope?.cancel()
            scope = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}
