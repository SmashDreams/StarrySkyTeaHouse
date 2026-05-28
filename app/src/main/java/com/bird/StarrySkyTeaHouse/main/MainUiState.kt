package com.bird.StarrySkyTeaHouse.main

import com.bird.StarrySkyTeaHouse.records.GameRecord
import com.bird.StarrySkyTeaHouse.records.GameRecordSummary

data class MainUiState(
    val currentUsername: String? = null,
    val isLoggedIn: Boolean = false,
    val lastLoginUsername: String? = null,
    val rememberPassword: Boolean = false,
    val rememberedPassword: String? = null,
    val gameEntryState: GameEntryUiState = GameEntryUiState.NotInstalled,
    val recordsState: RecordsUiState = RecordsUiState.LoginRequired,
    val recordSummary: GameRecordSummary? = null
)

enum class GameEntryUiState {
    Installed,
    NotInstalled
}

sealed class RecordsUiState {
    data object LoginRequired : RecordsUiState()
    data object Loading : RecordsUiState()
    data object Empty : RecordsUiState()
    data object Unavailable : RecordsUiState()
    data class Content(val records: List<GameRecord>, val summary: GameRecordSummary) : RecordsUiState()
}
