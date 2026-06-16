package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageRequest(
    val content: String,
    val attachmentId: String? = null
)

@Serializable
data class ChatMessageResponse(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: String,
    val attachmentId: String? = null
)
