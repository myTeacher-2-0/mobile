package com.crw.myteacher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.SessionManager
import com.crw.myteacher.data.remote.dto.UserDto
import com.crw.myteacher.data.repository.AccountRepository
import com.crw.myteacher.data.repository.DashboardRepository
import com.crw.myteacher.data.repository.MeetingRepository
import com.crw.myteacher.data.repository.NetworkDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val data: DashboardData = DashboardData(
        dateLabel = "",
        greetingName = "",
        progressPercent = 0,
        completedLabel = "",
        remainingMeetingsLabel = "",
        todaysLessons = emptyList(),
        subjects = emptyList()
    ),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var isLoadingInProgress = false

    fun loadDashboard() {
        if (isLoadingInProgress) return
        isLoadingInProgress = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = try {
                val dashboard = repository.getDashboard()
                HomeUiState(isLoading = false, data = dashboard)
            } catch (_: Exception) {
                emptyErrorState()
            } finally {
                isLoadingInProgress = false
            }
        }
    }

    fun loadDashboardWithUser(user: UserDto) {
        if (isLoadingInProgress) return
        isLoadingInProgress = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            SessionManager.suppressExpiredEvent = true
            _uiState.value = try {
                val dashboard = repository.getDashboardForUser(user)
                HomeUiState(isLoading = false, data = dashboard)
            } catch (_: Exception) {
                emptyErrorState()
            } finally {
                SessionManager.suppressExpiredEvent = false
                isLoadingInProgress = false
            }
        }
    }

    fun reset() {
        isLoadingInProgress = false
        _uiState.value = HomeUiState()
    }

    private fun emptyErrorState(): HomeUiState {
        val emptyData = DashboardData(
            dateLabel = "",
            greetingName = "",
            progressPercent = 0,
            completedLabel = "",
            remainingMeetingsLabel = "",
            todaysLessons = emptyList(),
            subjects = emptyList()
        )
        return HomeUiState(
            isLoading = false,
            data = emptyData,
            errorMessage = "Nie udało się pobrać danych."
        )
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val accRepo = AccountRepository(ApiClient.api)
                    val meetRepo = MeetingRepository(ApiClient.api)
                    val repository = NetworkDashboardRepository(accRepo, meetRepo)
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}
