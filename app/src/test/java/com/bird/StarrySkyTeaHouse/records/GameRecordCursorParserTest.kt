package com.bird.StarrySkyTeaHouse.records

import android.database.MatrixCursor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameRecordCursorParserTest {
    @Test
    fun parseRecordsReadsRequiredColumnsIncludingCreatedAt() {
        val cursor = MatrixCursor(GameResultsContract.Results.PROJECTION).apply {
            addRow(arrayOf("alice", 2, 33, 567, 1, 1_800_000_000_000L))
        }

        val records = GameRecordCursorParser.parse(cursor)

        assertEquals(listOf(GameRecord(level = 2, elapsedSeconds = 33, remainingSeconds = 567, completed = true)), records)
    }

    @Test
    fun parseRecordsThrowsWhenRequiredColumnIsMissing() {
        val cursor = MatrixCursor(arrayOf("username", "level", "elapsed_seconds", "remaining_seconds", "completed")).apply {
            addRow(arrayOf("alice", 2, 33, 567, 1))
        }

        val exception = assertThrows(MissingGameRecordColumnException::class.java) {
            GameRecordCursorParser.parse(cursor)
        }

        assertEquals("created_at", exception.columnName)
    }
}
