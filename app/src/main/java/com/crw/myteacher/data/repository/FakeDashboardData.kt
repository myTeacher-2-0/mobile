package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.model.Lesson
import com.crw.myteacher.data.model.LessonStatus
import com.crw.myteacher.data.model.QuickAction
import com.crw.myteacher.data.model.Subject

object FakeDashboardData {
    fun create(): DashboardData {
        return DashboardData(
            dateLabel = "PONIEDZIALEK, 23 PAZDZIERNIKA",
            greetingName = "Julian",
            progressPercent = 80,
            completedLabel = "ukonczone",
            remainingMeetingsLabel = "Zostaly ci tylko 2 spotkania w tym tygodniu. Tak trzymaj!",
            quickActions = listOf(
                QuickAction(id = "lesson", title = "Nowa lekcja", iconText = "+"),
                QuickAction(id = "chat", title = "Czat", iconText = "[]")
            ),
            todaysLessons = listOf(
                Lesson(
                    id = "1",
                    subjectLabel = "MATEMATYKA ROZSZERZONA",
                    timeRange = "14:00 - 15:30",
                    teacherTitle = "Nauczyciel",
                    teacherName = "Dr. Robert Nowicki",
                    status = LessonStatus.JOINABLE,
                    actionLabel = "Dolacz do zajec",
                    isPrimary = true
                ),
                Lesson(
                    id = "2",
                    subjectLabel = "FIZYKA",
                    timeRange = "16:30 - 18:00",
                    teacherTitle = "Nauczyciel",
                    teacherName = "Mgr Aneta Kowalska",
                    status = LessonStatus.UPCOMING,
                    actionLabel = "Wkrotce",
                    isPrimary = false
                )
            ),
            subjects = listOf(
                Subject(id = "math", name = "Matematyka", isSelected = true),
                Subject(id = "physics", name = "Fizyka", isSelected = false),
                Subject(id = "english", name = "Angielski", isSelected = false),
                Subject(id = "it", name = "Informatyka", isSelected = false)
            )
        )
    }
}

