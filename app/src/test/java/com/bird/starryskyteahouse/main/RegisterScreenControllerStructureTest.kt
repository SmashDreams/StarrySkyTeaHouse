package com.bird.starryskyteahouse.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RegisterScreenControllerStructureTest {
    @Test
    fun registerActivityDelegatesFormAccessAndButtonBinding() {
        val activity = File("src/main/java/com/bird/starryskyteahouse/RegisterActivity.kt").readText()
        val controller = File("src/main/java/com/bird/starryskyteahouse/main/RegisterScreenController.kt")

        assertTrue(controller.isFile)
        assertTrue(activity.contains("RegisterScreenController("))
        assertTrue(activity.contains("mScreenController.username"))
        assertTrue(activity.contains("mScreenController.bindActions"))
        assertFalse(activity.contains("private fun bindViews"))
        assertFalse(activity.contains("private fun bindActions"))
    }

    @Test
    fun screenControllerOwnsRegisterInputPropertiesAndClickListeners() {
        val controller = File("src/main/java/com/bird/starryskyteahouse/main/RegisterScreenController.kt").readText()

        assertTrue(controller.contains("val username"))
        assertTrue(controller.contains("val password"))
        assertTrue(controller.contains("val confirmPassword"))
        assertTrue(controller.contains("val rememberPassword"))
        assertTrue(controller.contains("setTeaClickListener"))
    }
}
