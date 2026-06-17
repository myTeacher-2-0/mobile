package com.crw.myteacher.data.model

data class DashboardData(
    val dateLabel: String,
    val greetingName: String,
    val progressPercent: Int,
    val completedLabel: String,
    val remainingMeetingsLabel: String,
    val todaysLessons: List<Lesson>,
    val subjects: List<Subject>
)

data class Lesson(
    val id: String,
    val subjectLabel: String,
    val timeRange: String,
    val teacherTitle: String,
    val teacherName: String,
    val status: LessonStatus,
    val actionLabel: String,
    val isPrimary: Boolean
)

enum class LessonStatus {
    JOINABLE,
    UPCOMING
}

data class Subject(
    val id: String,
    val name: String,
    val isSelected: Boolean
)

