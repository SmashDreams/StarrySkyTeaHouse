package com.bird.starryskyteahouse

import android.content.Context
import com.bird.starryskyteahouse.game.AndroidGameEntryGateway
import com.bird.starryskyteahouse.game.GameEntryGateway
import com.bird.starryskyteahouse.game.GameEntryManager
import com.bird.starryskyteahouse.records.GameRecordGateway
import com.bird.starryskyteahouse.records.GameRecordRepository
import com.bird.starryskyteahouse.records.GameRecordRepositoryGateway
import com.bird.starryskyteahouse.session.RegisterSessionGateway
import com.bird.starryskyteahouse.session.SessionGateway
import com.bird.starryskyteahouse.session.SessionStore
import com.bird.starryskyteahouse.session.SessionStoreGateway

class TeaHouseAppContainer(context: Context) {
    private val mAppContext = context.applicationContext
    private val mSessionStoreGateway = SessionStoreGateway(SessionStore.getInstance(mAppContext))

    val mSessionGateway: SessionGateway = mSessionStoreGateway
    val mRegisterSessionGateway: RegisterSessionGateway = mSessionStoreGateway
    val mGameEntryGateway: GameEntryGateway = AndroidGameEntryGateway(GameEntryManager(mAppContext))
    val mGameRecordGateway: GameRecordGateway = GameRecordRepositoryGateway(
        GameRecordRepository(mAppContext.contentResolver)
    )
}
