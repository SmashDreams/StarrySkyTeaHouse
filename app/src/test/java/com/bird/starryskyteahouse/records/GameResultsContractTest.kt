package com.bird.starryskyteahouse.records

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class GameResultsContractTest {
    @Test
    fun resultsContractExposesCreatedAtAndNewestFirstSortOrder() {
        assertEquals("com.bird.starryskysudoku.provider", GameResultsContract.AUTHORITY)
        assertEquals("content://com.bird.starryskysudoku.provider/results", GameResultsContract.Results.CONTENT_URI_STRING)
        assertEquals("created_at", GameResultsContract.Results.COLUMN_CREATED_AT)
        assertEquals("created_at DESC", GameResultsContract.Results.SORT_NEWEST_FIRST)
        assertEquals("username=?", GameResultsContract.Results.selectionForUsername())
        assertArrayEquals(
            arrayOf("username", "level", "elapsed_seconds", "remaining_seconds", "completed", "created_at"),
            GameResultsContract.Results.PROJECTION
        )
    }

    @Test
    fun resultsAuthorityIsDerivedFromGamePackageContract() {
        val source = File("src/main/java/com/bird/starryskyteahouse/records/GameResultsContract.kt").readText()

        assertEquals("${com.bird.starryskyteahouse.game.GameEntryContract.PACKAGE_NAME}.provider", GameResultsContract.AUTHORITY)
        assert(source.contains("GameEntryContract.PACKAGE_NAME"))
    }
}
