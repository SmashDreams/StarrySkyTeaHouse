package com.bird.StarrySkyTeaHouse.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bird.StarrySkyTeaHouse.game.GameEntryGateway
import com.bird.StarrySkyTeaHouse.game.GameInstallResult
import com.bird.StarrySkyTeaHouse.records.GameRecordGateway
import com.bird.StarrySkyTeaHouse.records.GameRecordLoadResult
import com.bird.StarrySkyTeaHouse.records.GameRecordSummary
import com.bird.StarrySkyTeaHouse.session.SessionGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class MainViewModel(
    private val mSessionGateway: SessionGateway,
    private val mGameEntryGateway: GameEntryGateway,
    private val mGameRecordGateway: GameRecordGateway,
    private val mIoDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val mUiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = mUiState.asStateFlow()

    private val mEvents = MutableSharedFlow<MainEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<MainEvent> = mEvents.asSharedFlow()
    private val mRecordsRequestId = AtomicInteger(0)

    fun refresh() {
        viewModelScope.launch {
            refreshLoginInputsAndGameState()
            refreshRecordsState()
        }
    }

    fun login(username: String, password: String, rememberPassword: Boolean) {
        viewModelScope.launch {
            if (mSessionGateway.isLoggedIn()) {
                emit(MainEvent.ShowToast(MainToast.AlreadyLoggedIn))
                return@launch
            }
            val success = withContext(mIoDispatcher) {
                mSessionGateway.login(username, password, rememberPassword)
            }
            if (success) {
                emit(MainEvent.ShowToast(MainToast.LoginSuccess))
                refreshLoginInputsAndGameState()
                refreshRecordsState()
            } else {
                emit(MainEvent.ShowToast(MainToast.LoginFailed))
                refreshLoginInputsAndGameState()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(mIoDispatcher) { mSessionGateway.logout() }
            emit(MainEvent.ShowToast(MainToast.LogoutSuccess))
            refreshLoginInputsAndGameState()
            refreshRecordsState()
        }
    }

    fun openRegisterPage() {
        viewModelScope.launch { emit(MainEvent.OpenRegisterPage) }
    }

    fun handleGameAction() {
        viewModelScope.launch {
            if (mGameEntryGateway.isGameInstalled()) {
                if (!mGameEntryGateway.openGame()) emit(MainEvent.ShowToast(MainToast.GameOpenFailed))
                refreshLoginInputsAndGameState()
                return@launch
            }
            when (withContext(mIoDispatcher) { mGameEntryGateway.installBundledGame() }) {
                GameInstallResult.Started -> emit(MainEvent.ShowToast(MainToast.GameInstallStarted))
                GameInstallResult.AssetMissing -> emit(MainEvent.ShowToast(MainToast.GameInstallAssetMissing))
                GameInstallResult.NoHandler -> emit(MainEvent.ShowToast(MainToast.GameInstallNoHandler))
                GameInstallResult.PermissionFailed -> emit(MainEvent.ShowToast(MainToast.GameInstallPermissionFailed))
            }
            refreshLoginInputsAndGameState()
        }
    }

    private fun refreshLoginInputsAndGameState() {
        val username = mSessionGateway.getCurrentUsername()
        mUiState.update { state ->
            state.copy(
                currentUsername = username,
                isLoggedIn = !username.isNullOrEmpty(),
                lastLoginUsername = mSessionGateway.getLastLoginUsername(),
                rememberPassword = mSessionGateway.isRememberPasswordEnabled(),
                rememberedPassword = mSessionGateway.getRememberedPassword(),
                gameEntryState = if (mGameEntryGateway.isGameInstalled()) {
                    GameEntryUiState.Installed
                } else {
                    GameEntryUiState.NotInstalled
                }
            )
        }
    }

    private suspend fun refreshRecordsState() {
        val requestId = mRecordsRequestId.incrementAndGet()
        val username = mSessionGateway.getCurrentUsername()
        if (username.isNullOrEmpty()) {
            mUiState.update { it.copy(recordsState = RecordsUiState.LoginRequired, recordSummary = null) }
            return
        }
        mUiState.update { it.copy(recordsState = RecordsUiState.Loading, recordSummary = null) }
        val result = withContext(mIoDispatcher) { mGameRecordGateway.loadForUsername(username) }
        if (requestId != mRecordsRequestId.get() || username != mSessionGateway.getCurrentUsername()) {
            refreshLoginInputsAndGameState()
            if (mSessionGateway.getCurrentUsername().isNullOrEmpty()) {
                mUiState.update { it.copy(recordsState = RecordsUiState.LoginRequired, recordSummary = null) }
            }
            return
        }
        mUiState.update { state ->
            when (result) {
                GameRecordLoadResult.Empty -> state.copy(recordsState = RecordsUiState.Empty, recordSummary = null)
                GameRecordLoadResult.Unavailable -> state.copy(recordsState = RecordsUiState.Unavailable, recordSummary = null)
                is GameRecordLoadResult.Records -> {
                    val summary = GameRecordSummary.from(result.records)
                    state.copy(recordsState = RecordsUiState.Content(result.records, summary), recordSummary = summary)
                }
            }
        }
    }

    private suspend fun emit(event: MainEvent) {
        mEvents.emit(event)
    }
}
