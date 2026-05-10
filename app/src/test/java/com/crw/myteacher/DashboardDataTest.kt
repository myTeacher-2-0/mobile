package com.crw.myteacher

import com.crw.myteacher.data.repository.FakeDashboardData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardDataTest {

    @Test
    fun fakeDashboard_containsEssentialSections() {
        val data = FakeDashboardData.create()

        assertTrue(data.quickActions.isNotEmpty())
        assertEquals(3, data.quickActions.size)
        assertEquals("Użytkowniku", data.greetingName)
        assertEquals(0, data.progressPercent)
    }
}
