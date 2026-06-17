package com.crw.myteacher.ui.splash

import android.util.Log
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
        if (hasValidated) {
            Log.d(TAG, "validateSession() SKIPPED — already validated")
            return
        }
        hasValidated = true

        if (!tokenManager.isLoggedIn) {
            Log.d(TAG, "validateSession() — no token found, marking Unauthenticated")
            _sessionState.value = SessionCheckResult.Unauthenticated
            return
        }

        Log.d(TAG, "validateSession() — token exists, calling /api/accounts/me...")
        viewModelScope.launch {
            SessionManager.suppressExpiredEvent = true
            try {
                accountRepository.getCurrentUser()
                    .onSuccess { user ->
                        Log.d(TAG, "validateSession() — SUCCESS: user=${user.firstName} (id=${user.accountId})")
                        _sessionState.value = SessionCheckResult.Authenticated(user)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "validateSession() — FAILED: code=${(e as? ApiException)?.code}, msg=${e.message}")
                        if (e is ApiException && (e.code == 401 || e.code == 403)) {
                            tokenManager.clear()
                            Log.d(TAG, "validateSession() — token cleared")
                        }
                        _sessionState.value = SessionCheckResult.Unauthenticated
                    }
            } finally {
                SessionManager.suppressExpiredEvent = false
            }
        }
    }

    companion object {
        private const val TAG = "SplashViewModel"
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
