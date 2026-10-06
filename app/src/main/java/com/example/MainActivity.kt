package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.ServiceLocator
import com.example.ui.MainScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme

enum class RootScreen {
    SPLASH,
    AUTH,
    MAIN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ SplashScreen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Initialize DI & Repository Singletons
        ServiceLocator.initialize(this)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigator()
                }
            }
        }
    }
}

@Composable
fun RootNavigator() {
    var currentScreen by remember { mutableStateOf(RootScreen.SPLASH) }
    val factory = remember { ServiceLocator.provideViewModelFactory() }
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    when (currentScreen) {
        RootScreen.SPLASH -> {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToMain = { currentScreen = RootScreen.MAIN },
                onNavigateToAuth = { currentScreen = RootScreen.AUTH }
            )
        }

        RootScreen.AUTH -> {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = { currentScreen = RootScreen.MAIN }
            )
        }

        RootScreen.MAIN -> {
            MainScreen(
                onSignOut = { currentScreen = RootScreen.AUTH }
            )
        }
    }
}
