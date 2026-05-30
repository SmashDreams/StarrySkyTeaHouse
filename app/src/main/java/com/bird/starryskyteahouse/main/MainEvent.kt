package com.bird.starryskyteahouse.main

sealed class MainEvent {
    data class ShowToast(val toast: MainToast) : MainEvent()
    data object OpenRegisterPage : MainEvent()
}

enum class MainToast {
    AlreadyLoggedIn,
    LoginSuccess,
    LoginFailed,
    LogoutSuccess,
    GameOpenFailed,
    GameInstallStarted,
    GameInstallAssetMissing,
    GameInstallNoHandler,
    GameInstallPermissionFailed
}
