package com.bird.StarrySkyTeaHouse.main

import com.bird.StarrySkyTeaHouse.game.GameEntryGateway
import com.bird.StarrySkyTeaHouse.game.GameInstallResult
import com.bird.StarrySkyTeaHouse.records.GameRecord
import com.bird.StarrySkyTeaHouse.records.GameRecordGateway
import com.bird.StarrySkyTeaHouse.records.GameRecordLoadResult
import com.bird.StarrySkyTeaHouse.session.SessionGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val mDispatcher = StandardTestDispatcher()
    private lateinit var mSession: FakeSessionGateway
    private lateinit var mGame: FakeGameEntryGateway
    private lateinit var mRecords: FakeGameRecordGateway

    @Before
    fun setUp() {
        Dispatchers.setMain(mDispatcher)
        mSession = FakeSessionGateway()
        mGame = FakeGameEntryGateway()
        mRecords = FakeGameRecordGateway()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshShowsLoggedOutStateWhenNoUserIsLoggedIn() = runTest(mDispatcher) {
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals(null, viewModel.uiState.value.currentUsername)
        assertEquals(RecordsUiState.LoginRequired, viewModel.uiState.value.recordsState)
    }

    @Test
    fun loginSuccessUpdatesCurrentUserAndLoadsRecords() = runTest(mDispatcher) {
        val records = listOf(GameRecord(level = 2, elapsedSeconds = 30, remainingSeconds = 570, completed = true))
        mRecords.result = GameRecordLoadResult.Records(records)
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.login("alice", "123", rememberPassword = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("alice", viewModel.uiState.value.currentUsername)
        assertEquals(RecordsUiState.Content(records, viewModel.uiState.value.recordSummary!!), viewModel.uiState.value.recordsState)
        assertEquals(MainEvent.ShowToast(MainToast.LoginSuccess), event.await())
    }

    @Test
    fun loginFailureEmitsFailureEventWithoutChangingLoggedOutState() = runTest(mDispatcher) {
        mSession.loginSucceeds = false
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.login("alice", "bad", rememberPassword = false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals(RecordsUiState.LoginRequired, viewModel.uiState.value.recordsState)
        assertEquals(MainEvent.ShowToast(MainToast.LoginFailed), event.await())
    }

    @Test
    fun logoutClearsUserAndEmitsLogoutEvent() = runTest(mDispatcher) {
        mSession.storedUsername = "alice"
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.logout()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals(RecordsUiState.LoginRequired, viewModel.uiState.value.recordsState)
        assertEquals(MainEvent.ShowToast(MainToast.LogoutSuccess), event.await())
    }

    @Test
    fun refreshUpdatesGameActionForInstalledAndMissingGame() = runTest(mDispatcher) {
        val viewModel = createViewModel()

        mGame.installed = true
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(GameEntryUiState.Installed, viewModel.uiState.value.gameEntryState)

        mGame.installed = false
        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(GameEntryUiState.NotInstalled, viewModel.uiState.value.gameEntryState)
    }



    @Test
    fun eventsAreNotReplayedToNewCollectors() = runTest(mDispatcher) {
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.openRegisterPage()
        assertEquals(MainEvent.OpenRegisterPage, event.await())

        assertTrue(viewModel.events.replayCache.isEmpty())
    }

    @Test
    fun gameActionEmitsOpenFailedWhenInstalledGameHasNoLaunchIntent() = runTest(mDispatcher) {
        mGame.installed = true
        mGame.openSucceeds = false
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.handleGameAction()
        advanceUntilIdle()

        assertEquals(MainEvent.ShowToast(MainToast.GameOpenFailed), event.await())
    }

    @Test
    fun gameActionMapsInstallFailuresToToastEvents() = runTest(mDispatcher) {
        val expectedEvents = mapOf(
            GameInstallResult.AssetMissing to MainToast.GameInstallAssetMissing,
            GameInstallResult.NoHandler to MainToast.GameInstallNoHandler,
            GameInstallResult.PermissionFailed to MainToast.GameInstallPermissionFailed
        )

        expectedEvents.forEach { (result, toast) ->
            mGame.installResult = result
            val viewModel = createViewModel()

            val event = async { viewModel.events.first() }
            viewModel.handleGameAction()
            advanceUntilIdle()

            assertEquals(MainEvent.ShowToast(toast), event.await())
        }
    }


    private fun createViewModel(): MainViewModel {
        return MainViewModel(
            mSession,
            mGame,
            mRecords,
            mDispatcher
        )
    }
}

private class FakeSessionGateway : SessionGateway {
    var storedUsername: String? = null
    var loginSucceeds = true

    override fun isLoggedIn(): Boolean = !storedUsername.isNullOrEmpty()
    override fun getCurrentUsername(): String? = storedUsername
    override fun getLastLoginUsername(): String? = storedUsername
    override fun isRememberPasswordEnabled(): Boolean = false
    override fun getRememberedPassword(): String? = null
    override fun login(username: String, password: String, rememberPassword: Boolean): Boolean {
        if (loginSucceeds) storedUsername = username
        return loginSucceeds
    }
    override fun logout() {
        storedUsername = null
    }
}

private class FakeGameEntryGateway : GameEntryGateway {
    var installed = false
    var openSucceeds = true
    var installResult: GameInstallResult = GameInstallResult.Started

    override fun isGameInstalled(): Boolean = installed
    override fun openGame(): Boolean = openSucceeds
    override fun installBundledGame(): GameInstallResult = installResult
}

private class FakeGameRecordGateway : GameRecordGateway {
    var result: GameRecordLoadResult = GameRecordLoadResult.Empty
    override fun loadForUsername(username: String): GameRecordLoadResult = result
}
