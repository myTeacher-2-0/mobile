package com.crw.myteacher.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.MockLesson
import com.crw.myteacher.data.repository.CalendarRepository
import com.crw.myteacher.data.repository.FakeCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: String = "Dzisiaj",
    val lessons: List<MockLesson> = emptyList(),
    val errorMessage: String? = null
)

class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    // TODO: Propozycje integracji kalendarza:
    // 1. Google Calendar API — OAuth2, odczyt/zapis wydarzeń użytkownika z jego konta Google.
    //    Wymaga: google-api-services-calendar, konfiguracji OAuth w Google Cloud Console.
    // 2. Android CalendarProvider (ContentResolver) — lokalny dostęp do kalendarza systemowego
    //    bez zewnętrznych zależności, wymaga uprawnień READ_CALENDAR / WRITE_CALENDAR.
    // 3. Własny backend + Room — lekcje przechowywane w bazie lokalnej (Room) zsynchronizowane
    //    z endpointem REST; pełna kontrola nad danymi, brak zależności od zewnętrznych API.

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadDataForDate(_uiState.value.selectedDate)
    }

    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadDataForDate(date)
    }

    private fun loadDataForDate(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val lessons = repository.getLessonsForDate(date)
                _uiState.value = _uiState.value.copy(isLoading = false, lessons = lessons)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Błąd pobierania danych kalendarza."
                )
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = FakeCalendarRepository()
                    return CalendarViewModel(repository) as T
                }
            }
        }
    }
}
