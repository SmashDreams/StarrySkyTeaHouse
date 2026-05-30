package com.bird.starryskyteahouse.main

sealed class RegisterEvent {
    data class ShowToast(val toast: RegisterToast) : RegisterEvent()
    data object Finish : RegisterEvent()
}

enum class RegisterToast {
    PasswordMismatch,
    RegisterSuccess,
    RegisterFailed
}
