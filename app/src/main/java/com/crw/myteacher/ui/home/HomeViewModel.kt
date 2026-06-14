package com.crw.myteacher.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.SessionManager
import com.crw.myteacher.data.remote.dto.UserDto
import com.crw.myteacher.data.repository.DashboardRepository
import com.crw.myteacher.data.repository.NetworkDashboardRepository
import com.crw.myteacher.data.repository.AccountRepository
import com.crw.myteacher.data.repository.MeetingRepository
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
        quickActions = emptyList(),
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
        if (isLoadingInProgress) {
            Log.d(TAG, "loadDashboard() SKIPPED — already loading")
            return
        }
        isLoadingInProgress = true
        Log.d(TAG, "loadDashboard() — will call /api/accounts/me + /api/meetings/me")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = try {
                val dashboard = repository.getDashboard()
                Log.d(TAG, "loadDashboard() — SUCCESS, greeting: ${dashboard.greetingName}")
                HomeUiState(isLoading = false, data = dashboard)
            } catch (e: Exception) {
                Log.e(TAG, "loadDashboard() — FAILED: ${e.message}", e)
                emptyErrorState()
            } finally {
                isLoadingInProgress = false
            }
        }
    }

    /**
     * Ładuje dashboard wykorzystując już pobrane dane użytkownika (z walidacji sesji lub logowania).
     * Dzięki temu nie odpytujemy ponownie /api/accounts/me.
     * suppressExpiredEvent = true — bo właśnie zweryfikowaliśmy sesję, więc ewentualny 403
     * z meetings/me to problem z uprawnieniami, NIE wygaśnięcie sesji.
     */
    fun loadDashboardWithUser(user: UserDto) {
        if (isLoadingInProgress) {
            Log.d(TAG, "loadDashboardWithUser() SKIPPED — already loading")
            return
        }
        isLoadingInProgress = true
        Log.d(TAG, "loadDashboardWithUser(${user.firstName}) — NO call to /api/accounts/me, only /api/meetings/me")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            SessionManager.suppressExpiredEvent = true
            _uiState.value = try {
                val dashboard = repository.getDashboardForUser(user)
                Log.d(TAG, "loadDashboardWithUser() — SUCCESS")
                HomeUiState(isLoading = false, data = dashboard)
            } catch (e: Exception) {
                Log.e(TAG, "loadDashboardWithUser() — FAILED: ${e.message}", e)
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
            quickActions = emptyList(),
            todaysLessons = emptyList(),
            subjects = emptyList()
        )
        return HomeUiState(
            isLoading = false,
            data = emptyData,
            errorMessage = "Nie udalo sie pobrac danych. Pokazujemy puste."
        )
    }

    companion object {
        private const val TAG = "HomeViewModel"
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
