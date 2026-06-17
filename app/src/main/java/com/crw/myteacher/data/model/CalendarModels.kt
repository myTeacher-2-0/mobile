package com.crw.myteacher.data.model

import com.crw.myteacher.data.remote.dto.MeetingDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LessonActionType {
    JOIN, DETAILS
}

data class CalendarMeeting(
    val id: String,
    val title: String,
    val timeRange: String,
    val teacherId: String,
    val status: String,
    val actionType: LessonActionType,
    val date: java.time.LocalDate
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

    val lessonDate = try {
        java.time.LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
        start?.toLocalDate() ?: java.time.LocalDate.now()
    }

    val actionType = if (status.equals("SCHEDULED", ignoreCase = true) || status.equals("IN_PROGRESS", ignoreCase = true)) {
        LessonActionType.JOIN
    } else {
        LessonActionType.DETAILS
    }

    return CalendarMeeting(
        id = meetingId,
        title = topic,
        timeRange = timeRange,
        teacherId = owner.accountId,
        status = status,
        actionType = actionType,
        date = lessonDate
    )
}
