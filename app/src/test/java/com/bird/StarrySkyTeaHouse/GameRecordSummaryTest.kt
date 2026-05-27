package com.bird.StarrySkyTeaHouse

import com.bird.StarrySkyTeaHouse.records.GameRecord
import com.bird.StarrySkyTeaHouse.records.GameRecordSummary

import org.junit.Assert.assertEquals
import org.junit.Test

class GameRecordSummaryTest {
    @Test
    fun summaryUsesFirstRecordAsLatestBecauseProviderSortsByCreatedAtDescending() {
        val records = listOf(
            GameRecord(level = 3, elapsedSeconds = 120, remainingSeconds = 180, completed = false),
            GameRecord(level = 2, elapsedSeconds = 88, remainingSeconds = 212, completed = true),
            GameRecord(level = 1, elapsedSeconds = 96, remainingSeconds = 204, completed = true)
        )

        val summary = GameRecordSummary.from(records)

        assertEquals(2, summary.completedCount)
        assertEquals(3, summary.highestLevel)
        assertEquals(3, summary.latestLevel)
    }
}
