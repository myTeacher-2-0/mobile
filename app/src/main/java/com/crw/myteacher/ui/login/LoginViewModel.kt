package com.crw.myteacher.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.TokenManager
import com.crw.myteacher.data.remote.dto.UserDto
import com.crw.myteacher.data.repository.AccountRepository
import com.crw.myteacher.data.repository.ApiException
import com.crw.myteacher.data.repository.AuthRepository
import com.crw.myteacher.utils.MyTeacherApplication
import com.google.android.recaptcha.RecaptchaAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val loggedInUser: UserDto? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false, loggedInUser = null)
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Podaj adres e-mail")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Podaj hasło")
            return
        }

        Log.d(TAG, "┌─── LOGIN PROCESS STARTED ───")
        Log.d(TAG, "│ Email: ${state.email.trim()}")
        Log.d(TAG, "│ Step 1: Requesting reCAPTCHA token...")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            MyTeacherApplication.instance.executeRecaptcha(RecaptchaAction.LOGIN)
                .addOnSuccessListener { recaptchaToken ->
                    Log.d(TAG, "│ Step 2: reCAPTCHA token received (${recaptchaToken.take(20)}...)")
                    Log.d(TAG, "│ Step 3: Calling /api/auth/login...")

                    viewModelScope.launch {
                        authRepository.login(state.email.trim(), state.password, recaptchaToken)
                            .onSuccess { authResponse ->
                                Log.d(TAG, "│ Step 4: Login API returned SUCCESS")
                                Log.d(TAG, "│   accessToken: ${authResponse.accessToken.take(20)}...")
                                Log.d(TAG, "│   refreshToken: ${authResponse.refreshToken?.take(20) ?: "null"}...")

                                tokenManager.accessToken = authResponse.accessToken
                                tokenManager.refreshToken = authResponse.refreshToken
                                Log.d(TAG, "│ Step 5: Tokens saved to TokenManager")

                                // Pobierz dane usera — JEDYNE zapytanie do account/me
                                Log.d(TAG, "│ Step 6: Fetching /api/accounts/me (single call)...")
                                val userResult = accountRepository.getCurrentUser()
                                userResult
                                    .onSuccess { user ->
                                        Log.d(TAG, "│ Step 7: User data received: ${user.firstName} (id=${user.accountId})")
                                        Log.d(TAG, "└─── LOGIN PROCESS COMPLETED SUCCESSFULLY ───")
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            isLoginSuccessful = true,
                                            loggedInUser = user
                                        )
                                    }
                                    .onFailure { e ->
                                        Log.e(TAG, "│ Step 7: FAILED to fetch user data: ${e.message}", e)
                                        Log.e(TAG, "└─── LOGIN PROCESS FAILED (user fetch) ───")
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            errorMessage = "Zalogowano, ale nie udało się pobrać danych użytkownika."
                                        )
                                    }
                            }
                            .onFailure { e ->
                                Log.e(TAG, "│ Step 4: Login API FAILED: code=${(e as? ApiException)?.code}, msg=${e.message}")
                                Log.e(TAG, "└─── LOGIN PROCESS FAILED (auth) ───")
                                val message = when {
                                    e is ApiException && e.code == 401 ->
                                        "Nieprawidłowy e-mail lub hasło"
                                    e is ApiException && e.code == 403 ->
                                        "Konto jest zablokowane"
                                    e.message?.contains("Unable to resolve host") == true ||
                                            e.message?.contains("timeout") == true ->
                                        "Brak połączenia z serwerem. Sprawdź internet."

                                    else -> e.message ?: "Wystąpił nieoczekiwany błąd"
                                }
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = message
                                )
                            }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "│ Step 2: reCAPTCHA FAILED: ${it.message}")
                    Log.e(TAG, "└─── LOGIN PROCESS FAILED (recaptcha) ───")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Weryfikacja reCAPTCHA nie powiodła się. Spróbuj ponownie."
                    )
                }
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = AuthRepository(ApiClient.api)
                    val accountRepository = AccountRepository(ApiClient.api)
                    val tokenManager = ApiClient.getTokenManager()
                    return LoginViewModel(repository, accountRepository, tokenManager) as T
                }
            }
        }
    }
}
