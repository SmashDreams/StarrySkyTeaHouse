package com.bird.starryskyteahouse.main

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import com.bird.starryskyteahouse.R
import com.bird.starryskyteahouse.databinding.ActivityMainBinding
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainScreenRendererTest {
    @Test
    fun loggedInRefreshReplacesFocusedLoginInputsWithRememberedCredentials() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_StarrySkyTeaHouse)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(context))
        val renderer = MainScreenRenderer(context, binding)
        binding.usernameInput.setText("1234")
        binding.passwordInput.setText("1234")
        binding.usernameInput.requestFocus()

        renderer.render(
            MainUiState(
                currentUsername = "123",
                isLoggedIn = true,
                lastLoginUsername = "123",
                rememberPassword = true,
                rememberedPassword = "123"
            )
        )

        assertEquals("123", binding.usernameInput.text.toString())
        assertEquals("123", binding.passwordInput.text.toString())
    }
}
