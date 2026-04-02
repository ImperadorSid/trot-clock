package com.imperadorsid.runningtracker.presentation.screen.createsession

import com.imperadorsid.runningtracker.domain.model.IntervalPattern

data class CreateSessionUiState(
    val patterns: List<IntervalPattern> = emptyList(),
    val isSaving: Boolean = false,
    val validationError: String? = null,
    val savedSessionId: Long? = null
)