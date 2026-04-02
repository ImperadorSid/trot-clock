package com.imperadorsid.runningtracker.presentation.screen.activesession

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.presentation.theme.RunningTheme
import com.imperadorsid.runningtracker.presentation.util.formatDuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ActiveSessionScreen(
    uiState: ActiveSessionUiState,
    intervalTransition: Flow<IntervalType> = emptyFlow(),
    onStart: (Long) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit
) {
    when (uiState) {
        is ActiveSessionUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ActiveSessionUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Session not found")
            }
        }
        is ActiveSessionUiState.Ready -> {
            ReadyContent(
                state = uiState,
                intervalTransition = intervalTransition,
                onStart = { onStart(uiState.session.id) },
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun ReadyContent(
    state: ActiveSessionUiState.Ready,
    intervalTransition: Flow<IntervalType>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        intervalTransition.collectLatest {
            vibrate(context)
        }
    }

    ScreenScaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state.timerState) {
                is TimerState.Idle -> IdleContent(state, onStart)
                is TimerState.Running -> RunningContent(state.timerState, onPause, onResume, onStop)
                is TimerState.Completed -> CompletedContent(onBack)
            }
        }
    }
}

@Composable
private fun IdleContent(
    state: ActiveSessionUiState.Ready,
    onStart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.session.dateLabel,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatDuration(state.session.totalDurationSeconds),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onStart,
            label = { Text("Start") }
        )
    }
}

@Composable
private fun RunningContent(
    timerState: TimerState.Running,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val phaseLabel = when (timerState.currentStep.phase) {
        TimerPhase.WARMUP -> "WARMUP"
        TimerPhase.ACTIVE -> timerState.currentStep.type.name
        TimerPhase.COOLDOWN -> "COOLDOWN"
    }

    val phaseColor = when {
        timerState.currentStep.phase != TimerPhase.ACTIVE -> MaterialTheme.colorScheme.onSurface
        timerState.currentStep.type == IntervalType.WALK -> RunningTheme.colors.walk
        else -> RunningTheme.colors.jog
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.titleLarge,
            color = phaseColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatDuration(timerState.remainingSeconds),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Total ${formatDuration(timerState.totalRemainingSeconds)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (timerState.isPaused) {
            Text(
                text = "PAUSED",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (timerState.isPaused) {
                Button(
                    onClick = onResume,
                    label = { Text("Resume") }
                )
            } else {
                FilledTonalButton(
                    onClick = onPause,
                    label = { Text("Pause") }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onStop,
                label = { Text("Stop") }
            )
        }
    }
}

@Composable
private fun CompletedContent(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Done!",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBack,
            label = { Text("Back") }
        )
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
}
