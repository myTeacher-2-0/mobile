package com.crw.myteacher.ui.proposelesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.MockTeacher
import com.crw.myteacher.data.repository.CalendarDummyData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProposeLessonUiState(
    val isLoading: Boolean = true,
    val teacher: MockTeacher? = null,
    val availableSlots: Map<String, List<String>> = emptyMap(),
    val selectedDateIndex: Int = 2, // Default selected day
    val selectedSlot: String? = null,
    val amountToPay: Double = 0.0,
    val errorMessage: String? = null
)

class ProposeLessonViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProposeLessonUiState())
    val uiState: StateFlow<ProposeLessonUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(300) // fake delay

            val teacher = CalendarDummyData.teacher
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                teacher = teacher,
                availableSlots = CalendarDummyData.availableSlots,
                amountToPay = teacher.pricePerLesson
            )
        }
    }

    fun selectDate(index: Int) {
        _uiState.value = _uiState.value.copy(selectedDateIndex = index, selectedSlot = null)
    }

    fun selectSlot(slot: String) {
        _uiState.value = _uiState.value.copy(selectedSlot = slot)
    }

    fun submitProposal() {
        // Here normally would call an API
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProposeLessonViewModel() as T
                }
            }
        }
    }
}
