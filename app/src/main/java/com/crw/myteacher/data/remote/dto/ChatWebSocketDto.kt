package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request wysyłany przez STOMP na /app/chat.sendMessage/{chatRoomId}
 */
@Serializable
data class ChatMessageRequest(
    val content: String,
    val attachmentId: String? = null
)

/**
 * Response odbierany z subskrypcji /topic/chatroom/{chatRoomId}
 */
@Serializable
data class ChatMessageResponse(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: String,
    val attachmentId: String? = null
)

