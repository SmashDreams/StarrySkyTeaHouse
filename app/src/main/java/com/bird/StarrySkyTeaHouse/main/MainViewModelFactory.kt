package com.bird.StarrySkyTeaHouse.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bird.StarrySkyTeaHouse.game.AndroidGameEntryGateway
import com.bird.StarrySkyTeaHouse.game.GameEntryManager
import com.bird.StarrySkyTeaHouse.records.GameRecordRepository
import com.bird.StarrySkyTeaHouse.records.GameRecordRepositoryGateway
import com.bird.StarrySkyTeaHouse.session.SessionStore
import com.bird.StarrySkyTeaHouse.session.SessionStoreGateway

class MainViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val mAppContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                SessionStoreGateway(SessionStore(mAppContext)),
                AndroidGameEntryGateway(GameEntryManager(mAppContext)),
                GameRecordRepositoryGateway(GameRecordRepository(mAppContext.contentResolver))
            ) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
