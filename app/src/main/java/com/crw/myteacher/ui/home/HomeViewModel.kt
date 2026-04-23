package com.crw.myteacher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.model.DashboardData
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.repository.DashboardRepository
import com.crw.myteacher.data.repository.FakeDashboardData
import com.crw.myteacher.data.repository.NetworkDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val data: DashboardData = FakeDashboardData.create(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = try {
                val dashboard = repository.getDashboard()
                HomeUiState(isLoading = false, data = dashboard)
            } catch (_: Exception) {
                HomeUiState(
                    isLoading = false,
                    data = FakeDashboardData.create(),
                    errorMessage = "Nie udalo sie pobrac danych. Pokazujemy dane lokalne."
                )
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = NetworkDashboardRepository(ApiClient.api)
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}

