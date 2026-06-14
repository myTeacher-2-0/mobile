package com.crw.myteacher.data.repository

import android.util.Log
import com.crw.myteacher.data.remote.ChatStompClient
import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import com.crw.myteacher.data.remote.dto.ChatRoomDto
import kotlinx.coroutines.flow.Flow

/**
 * Repozytorium chatu — łączy REST API (historia) z WebSocket STOMP (realtime).
 */
class ChatRepository(
    private val api: MyTeacherApi,
    private val stompClient: ChatStompClient
) : BaseRepository() {

    companion object {
        private const val TAG = "ChatRepository"
    }

    // ── REST API (historia wiadomości) ───────────────────────

    /**
     * Pobiera listę pokoi czatowych zalogowanego użytkownika.
     */
    suspend fun getMyChatRooms(): Result<List<ChatRoomDto>> {
        return safeApiCall { api.getMyChatRooms() }
    }

    /**
     * Pobiera historię wiadomości w pokoju (paginacja kursorem).
     *
     * @param chatRoomId ID pokoju
     * @param before Kursor — timestamp wiadomości przed którą pobrać starsze
     * @param limit Ile wiadomości pobrać (domyślnie 20)
     */
    suspend fun getMessages(
        chatRoomId: String,
        before: String? = null,
        limit: Int = 20
    ): Result<List<ChatMessageResponse>> {
        return safeApiCall {
            api.getLatestMessagesInChatRoom(chatRoomId, before, limit)
        }
    }

    // ── WebSocket STOMP (realtime) ───────────────────────────

    /**
     * Stan połączenia WebSocket.
     */
    val connectionState = stompClient.connectionState

    /**
     * Nawiązuje połączenie STOMP z serwerem.
     * Należy wywołać przed subskrypcją/wysyłaniem wiadomości.
     */
    suspend fun connectWebSocket() {
        Log.d(TAG, "connectWebSocket()")
        stompClient.connect()
    }

    /**
     * Subskrybuje nowe wiadomości w pokoju czatowym (realtime via STOMP).
     * Zwraca Flow emitujący nowe wiadomości.
     *
     * @param chatRoomId ID pokoju czatowego
     */
    suspend fun subscribeToMessages(chatRoomId: String): Flow<ChatMessageResponse> {
        Log.d(TAG, "subscribeToMessages($chatRoomId)")
        return stompClient.subscribeToChatRoom(chatRoomId)
    }

    /**
     * Wysyła wiadomość do pokoju czatowego przez STOMP.
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
        Log.d(TAG, "sendMessage($chatRoomId, content=${content.take(50)})")
        stompClient.sendMessage(chatRoomId, content, attachmentId)
    }

    /**
     * Rozłącza WebSocket.
     */
    suspend fun disconnectWebSocket() {
        Log.d(TAG, "disconnectWebSocket()")
        stompClient.disconnect()
    }
}

