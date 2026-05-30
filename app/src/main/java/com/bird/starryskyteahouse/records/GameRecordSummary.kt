package com.bird.starryskyteahouse.records

data class GameRecordSummary(
    val completedCount: Int,
    val highestLevel: Int,
    val latestLevel: Int
) {
    companion object {
        fun from(records: List<GameRecord>): GameRecordSummary {
            return GameRecordSummary(
                completedCount = records.count { it.completed },
                highestLevel = records.maxOfOrNull { it.level } ?: 0,
                latestLevel = records.firstOrNull()?.level ?: 0
            )
        }
    }
}
