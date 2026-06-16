package com.crw.myteacher.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.CalendarMeeting
import com.crw.myteacher.data.model.toCalendarMeeting
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.repository.MeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val allMeetings: List<CalendarMeeting> = emptyList(),
    val errorMessage: String? = null
) {
    val meetingsForSelectedDate: List<CalendarMeeting>
        get() = allMeetings.filter { it.date == selectedDate }
            .sortedBy { it.timeRange }

    val datesWithMeetings: Set<LocalDate>
        get() = allMeetings.map { it.date }.toSet()
}

class CalendarViewModel(
    private val meetingRepository: MeetingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMeetings()
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
    }

    private fun loadMeetings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            meetingRepository.getMyMeetings()
                .onSuccess { dtos ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allMeetings = dtos.map { it.toCalendarMeeting() }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Błąd pobierania spotkań."
                    )
                }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = MeetingRepository(ApiClient.api)
                    return CalendarViewModel(repository) as T
                }
            }
        }
    }
}
