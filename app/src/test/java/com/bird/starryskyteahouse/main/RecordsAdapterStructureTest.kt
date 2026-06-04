package com.bird.starryskyteahouse.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RecordsAdapterStructureTest {
    @Test
    fun mainActivityUsesRecyclerViewForRecords() {
        val layout = File("src/main/res/layout/activity_main.xml").readText()
        val activity = File("src/main/java/com/bird/starryskyteahouse/MainActivity.kt").readText()
        val renderer = File("src/main/java/com/bird/starryskyteahouse/main/MainScreenRenderer.kt").readText()

        assertTrue(layout.contains("androidx.recyclerview.widget.RecyclerView"))
        assertTrue(renderer.contains("RecordsAdapter"))
        assertTrue(activity.contains("MainScreenRenderer"))
        assertFalse(activity.contains("RecordsRenderer"))
        assertFalse(File("src/main/java/com/bird/starryskyteahouse/main/RecordsRenderer.kt").exists())
    }

    @Test
    fun recordsAdapterOwnsRecordRendering() {
        val adapter = File("src/main/java/com/bird/starryskyteahouse/main/RecordsAdapter.kt")

        assertTrue(adapter.isFile)
        assertTrue(adapter.readText().contains("ListAdapter<RecordsListItem"))
    }

    @Test
    fun recordsMessageCardKeepsFullWidthLikeOriginalContainer() {
        val adapter = File("src/main/java/com/bird/starryskyteahouse/main/RecordsAdapter.kt").readText()

        assertTrue(adapter.contains("applyFullWidthRecordItemLayout"))
        assertTrue(adapter.contains("RecyclerView.LayoutParams.MATCH_PARENT"))
        assertTrue(adapter.contains("RecyclerView.LayoutParams.WRAP_CONTENT"))
    }

    @Test
    fun recordsContentUsesWideSummaryPanelAndReadableRecordRows() {
        val adapter = File("src/main/java/com/bird/starryskyteahouse/main/RecordsAdapter.kt").readText()

        assertTrue(adapter.contains("createSummaryPanel"))
        assertTrue(adapter.contains("createSummaryDetail"))
        assertTrue(adapter.contains("createRecordInfoLine"))
        assertTrue(adapter.contains("bg_records_panel_solid"))
        assertTrue(adapter.contains("bg_record_status_complete"))
        assertTrue(adapter.contains("bg_record_status_pending"))
        assertTrue(adapter.contains("setSingleLine(true)"))
        assertFalse(adapter.contains("createSummaryMetricLayoutParams"))
    }
}
