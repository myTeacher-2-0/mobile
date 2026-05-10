package com.crw.myteacher.data.remote.dto
import kotlinx.serialization.Serializable
@Serializable
data class MeetingDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val startTime: String,
    val endTime: String,
    val status: String,
    val meetingLink: String? = null,
    val teacherId: Long? = null,
    val teacherName: String? = null,
    val studentId: Long? = null,
    val studentName: String? = null,
    val subjectName: String? = null,
    val notes: String? = null,
    val createdAt: String? = null
)
@Serializable
data class MeetingListResponseDto(
    val content: List<MeetingDto> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val size: Int = 20
)
