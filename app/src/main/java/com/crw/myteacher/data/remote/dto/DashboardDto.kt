package com.crw.myteacher.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    @SerialName("date_label") val dateLabel: String,
    @SerialName("greeting_name") val greetingName: String,
    @SerialName("progress_percent") val progressPercent: Int,
    @SerialName("completed_label") val completedLabel: String,
    @SerialName("remaining_meetings_label") val remainingMeetingsLabel: String,
    @SerialName("quick_actions") val quickActions: List<QuickActionDto>,
    @SerialName("todays_lessons") val todaysLessons: List<LessonDto>,
    @SerialName("subjects") val subjects: List<SubjectDto>
)

@Serializable
data class QuickActionDto(
    val id: String,
    val title: String,
    @SerialName("icon_text") val iconText: String
)

@Serializable
data class LessonDto(
    val id: String,
    @SerialName("subject_label") val subjectLabel: String,
    @SerialName("time_range") val timeRange: String,
    @SerialName("teacher_title") val teacherTitle: String,
    @SerialName("teacher_name") val teacherName: String,
    val status: String,
    @SerialName("action_label") val actionLabel: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

@Serializable
data class SubjectDto(
    val id: String,
    val name: String,
    @SerialName("is_selected") val isSelected: Boolean
)

