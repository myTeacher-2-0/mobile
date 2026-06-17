package com.crw.myteacher.data.remote.dto
import kotlinx.serialization.Serializable
@Serializable
data class MeetingDto(
    val meetingId: String,
    val topic: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val owner: MeetingMemberResponse,
    val members: List<MeetingMemberResponse> = emptyList(),
)

@Serializable
data class MeetingMemberResponse(val accountId: String, val status: String)
@Serializable
data class MeetingListResponseDto(
    val content: List<MeetingDto> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 20
)
