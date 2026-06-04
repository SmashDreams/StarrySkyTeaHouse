package com.bird.starryskyteahouse

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BundledSudokuApkSyncTaskStructureTest {
    @Test
    fun rootBuildDefinesExplicitBundledSudokuApkSyncTask() {
        val rootBuild = File("../build.gradle.kts").readText()

        assertTrue(rootBuild.contains("assembleBundledSudokuRelease"))
        assertTrue(rootBuild.contains("assembleRelease"))
        assertTrue(rootBuild.contains("app/build/outputs/apk/release/app-release.apk"))
        assertTrue(rootBuild.contains("syncBundledSudokuApk"))
        assertTrue(rootBuild.contains("StarrySkySudoku"))
        assertTrue(rootBuild.contains("starry_sky_sudoku.apk"))
    }

    @Test
    fun sudokuReleaseBuildReadsLocalSigningProperties() {
        val sudokuAppBuild = File("../../StarrySkySudoku/app/build.gradle").readText()

        assertTrue(sudokuAppBuild.contains("signingConfigs"))
        assertTrue(sudokuAppBuild.contains("RELEASE_STORE_FILE"))
        assertTrue(sudokuAppBuild.contains("RELEASE_STORE_PASSWORD"))
        assertTrue(sudokuAppBuild.contains("RELEASE_KEY_ALIAS"))
        assertTrue(sudokuAppBuild.contains("RELEASE_KEY_PASSWORD"))
        assertTrue(sudokuAppBuild.contains("signingConfig signingConfigs.release"))
    }
}
