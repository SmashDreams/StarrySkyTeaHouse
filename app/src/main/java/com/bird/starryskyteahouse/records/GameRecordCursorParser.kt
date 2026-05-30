package com.bird.starryskyteahouse.records

import android.database.Cursor

class MissingGameRecordColumnException(val columnName: String) : IllegalArgumentException("Missing game record column: $columnName")

object GameRecordCursorParser {
    fun parse(cursor: Cursor): List<GameRecord> {
        val levelIndex = requireColumn(cursor, GameResultsContract.Results.COLUMN_LEVEL)
        val elapsedIndex = requireColumn(cursor, GameResultsContract.Results.COLUMN_ELAPSED_SECONDS)
        val remainingIndex = requireColumn(cursor, GameResultsContract.Results.COLUMN_REMAINING_SECONDS)
        val completedIndex = requireColumn(cursor, GameResultsContract.Results.COLUMN_COMPLETED)
        requireColumn(cursor, GameResultsContract.Results.COLUMN_USERNAME)
        requireColumn(cursor, GameResultsContract.Results.COLUMN_CREATED_AT)

        val records = mutableListOf<GameRecord>()
        while (cursor.moveToNext()) {
            records += GameRecord(
                level = cursor.getInt(levelIndex),
                elapsedSeconds = cursor.getInt(elapsedIndex),
                remainingSeconds = cursor.getInt(remainingIndex),
                completed = cursor.getInt(completedIndex) == 1
            )
        }
        return records
    }

    private fun requireColumn(cursor: Cursor, columnName: String): Int {
        val index = cursor.getColumnIndex(columnName)
        if (index < 0) throw MissingGameRecordColumnException(columnName)
        return index
    }
}
