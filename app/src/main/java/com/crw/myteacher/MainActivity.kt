package com.crw.myteacher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crw.myteacher.ui.home.HomeRoute
import com.crw.myteacher.ui.home.HomeViewModel
import com.crw.myteacher.ui.theme.MyTeacherTheme

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            MyTeacherTheme(dynamicColor = false) {
                HomeRoute(
                    uiState = uiState,
                    onRetry = homeViewModel::loadDashboard
                )
            }
        }
    }
}

