package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.ServiceLocator
import com.example.ui.chat.RagChatScreen
import com.example.ui.chat.RagChatViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.drawer.VerbaDrawerContent
import com.example.ui.hub.KnowledgeHubScreen
import com.example.ui.hub.KnowledgeHubViewModel
import com.example.ui.library.LibraryScreen
import kotlinx.coroutines.launch

enum class AppDestination {
    DASHBOARD,
    HUB,
    CHAT,
    LIBRARY
}

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var targetSessionId by remember { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val factory = remember { ServiceLocator.provideViewModelFactory() }
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val hubViewModel: KnowledgeHubViewModel = viewModel(factory = factory)
    val chatViewModel: RagChatViewModel = viewModel(factory = factory)

    // Back handling: If drawer open, close it; else return to Dashboard
    BackHandler(enabled = drawerState.isOpen || currentDestination != AppDestination.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            VerbaDrawerContent(
                viewModel = dashboardViewModel,
                onSelectSession = { sessionId ->
                    targetSessionId = sessionId
                    hubViewModel.selectSession(sessionId)
                    currentDestination = AppDestination.HUB
                    scope.launch { drawerState.close() }
                },
                onNewIngestionClick = {
                    currentDestination = AppDestination.DASHBOARD
                    dashboardViewModel.openTextIngest()
                    scope.launch { drawerState.close() }
                },
                onSignOutClick = {
                    scope.launch { drawerState.close() }
                    dashboardViewModel.signOut()
                    onSignOut()
                }
            )
        },
        gesturesEnabled = true,
        modifier = modifier
            .fillMaxSize()
            .testTag("main_modal_navigation_drawer")
    ) {
        when (currentDestination) {
            AppDestination.DASHBOARD -> {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onNavigateToHub = { sessionId ->
                        targetSessionId = sessionId
                        hubViewModel.selectSession(sessionId)
                        currentDestination = AppDestination.HUB
                    },
                    onNavigateToChat = { sessionId ->
                        targetSessionId = sessionId
                        chatViewModel.setActiveSession(sessionId)
                        currentDestination = AppDestination.CHAT
                    }
                )
            }

            AppDestination.HUB -> {
                KnowledgeHubScreen(
                    viewModel = hubViewModel,
                    initialSessionId = targetSessionId,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onNavigateToChat = { sessionId ->
                        targetSessionId = sessionId
                        chatViewModel.setActiveSession(sessionId)
                        currentDestination = AppDestination.CHAT
                    }
                )
            }

            AppDestination.CHAT -> {
                RagChatScreen(
                    viewModel = chatViewModel,
                    initialSessionId = targetSessionId,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }

            AppDestination.LIBRARY -> {
                LibraryScreen(
                    viewModel = dashboardViewModel,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onNavigateToHub = { sessionId ->
                        targetSessionId = sessionId
                        hubViewModel.selectSession(sessionId)
                        currentDestination = AppDestination.HUB
                    },
                    onNavigateToChat = { sessionId ->
                        targetSessionId = sessionId
                        chatViewModel.setActiveSession(sessionId)
                        currentDestination = AppDestination.CHAT
                    }
                )
            }
        }
    }
}
