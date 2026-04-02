package com.imperadorsid.runningtracker.presentation.navigation

object Screen {
    const val SESSION_LIST = "session_list"
    const val CREATE_SESSION = "create_session"
    const val ACTIVE_SESSION = "active_session/{sessionId}"

    fun activeSession(sessionId: Long) = "active_session/$sessionId"
}
