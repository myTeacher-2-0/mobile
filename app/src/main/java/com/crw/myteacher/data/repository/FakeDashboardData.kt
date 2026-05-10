package com.crw.myteacher.data.repository
import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.model.QuickAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
object FakeDashboardData {
    fun create(): DashboardData {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("pl"))
        val dateLabel = today.format(dateFormatter)
            .replaceFirstChar { it.titlecase(Locale("pl")) }
        return DashboardData(
            dateLabel = dateLabel,
            greetingName = "Użytkowniku",
            progressPercent = 0,
            completedLabel = "0 ukończonych",
            remainingMeetingsLabel = "0 pozostałych",
            quickActions = listOf(
                QuickAction(id = "propose", title = "Umów lekcję", iconText = "\uD83D\uDCDA"),
                QuickAction(id = "calendar", title = "Kalendarz", iconText = "\uD83D\uDCC5"),
                QuickAction(id = "messages", title = "Wiadomości", iconText = "\uD83D\uDCAC")
            ),
            todaysLessons = emptyList(),
            subjects = emptyList()
        )
    }
}
