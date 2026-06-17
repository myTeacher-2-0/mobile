package com.crw.myteacher.data.repository

import android.util.Log
import com.crw.myteacher.data.remote.ChatStompClient
import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.ChatMessageResponse
import com.crw.myteacher.data.remote.dto.ChatRoomDto
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val api: MyTeacherApi,
    private val stompClient: ChatStompClient
) : BaseRepository() {

    companion object {
        private const val TAG = "ChatRepository"
    }

    suspend fun getMyChatRooms(): Result<List<ChatRoomDto>> {
        return safeApiCall { api.getMyChatRooms() }
    }

    suspend fun getMessages(
        chatRoomId: String,
        before: String? = null,
        limit: Int = 20
    ): Result<List<ChatMessageResponse>> {
        return safeApiCall {
            api.getLatestMessagesInChatRoom(chatRoomId, before, limit)
        }
    }

    suspend fun connectWebSocket() {
        Log.d(TAG, "connectWebSocket()")
        stompClient.connect()
    }

    suspend fun subscribeToMessages(chatRoomId: String): Flow<ChatMessageResponse> {
        Log.d(TAG, "subscribeToMessages($chatRoomId)")
        return stompClient.subscribeToChatRoom(chatRoomId)
    }

    suspend fun sendMessage(
        chatRoomId: String,
        content: String,
        attachmentId: String? = null
    ) {
        Log.d(TAG, "sendMessage($chatRoomId, content=${content.take(50)})")
        stompClient.sendMessage(chatRoomId, content, attachmentId)
    }

    suspend fun disconnectWebSocket() {
        Log.d(TAG, "disconnectWebSocket()")
        stompClient.disconnect()
    }
}

