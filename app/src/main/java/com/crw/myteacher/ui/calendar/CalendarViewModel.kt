package com.crw.myteacher.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.CalendarMeeting
import com.crw.myteacher.data.model.toCalendarMeeting
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.repository.MeetingRepository
import com.crw.myteacher.data.repository.OfferRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val allMeetings: List<CalendarMeeting> = emptyList(),
    val errorMessage: String? = null
) {
    val meetingDates: Set<LocalDate> get() = allMeetings.map { it.date }.toSet()
    val meetings: List<CalendarMeeting> get() = allMeetings.filter { it.date == selectedDate }
    val monthLabel: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("pl"))
            return currentMonth.format(formatter)
                .replaceFirstChar { it.titlecase(Locale("pl")) }
        }
}

class CalendarViewModel(
    private val meetingRepository: MeetingRepository,
    private val offerRepository: OfferRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMeetings()
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadMeetings()
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(
            currentMonth = newMonth,
            selectedDate = newMonth.atDay(1)
        )
        loadMeetings()
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(
            currentMonth = newMonth,
            selectedDate = newMonth.atDay(1)
        )
        loadMeetings()
    }

    private fun loadMeetings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = meetingRepository.getMyMeetings()
            result.onSuccess { dtos ->
                val calendarMeetings = coroutineScope {
                    dtos.map { dto ->
                        async {
                            val offerId = dto.context.offerId
                            val subjectName = offerRepository.getOfferById(offerId)
                                .getOrNull()?.subject
                            dto.toCalendarMeeting(subjectName = subjectName)
                        }
                    }.awaitAll()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allMeetings = calendarMeetings
                )
            }.onFailure { e ->
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
                    val meetingRepository = MeetingRepository(ApiClient.api)
                    val offerRepository = OfferRepository(ApiClient.api)
                    return CalendarViewModel(meetingRepository, offerRepository) as T
                }
            }
        }
    }
}
