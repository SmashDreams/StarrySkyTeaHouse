package com.bird.starryskyteahouse.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bird.starryskyteahouse.session.RegisterSessionGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val mSessionGateway: RegisterSessionGateway,
    private val mIoDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val mEvents = MutableSharedFlow<RegisterEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RegisterEvent> = mEvents.asSharedFlow()

    fun registerAndLogin(username: String, password: String, confirmPassword: String, rememberPassword: Boolean) {
        viewModelScope.launch {
            if (password != confirmPassword) {
                emit(RegisterEvent.ShowToast(RegisterToast.PasswordMismatch))
                return@launch
            }
            val success = withContext(mIoDispatcher) {
                mSessionGateway.register(username, password, rememberPassword)
            }
            if (success) {
                emit(RegisterEvent.ShowToast(RegisterToast.RegisterSuccess))
                emit(RegisterEvent.Finish)
            } else {
                emit(RegisterEvent.ShowToast(RegisterToast.RegisterFailed))
            }
        }
    }

    private suspend fun emit(event: RegisterEvent) {
        mEvents.emit(event)
    }
}

sealed class RegisterEvent {
    data class ShowToast(val toast: RegisterToast) : RegisterEvent()
    data object Finish : RegisterEvent()
}

enum class RegisterToast {
    PasswordMismatch,
    RegisterSuccess,
    RegisterFailed
}
