package com.bird.starryskyteahouse.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import com.bird.starryskyteahouse.session.RegisterSessionGateway

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val mDispatcher = StandardTestDispatcher()
    private lateinit var mSession: FakeRegisterSessionGateway

    @Before
    fun setUp() {
        Dispatchers.setMain(mDispatcher)
        mSession = FakeRegisterSessionGateway()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerRejectsMismatchedPasswords() = runTest(mDispatcher) {
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.registerAndLogin("alice", "123", "456", rememberPassword = false)
        advanceUntilIdle()

        assertEquals(RegisterEvent.ShowToast(RegisterToast.PasswordMismatch), event.await())
        assertEquals(0, mSession.registerCalls)
    }

    @Test
    fun registerSuccessEmitsSuccessAndFinishEvents() = runTest(mDispatcher) {
        val viewModel = createViewModel()

        val events = mutableListOf<RegisterEvent>()
        val job = launch { viewModel.events.take(2).toList(events) }
        runCurrent()
        viewModel.registerAndLogin("alice", "123", "123", rememberPassword = true)
        advanceUntilIdle()

        assertEquals(listOf(RegisterEvent.ShowToast(RegisterToast.RegisterSuccess), RegisterEvent.Finish), events)
        job.cancel()
        assertEquals("alice", mSession.registeredUsername)
    }

    @Test
    fun registerFailureEmitsFailureEvent() = runTest(mDispatcher) {
        mSession.registerSucceeds = false
        val viewModel = createViewModel()

        val event = async { viewModel.events.first() }
        runCurrent()
        viewModel.registerAndLogin("alice", "123", "123", rememberPassword = false)
        advanceUntilIdle()

        assertEquals(RegisterEvent.ShowToast(RegisterToast.RegisterFailed), event.await())
    }

    private fun createViewModel(): RegisterViewModel {
        return RegisterViewModel(mSession, mDispatcher)
    }
}

private class FakeRegisterSessionGateway : RegisterSessionGateway {
    var registerSucceeds = true
    var registerCalls = 0
    var registeredUsername: String? = null

    override fun register(username: String, password: String, rememberPassword: Boolean): Boolean {
        registerCalls += 1
        registeredUsername = username
        return registerSucceeds
    }
}
