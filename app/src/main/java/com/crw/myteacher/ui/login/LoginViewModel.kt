package com.crw.myteacher.ui.login

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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            MyTeacherApplication.instance.executeRecaptcha(RecaptchaAction.LOGIN)
                .addOnSuccessListener { recaptchaToken ->
                    viewModelScope.launch {
                        authRepository.login(state.email.trim(), state.password, recaptchaToken)
                            .onSuccess { authResponse ->
                                tokenManager.accessToken = authResponse.accessToken
                                tokenManager.refreshToken = authResponse.refreshToken

                                accountRepository.getCurrentUser()
                                    .onSuccess { user ->
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            isLoginSuccessful = true,
                                            loggedInUser = user
                                        )
                                    }
                                    .onFailure {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            errorMessage = "Zalogowano, ale nie udało się pobrać danych użytkownika."
                                        )
                                    }
                            }
                            .onFailure { e ->
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Weryfikacja reCAPTCHA nie powiodła się. Spróbuj ponownie."
                    )
                }
        }
    }

    companion object {
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
