package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.model.Lesson
import com.crw.myteacher.data.model.LessonStatus
import com.crw.myteacher.data.model.QuickAction
import com.crw.myteacher.data.model.Subject
import com.crw.myteacher.data.remote.MyTeacherApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NetworkDashboardRepository(
    private val api: MyTeacherApi
) : DashboardRepository {

    override suspend fun getDashboard(): DashboardData {
        val userResponse = api.getCurrentUser()
        val meetingsResponse = api.getMeetings(page = 0, size = 10)

        val userName = if (userResponse.isSuccessful) {
            userResponse.body()?.firstName ?: "Użytkowniku"
        } else {
            "Użytkowniku"
        }

        val meetings = if (meetingsResponse.isSuccessful) {
            meetingsResponse.body()?.content ?: emptyList()
        } else {
            emptyList()
        }

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))
        val dateLabel = today.format(dateFormatter)
            .replaceFirstChar { it.titlecase(Locale("pl")) }

        val todayMeetings = meetings.filter { dto ->
            try {
                val start = LocalDateTime.parse(dto.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                start.toLocalDate() == today
            } catch (_: Exception) {
                false
            }
        }

        val completedCount = meetings.count {
            it.status.equals("COMPLETED", ignoreCase = true)
        }
        val totalCount = meetings.size
        val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0
        val remainingCount = totalCount - completedCount

        val todaysLessons = todayMeetings.map { dto ->
            val timeRange = try {
                val start = LocalDateTime.parse(dto.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val end = LocalDateTime.parse(dto.endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val tf = DateTimeFormatter.ofPattern("HH:mm")
                "${start.format(tf)} - ${end.format(tf)}"
            } catch (_: Exception) {
                "${dto.startTime} - ${dto.endTime}"
            }

            val isJoinable = dto.status.equals("SCHEDULED", ignoreCase = true)
                    || dto.status.equals("IN_PROGRESS", ignoreCase = true)

            Lesson(
                id = dto.id.toString(),
                subjectLabel = dto.subjectName ?: "",
                timeRange = timeRange,
                teacherTitle = "Nauczyciel",
                teacherName = dto.teacherName ?: "",
                status = if (isJoinable) LessonStatus.JOINABLE else LessonStatus.UPCOMING,
                actionLabel = if (isJoinable) "DOŁĄCZ" else "SZCZEGÓŁY",
                isPrimary = isJoinable
            )
        }

        val quickActions = listOf(
            QuickAction(id = "propose", title = "Umów lekcję", iconText = "📚"),
            QuickAction(id = "calendar", title = "Kalendarz", iconText = "📅"),
            QuickAction(id = "messages", title = "Wiadomości", iconText = "💬")
        )

        return DashboardData(
            dateLabel = dateLabel,
            greetingName = userName,
            progressPercent = progressPercent,
            completedLabel = "$completedCount ukończonych",
            remainingMeetingsLabel = "$remainingCount pozostałych",
            quickActions = quickActions,
            todaysLessons = todaysLessons,
            subjects = emptyList()
        )
    }
}

