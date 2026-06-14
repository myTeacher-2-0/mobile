package com.crw.myteacher.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.TokenManager
import com.crw.myteacher.data.remote.dto.UserDto
import com.crw.myteacher.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val accountRepository: AccountRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            accountRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    _uiState.value = ProfileUiState(isLoading = false, user = user)
                },
                onFailure = { error ->
                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Błąd ładowania profilu"
                    )
                }
            )
        }
    }

    fun logout() {
        tokenManager.clear()
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    accountRepository = AccountRepository(ApiClient.api),
                    tokenManager = ApiClient.getTokenManager()
                )
            }
        }
    }
}