package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.model.Lesson
import com.crw.myteacher.data.model.LessonStatus
import com.crw.myteacher.data.model.QuickAction
import com.crw.myteacher.data.model.Subject
import com.crw.myteacher.data.remote.MyTeacherApi
import com.crw.myteacher.data.remote.dto.DashboardDto

class NetworkDashboardRepository(
    private val api: MyTeacherApi
) : DashboardRepository {

    override suspend fun getDashboard(): DashboardData {
        val response = api.getDashboard()
        return response.toDomain()
    }
}

private fun DashboardDto.toDomain(): DashboardData {
    return DashboardData(
        dateLabel = dateLabel,
        greetingName = greetingName,
        progressPercent = progressPercent,
        completedLabel = completedLabel,
        remainingMeetingsLabel = remainingMeetingsLabel,
        quickActions = quickActions.map {
            QuickAction(
                id = it.id,
                title = it.title,
                iconText = it.iconText
            )
        },
        todaysLessons = todaysLessons.map {
            Lesson(
                id = it.id,
                subjectLabel = it.subjectLabel,
                timeRange = it.timeRange,
                teacherTitle = it.teacherTitle,
                teacherName = it.teacherName,
                status = if (it.status.equals("joinable", ignoreCase = true)) {
                    LessonStatus.JOINABLE
                } else {
                    LessonStatus.UPCOMING
                },
                actionLabel = it.actionLabel,
                isPrimary = it.isPrimary
            )
        },
        subjects = subjects.map {
            Subject(
                id = it.id,
                name = it.name,
                isSelected = it.isSelected
            )
        }
    )
}

