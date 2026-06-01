package com.bird.starryskyteahouse

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BundledSudokuApkSyncTaskStructureTest {
    @Test
    fun rootBuildDefinesExplicitBundledSudokuApkSyncTask() {
        val rootBuild = File("../build.gradle.kts").readText()

        assertTrue(rootBuild.contains("assembleBundledSudokuDebug"))
        assertTrue(rootBuild.contains("syncBundledSudokuApk"))
        assertTrue(rootBuild.contains("StarrySkySudoku"))
        assertTrue(rootBuild.contains("starry_sky_sudoku.apk"))
    }
}
