package com.crw.myteacher.data.remote.dto
import kotlinx.serialization.Serializable
@Serializable
data class DashboardDto(
    val dateLabel: String,
    val greetingName: String,
    val progressPercent: Int,
    val completedLabel: String,
    val remainingMeetingsLabel: String,
    val quickActions: List<QuickActionDto>,
    val todaysLessons: List<LessonDto>,
    val subjects: List<SubjectDto>
)
@Serializable
data class QuickActionDto(
    val id: String,
    val title: String,
    val iconText: String
)
@Serializable
data class LessonDto(
    val id: String,
    val subjectLabel: String,
    val timeRange: String,
    val teacherTitle: String,
    val teacherName: String,
    val status: String,
    val actionLabel: String,
    val isPrimary: Boolean
)
@Serializable
data class SubjectDto(
    val id: String,
    val name: String,
    val isSelected: Boolean
)
