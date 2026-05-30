package com.bird.starryskyteahouse.records

import android.net.Uri
import com.bird.starryskyteahouse.game.GameEntryContract

object GameResultsContract {
    const val AUTHORITY = "${GameEntryContract.PACKAGE_NAME}.provider"
    const val CONTENT_URI_BASE = "content://$AUTHORITY"

    object Results {
        const val PATH = "results"
        const val CONTENT_URI_STRING = "$CONTENT_URI_BASE/$PATH"
        val CONTENT_URI: Uri = Uri.parse(CONTENT_URI_STRING)

        const val COLUMN_USERNAME = "username"
        const val COLUMN_LEVEL = "level"
        const val COLUMN_ELAPSED_SECONDS = "elapsed_seconds"
        const val COLUMN_REMAINING_SECONDS = "remaining_seconds"
        const val COLUMN_COMPLETED = "completed"
        const val COLUMN_CREATED_AT = "created_at"
        const val SORT_NEWEST_FIRST = "$COLUMN_CREATED_AT DESC"

        val PROJECTION = arrayOf(
            COLUMN_USERNAME,
            COLUMN_LEVEL,
            COLUMN_ELAPSED_SECONDS,
            COLUMN_REMAINING_SECONDS,
            COLUMN_COMPLETED,
            COLUMN_CREATED_AT
        )

        fun selectionForUsername(): String = "$COLUMN_USERNAME=?"
    }
}
