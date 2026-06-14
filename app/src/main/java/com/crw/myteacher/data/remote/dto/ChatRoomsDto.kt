package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomDto (
    val id: String,
    val memberIds: List<String>,
    val name: String,
    val lastMessage: ChatMessageResponse?,
)