package com.imperadorsid.runningtracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.AppScaffold
import com.imperadorsid.runningtracker.di.AppContainer
import com.imperadorsid.runningtracker.presentation.navigation.TrotClockNavGraph
import com.imperadorsid.runningtracker.presentation.theme.TrotClockTheme
import com.imperadorsid.runningtracker.service.RunTrackingService

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RunTrackingService.setRepository(container.sessionRepository)

        setContent {
            TrotClockTheme {
                AppScaffold {
                    TrotClockNavGraph(container)
                }
            }
        }
    }
}
