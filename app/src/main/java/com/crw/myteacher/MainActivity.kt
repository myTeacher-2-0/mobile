package com.crw.myteacher

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.navigation.toRoute
import com.crw.myteacher.data.remote.ApiClient
import com.crw.myteacher.data.remote.SessionManager
import com.crw.myteacher.push.PushNotificationService
import com.crw.myteacher.ui.calendar.CalendarRoute
import com.crw.myteacher.ui.calendar.CalendarViewModel
import com.crw.myteacher.ui.home.HomeRoute
import com.crw.myteacher.ui.home.HomeViewModel
import com.crw.myteacher.ui.login.LoginRoute
import com.crw.myteacher.ui.login.LoginViewModel
import com.crw.myteacher.ui.messages.ConversationRoute
import com.crw.myteacher.ui.messages.MessagesRoute
import com.crw.myteacher.ui.profile.ProfileRoute
import com.crw.myteacher.ui.profile.ProfileViewModel
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
object Profile

@Serializable
object Messages

@Serializable
data class Conversation(val chatRoomId: String)

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.factory() }
    private val splashViewModel: SplashViewModel by viewModels { SplashViewModel.factory() }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            MyTeacherTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    SessionManager.sessionExpired.collect {
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
                            onSplashFinished = {},
                            onAnimationReady = { splashViewModel.validateSession() }
                        )

                        LaunchedEffect(sessionState) {
                            if (hasNavigated) return@LaunchedEffect
                            when (val state = sessionState) {
                                is SessionCheckResult.Authenticated -> {
                                    hasNavigated = true
                                    homeViewModel.loadDashboardWithUser(state.user)
                                    PushNotificationService.start(this@MainActivity)
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
                                SessionCheckResult.Loading -> {}
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
                                loginViewModel.resetLoginSuccess()
                                if (user != null) {
                                    homeViewModel.loadDashboardWithUser(user)
                                } else {
                                    homeViewModel.loadDashboard()
                                }
                                PushNotificationService.start(this@MainActivity)
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
                            onNavigateToCalendar = { navController.navigate(Calendar) },
                            onNavigateToProfile = { navController.navigate(Profile) },
                            onNavigateToMessages = { navController.navigate(Messages) { launchSingleTop = true } }
                        )
                    }
                    composable<Calendar> {
                        val calendarViewModel: CalendarViewModel by viewModels { CalendarViewModel.factory() }
                        val calendarState by calendarViewModel.uiState.collectAsStateWithLifecycle()
                        CalendarRoute(
                            uiState = calendarState,
                            onDateSelected = calendarViewModel::selectDate,
                            onNavigateToHome = {
                                navController.navigate(Home) {
                                    popUpTo(Home) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate(Profile) { launchSingleTop = true }
                            },
                            onNavigateToMessages = {
                                navController.navigate(Messages) { launchSingleTop = true }
                            },
                            onPreviousMonth = calendarViewModel::previousMonth,
                            onNextMonth = calendarViewModel::nextMonth
                        )
                    }
                    composable<Profile> {
                        val profileViewModel: ProfileViewModel by viewModels { ProfileViewModel.factory() }
                        val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
                        ProfileRoute(
                            uiState = profileState,
                            onNavigateToStart = {
                                navController.navigate(Home) {
                                    popUpTo(Home) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToCalendar = {
                                navController.navigate(Calendar) { launchSingleTop = true }
                            },
                            onNavigateToMessages = {
                                navController.navigate(Messages) { launchSingleTop = true }
                            },
                            onLogout = {
                                profileViewModel.logout()
                                homeViewModel.reset()
                                PushNotificationService.stop(this@MainActivity)
                                navController.navigate(Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable<Messages> {
                        MessagesRoute(
                            onNavigateToHome = {
                                navController.navigate(Home) {
                                    popUpTo(Home) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToCalendar = {
                                navController.navigate(Calendar) { launchSingleTop = true }
                            },
                            onNavigateToProfile = {
                                navController.navigate(Profile) { launchSingleTop = true }
                            },
                            onConversationClick = { chatRoomId ->
                                navController.navigate(Conversation(chatRoomId))
                            }
                        )
                    }
                    composable<Conversation> { backStackEntry ->
                        val conversation = backStackEntry.toRoute<Conversation>()

                        androidx.compose.runtime.DisposableEffect(conversation.chatRoomId) {
                            PushNotificationService.activeChatRoomId = conversation.chatRoomId
                            onDispose {
                                PushNotificationService.activeChatRoomId = null
                            }
                        }

                        ConversationRoute(
                            chatRoomId = conversation.chatRoomId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
