package com.bird.StarrySkyTeaHouse.records

import android.content.ContentResolver
import android.util.Log

class GameRecordRepository(private val mContentResolver: ContentResolver) {
    fun loadForUsername(username: String): GameRecordLoadResult {
        return try {
            mContentResolver.query(
                GameResultsContract.Results.CONTENT_URI,
                GameResultsContract.Results.PROJECTION,
                GameResultsContract.Results.selectionForUsername(),
                arrayOf(username),
                GameResultsContract.Results.SORT_NEWEST_FIRST
            )?.use { cursor ->
                val records = GameRecordCursorParser.parse(cursor)
                if (records.isEmpty()) GameRecordLoadResult.Empty else GameRecordLoadResult.Records(records)
            } ?: GameRecordLoadResult.Unavailable
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Unable to load game records", exception)
            GameRecordLoadResult.Unavailable
        }
    }

    private companion object {
        const val TAG = "GameRecordRepository"
    }
}

sealed class GameRecordLoadResult {
    data object Empty : GameRecordLoadResult()
    data object Unavailable : GameRecordLoadResult()
    data class Records(val records: List<GameRecord>) : GameRecordLoadResult()
}
