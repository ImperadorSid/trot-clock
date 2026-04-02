package com.imperadorsid.runningtracker.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme

@Immutable
data class RunningColors(
    val walk: Color = Color.Unspecified,
    val jog: Color = Color.Unspecified
)

val LocalRunningColors = staticCompositionLocalOf { RunningColors() }

object RunningTheme {
    val colors: RunningColors
        @Composable
        get() = LocalRunningColors.current
}

private val DefaultRunningColors = RunningColors(
    walk = Color(0xFF4CAF50),
    jog = Color(0xFFFF9800)
)

@Composable
fun TrotClockTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRunningColors provides DefaultRunningColors) {
        MaterialTheme(
            content = content
        )
    }
}