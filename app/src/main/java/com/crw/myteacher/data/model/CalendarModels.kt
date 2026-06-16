package com.crw.myteacher.data.model

import com.crw.myteacher.data.remote.dto.MeetingDto
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LessonActionType {
    JOIN, DETAILS
}

data class CalendarMeeting(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val timeRange: String,
    val durationMin: Int,
    val teacherId: String,
    val status: String,
    val actionType: LessonActionType
)

fun MeetingDto.toCalendarMeeting(): CalendarMeeting {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val start = runCatching { LocalDateTime.parse(startTime, formatter) }.getOrNull()
    val end = runCatching { LocalDateTime.parse(endTime, formatter) }.getOrNull()

    val resolvedDate = start?.toLocalDate()
        ?: runCatching { LocalDate.parse(date) }.getOrNull()
        ?: LocalDate.now()

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
        id = meetingId,
        title = topic,
        date = resolvedDate,
        timeRange = timeRange,
        durationMin = durationMin,
        teacherId = owner.accountId,
        status = status,
        actionType = actionType
    )
}
