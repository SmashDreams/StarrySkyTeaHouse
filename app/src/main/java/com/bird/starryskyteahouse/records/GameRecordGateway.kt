package com.bird.starryskyteahouse.records

interface GameRecordGateway {
    fun loadForUsername(username: String): GameRecordLoadResult
}

class GameRecordRepositoryGateway(private val mRepository: GameRecordRepository) : GameRecordGateway {
    override fun loadForUsername(username: String): GameRecordLoadResult = mRepository.loadForUsername(username)
}
