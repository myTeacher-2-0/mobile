package com.crw.myteacher

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.SessionManager
import com.crw.myteacher.ui.calendar.CalendarRoute
import com.crw.myteacher.ui.calendar.CalendarViewModel
import com.crw.myteacher.ui.chat.ChatListRoute
import com.crw.myteacher.ui.chat.ChatListViewModel
import com.crw.myteacher.ui.home.HomeRoute
import com.crw.myteacher.ui.home.HomeViewModel
import com.crw.myteacher.ui.login.LoginRoute
import com.crw.myteacher.ui.login.LoginViewModel
import com.crw.myteacher.ui.splash.SessionCheckResult
import com.crw.myteacher.ui.splash.SplashScreen
import com.crw.myteacher.ui.splash.SplashViewModel
import com.crw.myteacher.ui.theme.MyTeacherTheme
import kotlinx.serialization.Serializable


@Serializable
object Splash

@Serializable
object Login

@Serializable
object Home

@Serializable
object Calendar

@Serializable
object ProposeLesson

@Serializable
object ChatList

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.factory() }
    private val splashViewModel: SplashViewModel by viewModels { SplashViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            MyTeacherTheme(dynamicColor = false) {
                val navController = rememberNavController()

                // Nasłuchuj na wygaśnięcie sesji — przekieruj na Login
                LaunchedEffect(Unit) {
                    SessionManager.sessionExpired.collect {
                        Log.w("MainActivity", "⚠ Received sessionExpired event → showing toast + navigating to Login")
                        Toast.makeText(
                            this@MainActivity,
                            "Sesja wygasła. Zaloguj się ponownie.",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate(Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Splash
                ) {
                    composable<Splash> {
                        val sessionState by splashViewModel.sessionState.collectAsStateWithLifecycle()
                        var hasNavigated by rememberSaveable { mutableStateOf(false) }

                        SplashScreen(
                            onSplashFinished = {
                                // Nawigacja nastąpi po zakończeniu walidacji
                            },
                            onAnimationReady = {
                                splashViewModel.validateSession()
                            }
                        )

                        // Gdy walidacja się zakończy — nawiguj (tylko raz)
                        LaunchedEffect(sessionState) {
                            if (hasNavigated) return@LaunchedEffect
                            when (val state = sessionState) {
                                is SessionCheckResult.Authenticated -> {
                                    hasNavigated = true
                                    homeViewModel.loadDashboardWithUser(state.user)
                                    navController.navigate(Home) {
                                        popUpTo(Splash) { inclusive = true }
                                    }
                                }
                                is SessionCheckResult.Unauthenticated -> {
                                    hasNavigated = true
                                    navController.navigate(Login) {
                                        popUpTo(Splash) { inclusive = true }
                                    }
                                }
                                SessionCheckResult.Loading -> { /* czekamy */ }
                            }
                        }
                    }
                    composable<Login> {
                        val loginViewModel: LoginViewModel by viewModels { LoginViewModel.factory() }
                        val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
                        LoginRoute(
                            uiState = loginState,
                            onEmailChange = loginViewModel::onEmailChange,
                            onPasswordChange = loginViewModel::onPasswordChange,
                            onTogglePasswordVisibility = loginViewModel::togglePasswordVisibility,
                            onLogin = loginViewModel::login,
                            onLoginSuccess = {
                                val user = loginState.loggedInUser
                                Log.d("MainActivity", "onLoginSuccess: user=${user?.firstName}, id=${user?.accountId}")
                                loginViewModel.resetLoginSuccess()
                                if (user != null) {
                                    // Używamy danych usera pobranych podczas logowania
                                    // — BEZ ponownego zapytania do /api/accounts/me
                                    homeViewModel.loadDashboardWithUser(user)
                                } else {
                                    Log.w("MainActivity", "onLoginSuccess: user is NULL, falling back to loadDashboard()")
                                    homeViewModel.loadDashboard()
                                }
                                navController.navigate(Home) {
                                    popUpTo(Login) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable<Home> {
                        val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                        HomeRoute(
                            uiState = uiState,
                            onRetry = homeViewModel::loadDashboard,
                            onNavigateToProposeLesson = { navController.navigate(ProposeLesson) },
                            onNavigateToCalendar = { navController.navigate(Calendar) },
                            onNavigateToChat = { navController.navigate(ChatList) }
                        )
                    }
                    composable<Calendar> {
                        val calendarViewModel: CalendarViewModel by viewModels { CalendarViewModel.factory() }
                        val calendarState by calendarViewModel.uiState.collectAsStateWithLifecycle()
                        CalendarRoute(
                            uiState = calendarState,
                            onDateSelected = calendarViewModel::selectDate,
                            onNavigateBack = { navController.popBackStack() },
                            onProposeLessonClick = { navController.navigate(ProposeLesson) },
                            onPreviousMonth = calendarViewModel::previousMonth,
                            onNextMonth = calendarViewModel::nextMonth,
                            onNavigateToChat = { navController.navigate(ChatList) }
                        )
                    }
                    composable<ChatList> {
                        val chatListViewModel: ChatListViewModel by viewModels { ChatListViewModel.factory() }
                        val chatListState by chatListViewModel.uiState.collectAsStateWithLifecycle()
                        ChatListRoute(
                            uiState = chatListState,
                            onNavigateBack = { navController.popBackStack() },
                            onChatRoomClick = { chatRoomId ->
                                // TODO: nawigacja do widoku konwersacji
                            },
                            onRetry = chatListViewModel::loadChatRooms
                        )
                    }
                }
            }
        }
    }
}
