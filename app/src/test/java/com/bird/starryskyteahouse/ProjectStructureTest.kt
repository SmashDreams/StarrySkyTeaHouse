package com.bird.starryskyteahouse

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProjectStructureTest {
    @Test
    fun appModuleEnablesViewBinding() {
        val source = File("build.gradle.kts").readText()

        assertTrue(source.contains("viewBinding = true"))
    }

    @Test
    fun activitiesUseGeneratedBindingsInsteadOfFindViewById() {
        val mainActivity = File("src/main/java/com/bird/starryskyteahouse/MainActivity.kt").readText()
        val registerActivity = File("src/main/java/com/bird/starryskyteahouse/RegisterActivity.kt").readText()

        assertTrue(mainActivity.contains("ActivityMainBinding"))
        assertTrue(registerActivity.contains("ActivityRegisterBinding"))
        assertFalse(mainActivity.contains("findViewById<"))
        assertFalse(registerActivity.contains("findViewById<"))
    }

    @Test
    fun viewModelFactoriesUseAppContainer() {
        val mainFactory = File("src/main/java/com/bird/starryskyteahouse/main/MainViewModelFactory.kt").readText()
        val registerFactory = File("src/main/java/com/bird/starryskyteahouse/main/RegisterViewModelFactory.kt").readText()

        assertTrue(File("src/main/java/com/bird/starryskyteahouse/TeaHouseAppContainer.kt").isFile)
        assertTrue(mainFactory.contains("TeaHouseAppContainer"))
        assertTrue(registerFactory.contains("TeaHouseAppContainer"))
    }

    @Test
    fun packageNameUsesLowercaseSegments() {
        val buildFile = File("build.gradle.kts").readText()
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(buildFile.contains("namespace = \"com.bird.starryskyteahouse\""))
        assertTrue(buildFile.contains("applicationId = \"com.bird.starryskyteahouse\""))
        assertTrue(File("src/main/java/com/bird/starryskyteahouse/MainActivity.kt").isFile)
        assertFalse(manifest.contains("com.bird.StarrySkyTeaHouse"))
    }

    @Test
    fun releaseBuildUsesLocalSigningProperties() {
        val buildFile = File("build.gradle.kts").readText()

        assertTrue(buildFile.contains("RELEASE_STORE_FILE"))
        assertTrue(buildFile.contains("RELEASE_STORE_PASSWORD"))
        assertTrue(buildFile.contains("RELEASE_KEY_ALIAS"))
        assertTrue(buildFile.contains("RELEASE_KEY_PASSWORD"))
        assertTrue(buildFile.contains("signingConfigs"))
        assertTrue(buildFile.contains("signingConfig = signingConfigs.getByName(\"release\")"))
    }
}
