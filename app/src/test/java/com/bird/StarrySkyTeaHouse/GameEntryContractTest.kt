package com.bird.StarrySkyTeaHouse

import com.bird.StarrySkyTeaHouse.game.GameEntryContract

import org.junit.Assert.assertEquals
import org.junit.Test

class GameEntryContractTest {
    @Test
    fun starrySkySudokuEntryUsesStablePackageAndBundledAsset() {
        assertEquals("com.bird.starryskysudoku", GameEntryContract.PACKAGE_NAME)
        assertEquals("starry_sky_sudoku.apk", GameEntryContract.ASSET_FILE_NAME)
        assertEquals("application/vnd.android.package-archive", GameEntryContract.APK_MIME_TYPE)
        assertEquals("com.bird.StarrySkyTeaHouse.fileprovider", GameEntryContract.FILE_PROVIDER_AUTHORITY)
    }
}
