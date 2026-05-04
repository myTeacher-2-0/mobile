package com.crw.myteacher.data.model

enum class LessonActionType {
    JOIN, DETAILS
}

data class MockLesson(
    val subject: String,
    val title: String,
    val timeRange: String,
    val durationMin: Int,
    val teacherName: String,
    val teacherRole: String,
    val actionType: LessonActionType
)

data class MockTeacher(
    val name: String,
    val role: String,
    val description: String,
    val rating: Float,
    val lessonsCount: Int,
    val pricePerLesson: Double
)

