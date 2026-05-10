package com.crw.myteacher.ui.proposelesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.MockTeacher
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.dto.AvailabilitySlotDto
import com.crw.myteacher.data.remote.dto.AvailabilityWeekDto
import com.crw.myteacher.data.remote.dto.OfferDto
import com.crw.myteacher.data.repository.AvailabilityRepository
import com.crw.myteacher.data.repository.OfferRepository
import com.crw.myteacher.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class ProposeLessonUiState(
    val isLoading: Boolean = true,
    val teacher: MockTeacher? = null,
    val availableSlots: Map<String, List<String>> = emptyMap(),
    val selectedDateIndex: Int = 0,
    val selectedSlot: String? = null,
    val amountToPay: Double = 0.0,
    val errorMessage: String? = null,
    val dates: List<Pair<String, String>> = emptyList(),
    val offer: OfferDto? = null,
    val availabilityWeeks: List<AvailabilityWeekDto> = emptyList()
)

class ProposeLessonViewModel(
    private val offerRepository: OfferRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProposeLessonUiState())
    val uiState: StateFlow<ProposeLessonUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val offersResult = offerRepository.getOffersList(page = 0, size = 1)
            offersResult.onSuccess { offers ->
                val offer = offers.firstOrNull()
                if (offer == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Brak dostępnych ofert."
                    )
                    return@launch
                }

                val teacher = MockTeacher(
                    name = offer.teacherName ?: "Nauczyciel",
                    role = offer.subjectName ?: "Korepetytor",
                    description = offer.teacherDescription ?: "",
                    rating = offer.teacherRating ?: 0f,
                    lessonsCount = offer.teacherLessonsCount ?: 0,
                    pricePerLesson = offer.pricePerLesson
                )

                // Generate dates for the next 7 days
                val today = LocalDate.now()
                val dates = (0..6).map { offset ->
                    val date = today.plusDays(offset.toLong())
                    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl")).uppercase()
                    dayName to date.dayOfMonth.toString()
                }

                // Load availability from AvailabilityRepository
                val teacherId = offer.teacherId
                if (teacherId != null) {
                    val availResult = availabilityRepository.getAvailabilityWeekByOwner(teacherId)

                    availResult.onSuccess { weeks ->
                        val selectedDaySlots = getSlotsForDate(weeks, today)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            teacher = teacher,
                            offer = offer,
                            dates = dates,
                            availabilityWeeks = weeks,
                            availableSlots = groupSlotsByTimeOfDay(selectedDaySlots),
                            amountToPay = offer.pricePerLesson
                        )
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            teacher = teacher,
                            offer = offer,
                            dates = dates,
                            availableSlots = emptyMap(),
                            amountToPay = offer.pricePerLesson,
                            errorMessage = "Nie udało się pobrać dostępnych terminów."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teacher = teacher,
                        offer = offer,
                        dates = dates,
                        amountToPay = offer.pricePerLesson
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Błąd pobierania ofert."
                )
            }
        }
    }

    fun selectDate(index: Int) {
        _uiState.value = _uiState.value.copy(selectedDateIndex = index, selectedSlot = null)

        val dates = _uiState.value.dates
        if (index in dates.indices) {
            val today = LocalDate.now()
            val selectedDate = today.plusDays(index.toLong())
            val slotsForDay = getSlotsForDate(_uiState.value.availabilityWeeks, selectedDate)
            _uiState.value = _uiState.value.copy(
                availableSlots = groupSlotsByTimeOfDay(slotsForDay)
            )
        }
    }

    fun selectSlot(slot: String) {
        _uiState.value = _uiState.value.copy(selectedSlot = slot)
    }

    fun submitProposal() {
        val offer = _uiState.value.offer ?: return
        val slot = _uiState.value.selectedSlot ?: return
        val teacherId = offer.teacherId ?: return
        val today = LocalDate.now()
        val selectedDate = today.plusDays(_uiState.value.selectedDateIndex.toLong())
        val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Build start/end time as ISO datetime strings
        val startTime = "${dateStr}T${slot}:00"
        val endTime = findEndTimeForSlot(slot)?.let { "${dateStr}T${it}:00" } ?: startTime

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = reservationRepository.createReservation(
                teacherId = teacherId,
                offerId = offer.id,
                startTime = startTime,
                endTime = endTime
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Nie udało się zarezerwować terminu."
                )
            }
        }
    }

    /**
     * Finds slots for a given date by matching the day of week
     * against the availability week schedule.
     */
    private fun getSlotsForDate(
        weeks: List<AvailabilityWeekDto>,
        date: LocalDate
    ): List<AvailabilitySlotDto> {
        val dayOfWeekName = date.dayOfWeek.name // "MONDAY", "TUESDAY", etc.

        return weeks.flatMap { week ->
            week.days
                .filter { it.dayOfWeek.equals(dayOfWeekName, ignoreCase = true) }
                .flatMap { it.slots }
        }
    }

    /**
     * Finds the end time for a selected startTime slot
     * based on the current availability data.
     */
    private fun findEndTimeForSlot(startTime: String): String? {
        val weeks = _uiState.value.availabilityWeeks
        for (week in weeks) {
            for (day in week.days) {
                for (slot in day.slots) {
                    if (slot.startTime == startTime) {
                        return slot.endTime
                    }
                }
            }
        }
        return null
    }

    private fun groupSlotsByTimeOfDay(slots: List<AvailabilitySlotDto>): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        for (slot in slots) {
            val hour = try {
                slot.startTime.substringBefore(":").toInt()
            } catch (_: Exception) {
                12
            }
            val section = when {
                hour < 12 -> "Rano"
                hour < 17 -> "Popołudnie"
                else -> "Wieczór"
            }
            result.getOrPut(section) { mutableListOf() }.add(slot.startTime)
        }
        return result
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val api = ApiClient.api
                    return ProposeLessonViewModel(
                        offerRepository = OfferRepository(api),
                        availabilityRepository = AvailabilityRepository(api),
                        reservationRepository = ReservationRepository(api)
                    ) as T
                }
            }
        }
    }
}
