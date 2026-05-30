package com.bird.starryskyteahouse.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MainScreenRendererStructureTest {
    @Test
    fun mainActivityDelegatesStateRenderingAndFormAccess() {
        val activity = File("src/main/java/com/bird/starryskyteahouse/MainActivity.kt").readText()
        val renderer = File("src/main/java/com/bird/starryskyteahouse/main/MainScreenRenderer.kt")

        assertTrue(renderer.isFile)
        assertTrue(activity.contains("MainScreenRenderer("))
        assertTrue(activity.contains("mScreenRenderer.render(state)"))
        assertTrue(activity.contains("mScreenRenderer.username"))
        assertFalse(activity.contains("private fun renderLoginInputs"))
        assertFalse(activity.contains("private fun renderCurrentUser"))
        assertFalse(activity.contains("private fun renderGameEntry"))
    }

    @Test
    fun rendererOwnsRecordsAdapterAndInputSyncing() {
        val renderer = File("src/main/java/com/bird/starryskyteahouse/main/MainScreenRenderer.kt").readText()

        assertTrue(renderer.contains("RecordsAdapter"))
        assertTrue(renderer.contains("LinearLayoutManager"))
        assertTrue(renderer.contains("syncInputText"))
        assertTrue(renderer.contains("GameEntryUiState.Installed"))
    }
}
