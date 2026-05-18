package com.crw.myteacher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.ui.calendar.CalendarRoute
import com.crw.myteacher.ui.calendar.CalendarViewModel
import com.crw.myteacher.ui.home.HomeRoute
import com.crw.myteacher.ui.home.HomeViewModel
import com.crw.myteacher.ui.login.LoginRoute
import com.crw.myteacher.ui.login.LoginViewModel
import com.crw.myteacher.ui.proposelesson.ProposeLessonRoute
import com.crw.myteacher.ui.proposelesson.ProposeLessonViewModel
import com.crw.myteacher.ui.splash.SplashScreen
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

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            MyTeacherTheme(dynamicColor = false) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Splash
                ) {
                    composable<Splash> {
                        SplashScreen(
                            onSplashFinished = {
                                val isLoggedIn = ApiClient.getTokenManager().isLoggedIn
                                val destination: Any = if (isLoggedIn) Home else Login
                                navController.navigate(destination) {
                                    popUpTo(Splash) { inclusive = true }
                                }
                            }
                        )
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
                            onNavigateToRegister = { /* TODO: navigate to register */ },
                            onLoginSuccess = {
                                homeViewModel.loadDashboard()
                                navController.navigate(Home) {
                                    popUpTo(Login) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable<Home> {
                        HomeRoute(
                            uiState = uiState,
                            onRetry = homeViewModel::loadDashboard,
                            onNavigateToProposeLesson = { navController.navigate(ProposeLesson) },
                            onNavigateToCalendar = { navController.navigate(Calendar) }
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
                            onNextMonth = calendarViewModel::nextMonth
                        )
                    }
                    composable<ProposeLesson> {
                        val proposeLessonViewModel: ProposeLessonViewModel by viewModels { ProposeLessonViewModel.factory() }
                        val proposeLessonState by proposeLessonViewModel.uiState.collectAsStateWithLifecycle()
                        ProposeLessonRoute(
                            uiState = proposeLessonState,
                            onNavigateBack = { navController.popBackStack() },
                            onDateSelected = proposeLessonViewModel::selectDate,
                            onSlotSelected = proposeLessonViewModel::selectSlot,
                            onSubmit = proposeLessonViewModel::submitProposal
                        )
                    }
                }
            }
        }
    }
}
