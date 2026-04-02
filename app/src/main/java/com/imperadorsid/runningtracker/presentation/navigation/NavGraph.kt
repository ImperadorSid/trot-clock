package com.imperadorsid.runningtracker.presentation.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.imperadorsid.runningtracker.di.AppContainer
import com.imperadorsid.runningtracker.presentation.screen.activesession.ActiveSessionScreen
import com.imperadorsid.runningtracker.presentation.screen.activesession.ActiveSessionViewModel
import com.imperadorsid.runningtracker.presentation.screen.createsession.CreateSessionScreen
import com.imperadorsid.runningtracker.presentation.screen.createsession.CreateSessionViewModel
import com.imperadorsid.runningtracker.presentation.screen.sessionlist.SessionListScreen
import com.imperadorsid.runningtracker.presentation.screen.sessionlist.SessionListViewModel
import com.imperadorsid.runningtracker.service.RunTrackingService

@Composable
fun RunningTrackerNavGraph(container: AppContainer) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Screen.SESSION_LIST
    ) {
        composable(Screen.SESSION_LIST) {
            val viewModel: SessionListViewModel = viewModel(
                factory = SessionListViewModel.factory(container.sessionRepository)
            )
            val uiState by viewModel.uiState.collectAsState()

            SessionListScreen(
                uiState = uiState,
                onCreateSession = { navController.navigate(Screen.CREATE_SESSION) },
                onStartSession = { id -> navController.navigate(Screen.activeSession(id)) },
                onDeleteSession = { id -> viewModel.deleteSession(id) }
            )
        }

        composable(Screen.CREATE_SESSION) {
            val viewModel: CreateSessionViewModel = viewModel(
                factory = CreateSessionViewModel.factory(container.sessionRepository, container.clock)
            )
            val uiState by viewModel.uiState.collectAsState()

            CreateSessionScreen(
                uiState = uiState,
                onAddPattern = { reps, walk, jog -> viewModel.addPattern(reps, walk, jog) },
                onRemovePattern = { index -> viewModel.removePattern(index) },
                onSave = { viewModel.saveSession() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ACTIVE_SESSION,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
            val context = LocalContext.current
            val viewModel: ActiveSessionViewModel = viewModel(
                factory = ActiveSessionViewModel.factory(sessionId, container.sessionRepository)
            )
            val uiState by viewModel.uiState.collectAsState()

            ActiveSessionScreen(
                uiState = uiState,
                intervalTransition = RunTrackingService.intervalTransition,
                onStart = { id, intervalsOnly ->
                    val intent = Intent(context, RunTrackingService::class.java).apply {
                        action = RunTrackingService.ACTION_START
                        putExtra(RunTrackingService.EXTRA_SESSION_ID, id)
                        putExtra(RunTrackingService.EXTRA_INTERVALS_ONLY, intervalsOnly)
                    }
                    context.startForegroundService(intent)
                },
                onPause = {
                    context.startService(
                        Intent(context, RunTrackingService::class.java).apply {
                            action = RunTrackingService.ACTION_PAUSE
                        }
                    )
                },
                onResume = {
                    context.startService(
                        Intent(context, RunTrackingService::class.java).apply {
                            action = RunTrackingService.ACTION_RESUME
                        }
                    )
                },
                onStop = {
                    context.startService(
                        Intent(context, RunTrackingService::class.java).apply {
                            action = RunTrackingService.ACTION_STOP
                        }
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
