package com.bird.starryskyteahouse

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SharedContractsStructureTest {
    @Test
    fun appIncludesSharedContractSources() {
        val buildFile = File("build.gradle.kts").readText()

        assertTrue(buildFile.contains("../../StarrySkySudoku/shared-contracts/src/main/java"))
    }

    @Test
    fun localContractsDelegateToSharedContracts() {
        val gameEntryContract = File("src/main/java/com/bird/starryskyteahouse/game/GameEntryContract.kt").readText()
        val resultContract = File("src/main/java/com/bird/starryskyteahouse/records/GameResultsContract.kt").readText()
        val sessionContract = File("src/main/java/com/bird/starryskyteahouse/session/SessionContract.kt").readText()

        assertTrue(gameEntryContract.contains("SharedGameEntryContract"))
        assertTrue(resultContract.contains("SharedGameResultsContract"))
        assertTrue(sessionContract.contains("SharedSessionContract"))
    }
}
