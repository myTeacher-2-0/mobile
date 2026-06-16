package com.crw.myteacher.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.SessionManager
import com.crw.myteacher.data.remote.TokenManager
import com.crw.myteacher.data.remote.dto.UserDto
import com.crw.myteacher.data.repository.AccountRepository
import com.crw.myteacher.data.repository.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SessionCheckResult {
    data object Loading : SessionCheckResult()
    data class Authenticated(val user: UserDto) : SessionCheckResult()
    data object Unauthenticated : SessionCheckResult()
}

class SplashViewModel(
    private val accountRepository: AccountRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionCheckResult>(SessionCheckResult.Loading)
    val sessionState: StateFlow<SessionCheckResult> = _sessionState.asStateFlow()

    private var hasValidated = false

    fun validateSession() {
        if (hasValidated) return
        hasValidated = true

        if (!tokenManager.isLoggedIn) {
            _sessionState.value = SessionCheckResult.Unauthenticated
            return
        }

        viewModelScope.launch {
            SessionManager.suppressExpiredEvent = true
            try {
                accountRepository.getCurrentUser()
                    .onSuccess { user ->
                        _sessionState.value = SessionCheckResult.Authenticated(user)
                    }
                    .onFailure { e ->
                        if (e is ApiException && (e.code == 401 || e.code == 403)) {
                            tokenManager.clear()
                        }
                        _sessionState.value = SessionCheckResult.Unauthenticated
                    }
            } finally {
                SessionManager.suppressExpiredEvent = false
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val accountRepository = AccountRepository(ApiClient.api)
                    val tokenManager = ApiClient.getTokenManager()
                    return SplashViewModel(accountRepository, tokenManager) as T
                }
            }
        }
    }
}
