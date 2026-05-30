package com.bird.starryskyteahouse.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bird.starryskyteahouse.TeaHouseAppContainer

class MainViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val mAppContainer = TeaHouseAppContainer(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                mAppContainer.mSessionGateway,
                mAppContainer.mGameEntryGateway,
                mAppContainer.mGameRecordGateway
            ) as T
        }
        throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
    }
}
