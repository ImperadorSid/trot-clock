package com.imperadorsid.runningtracker.presentation.screen.sessionlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.presentation.util.formatDurationLong

@Composable
fun SessionListScreen(
    uiState: SessionListUiState,
    onCreateSession: () -> Unit,
    onStartSession: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit
) {
    when (uiState) {
        is SessionListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SessionListUiState.Success -> {
            SessionListContent(
                sessions = uiState.sessions,
                onCreateSession = onCreateSession,
                onStartSession = onStartSession,
                onDeleteSession = onDeleteSession
            )
        }
    }
}

@Composable
private fun SessionListContent(
    sessions: List<Session>,
    onCreateSession: () -> Unit,
    onStartSession: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = onCreateSession,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("New")
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = listState
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text("Sessions")
                }
            }

            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = "No sessions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                    )
                }
            } else {
                items(sessions.size) { index ->
                    val session = sessions[index]
                    SwipeToReveal(
                        primaryAction = {
                            PrimaryActionButton(
                                onClick = { onDeleteSession(session.id) },
                                icon = { Text("X") },
                                text = { Text("Delete") }
                            )
                        },
                        onSwipePrimaryAction = { onDeleteSession(session.id) }
                    ) {
                        Button(
                            onClick = { onStartSession(session.id) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(session.dateLabel) },
                            secondaryLabel = {
                                Text(formatDurationLong(session.totalDurationSeconds))
                            }
                        )
                    }
                }
            }
        }
    }
}