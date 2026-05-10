package com.crw.myteacher.data.model

import com.crw.myteacher.data.remote.dto.MeetingDto
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LessonActionType {
    JOIN, DETAILS
}

data class CalendarMeeting(
    val id: Long,
    val subject: String,
    val title: String,
    val timeRange: String,
    val durationMin: Int,
    val teacherName: String,
    val status: String,
    val meetingLink: String?,
    val actionType: LessonActionType
)

fun MeetingDto.toCalendarMeeting(): CalendarMeeting {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val start = try { LocalDateTime.parse(startTime, formatter) } catch (_: Exception) { null }
    val end = try { LocalDateTime.parse(endTime, formatter) } catch (_: Exception) { null }

    val timeRange = if (start != null && end != null) {
        val tf = DateTimeFormatter.ofPattern("HH:mm")
        "${start.format(tf)} - ${end.format(tf)}"
    } else {
        "$startTime - $endTime"
    }

    val durationMin = if (start != null && end != null) {
        Duration.between(start, end).toMinutes().toInt()
    } else {
        0
    }

    val actionType = if (status.equals("SCHEDULED", ignoreCase = true) || status.equals("IN_PROGRESS", ignoreCase = true)) {
        LessonActionType.JOIN
    } else {
        LessonActionType.DETAILS
    }

    return CalendarMeeting(
        id = id,
        subject = subjectName ?: "",
        title = title ?: "Spotkanie #$id",
        timeRange = timeRange,
        durationMin = durationMin,
        teacherName = teacherName ?: "",
        status = status,
        meetingLink = meetingLink,
        actionType = actionType
    )
}

data class MockTeacher(
    val name: String,
    val role: String,
    val description: String,
    val rating: Float,
    val lessonsCount: Int,
    val pricePerLesson: Double
)
