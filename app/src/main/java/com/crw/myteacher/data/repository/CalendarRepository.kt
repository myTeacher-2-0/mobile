package com.crw.myteacher.data.repository

import com.crw.myteacher.data.model.MockLesson
import kotlinx.coroutines.delay

interface CalendarRepository {
    suspend fun getLessonsForDate(date: String): List<MockLesson>
}

class FakeCalendarRepository : CalendarRepository {
    override suspend fun getLessonsForDate(date: String): List<MockLesson> {
        return CalendarDummyData.lessons
    }
}

