package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.LessonActionType
import com.crw.myteacher.data.model.MockLesson
import com.crw.myteacher.data.model.MockTeacher

object CalendarDummyData {

    val lessons = listOf(
        MockLesson(
            subject = "MATEMATYKA",
            title = "Analiza Matematyczna II",
            timeRange = "14:00 - 15:30",
            durationMin = 90,
            teacherName = "Dr. Julian Vance",
            teacherRole = "Top Expert",
            actionType = LessonActionType.JOIN
        ),
        MockLesson(
            subject = "FIZYKA",
            title = "Mechanika Kwantowa",
            timeRange = "17:00 - 18:00",
            durationMin = 60,
            teacherName = "Sarah Jenkins",
            teacherRole = "Certyfikowany",
            actionType = LessonActionType.DETAILS
        ),
        MockLesson(
            subject = "JĘZYKI",
            title = "Konwersacje Francuskie",
            timeRange = "19:30 - 20:30",
            durationMin = 60,
            teacherName = "Marc Dupont",
            teacherRole = "Native Speaker",
            actionType = LessonActionType.DETAILS
        )
    )

    val teacher = MockTeacher(
        name = "Dr. Elena Sterling",
        role = "Starszy Korepetytor",
        description = "Fizyka rozszerzona i analiza",
        rating = 4.8f,
        lessonsCount = 1240,
        pricePerLesson = 45.0
    )

    val availableSlots = mapOf(
        "RANO" to listOf("09:00 AM", "10:30 AM"),
        "PO POŁUDNIU" to listOf("01:00 PM", "03:30 PM"),
        "WIECZOREM" to listOf("06:00 PM", "07:30 PM")
    )
}
