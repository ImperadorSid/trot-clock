package com.imperadorsid.runningtracker.presentation.screen.createsession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.presentation.util.formatDuration

private val DURATION_OPTIONS = listOf(30, 60, 90, 120, 150, 180, 240, 300, 360, 480, 600)

@Composable
fun CreateSessionScreen(
    uiState: CreateSessionUiState,
    onAddPattern: (reps: Int, walkDurationSeconds: Int, jogDurationSeconds: Int) -> Unit,
    onRemovePattern: (index: Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    var reps by remember { mutableIntStateOf(3) }
    var walkIndex by remember { mutableIntStateOf(1) }
    var jogIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(uiState.savedSessionId) {
        if (uiState.savedSessionId != null) onBack()
    }

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = onSave,
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(if (uiState.isSaving) "..." else "Save")
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
                    Text("New Session")
                }
            }

            // Walk duration selector
            item {
                DurationSelector(
                    label = "Walk",
                    selectedIndex = walkIndex,
                    onPrevious = { if (walkIndex > 0) walkIndex-- },
                    onNext = { if (walkIndex < DURATION_OPTIONS.size - 1) walkIndex++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }

            // Jog duration selector
            item {
                DurationSelector(
                    label = "Jog",
                    selectedIndex = jogIndex,
                    onPrevious = { if (jogIndex > 0) jogIndex-- },
                    onNext = { if (jogIndex < DURATION_OPTIONS.size - 1) jogIndex++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                )
            }

            // Reps selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactButton(onClick = { if (reps > 1) reps-- }) {
                        Text("-")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${reps}x",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CompactButton(onClick = { reps++ }) {
                        Text("+")
                    }
                }
            }

            // Add pattern button
            item {
                FilledTonalButton(
                    onClick = {
                        onAddPattern(reps, DURATION_OPTIONS[walkIndex], DURATION_OPTIONS[jogIndex])
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    label = { Text("Add Pattern") }
                )
            }

                      // Validation error
                      if (uiState.validationError != null) {
                        item {
                          Text(
                            text = uiState.validationError,
                            color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                    )
                }
            }

            // Added patterns list
            if (uiState.patterns.isNotEmpty()) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec)
                    ) {
                        Text("Patterns")
                    }
                }

                items(uiState.patterns.size) { index ->
                    PatternItem(
                        pattern = uiState.patterns[index],
                        onRemove = { onRemovePattern(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationSelector(
    label: String,
    selectedIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CompactButton(onClick = onPrevious) {
                Text("<")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatDuration(DURATION_OPTIONS[selectedIndex]),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            CompactButton(onClick = onNext) {
                Text(">")
            }
        }
    }
}

@Composable
private fun PatternItem(
    pattern: IntervalPattern,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${pattern.reps}x ${formatDuration(pattern.walkDurationSeconds)}w ${formatDuration(pattern.jogDurationSeconds)}j",
            style = MaterialTheme.typography.bodyMedium
        )
        CompactButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Text("x", style = MaterialTheme.typography.labelSmall)
        }
    }
}