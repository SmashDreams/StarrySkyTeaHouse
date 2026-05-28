package com.bird.StarrySkyTeaHouse.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bird.StarrySkyTeaHouse.session.SessionStore
import com.bird.StarrySkyTeaHouse.session.SessionStoreGateway

class RegisterViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val mAppContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(SessionStoreGateway(SessionStore(mAppContext))) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
