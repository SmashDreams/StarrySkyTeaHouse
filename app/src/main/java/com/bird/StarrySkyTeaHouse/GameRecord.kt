package com.bird.StarrySkyTeaHouse

data class GameRecord(
    val level: Int,
    val elapsedSeconds: Int,
    val remainingSeconds: Int,
    val completed: Boolean
)

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
