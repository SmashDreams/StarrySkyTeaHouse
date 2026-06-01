package com.bird.starryskyteahouse.records

import android.net.Uri
import com.bird.starrysky.contracts.SharedGameResultsContract

object GameResultsContract {
    const val AUTHORITY = SharedGameResultsContract.AUTHORITY
    const val CONTENT_URI_BASE = SharedGameResultsContract.CONTENT_URI_BASE

    object Results {
        const val PATH = SharedGameResultsContract.Results.PATH
        const val CONTENT_URI_STRING = SharedGameResultsContract.Results.CONTENT_URI_STRING
        val CONTENT_URI: Uri = SharedGameResultsContract.Results.CONTENT_URI

        const val COLUMN_USERNAME = SharedGameResultsContract.Results.COLUMN_USERNAME
        const val COLUMN_LEVEL = SharedGameResultsContract.Results.COLUMN_LEVEL
        const val COLUMN_ELAPSED_SECONDS = SharedGameResultsContract.Results.COLUMN_ELAPSED_SECONDS
        const val COLUMN_REMAINING_SECONDS = SharedGameResultsContract.Results.COLUMN_REMAINING_SECONDS
        const val COLUMN_COMPLETED = SharedGameResultsContract.Results.COLUMN_COMPLETED
        const val COLUMN_CREATED_AT = SharedGameResultsContract.Results.COLUMN_CREATED_AT
        const val SORT_NEWEST_FIRST = SharedGameResultsContract.Results.SORT_NEWEST_FIRST

        val PROJECTION = SharedGameResultsContract.Results.PROJECTION

        fun selectionForUsername(): String = SharedGameResultsContract.Results.selectionForUsername()
    }
}
