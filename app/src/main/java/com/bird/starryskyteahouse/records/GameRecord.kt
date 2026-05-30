package com.bird.starryskyteahouse.records

data class GameRecord(
    val level: Int,
    val elapsedSeconds: Int,
    val remainingSeconds: Int,
    val completed: Boolean
)
