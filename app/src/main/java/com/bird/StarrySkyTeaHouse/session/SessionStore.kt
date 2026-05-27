package com.bird.StarrySkyTeaHouse.session

import android.content.Context
import android.content.SharedPreferences
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SessionStore(context: Context) {
    private val preferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun register(username: String?, password: String?): Boolean {
        val normalizedUsername = normalizeUsername(username)
        if (!isValidCredential(normalizedUsername, password) || hasUser(normalizedUsername)) {
            return false
        }
        return preferences.edit()
            .putString(userKey(normalizedUsername), hashPassword(password.orEmpty()))
            .putString(KEY_CURRENT_USERNAME, normalizedUsername)
            .commit()
    }

    fun login(username: String?, password: String?): Boolean {
        val normalizedUsername = normalizeUsername(username)
        if (normalizedUsername.isEmpty() || password == null) return false
        val storedHash = preferences.getString(userKey(normalizedUsername), null) ?: return false
        if (storedHash != hashPassword(password)) return false
        return preferences.edit().putString(KEY_CURRENT_USERNAME, normalizedUsername).commit()
    }

    fun logout() {
        preferences.edit().remove(KEY_CURRENT_USERNAME).commit()
    }

    fun getCurrentUsername(): String? = preferences.getString(KEY_CURRENT_USERNAME, null)

    fun isLoggedIn(): Boolean = !getCurrentUsername().isNullOrEmpty()

    private fun hasUser(username: String): Boolean = preferences.contains(userKey(username))

    private fun isValidCredential(username: String, password: String?): Boolean {
        return username.isNotEmpty() && password != null && password.length >= 3
    }

    private fun normalizeUsername(username: String?): String = username?.trim().orEmpty()

    private fun userKey(username: String): String = USER_PREFIX + username

    private fun hashPassword(password: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest(password.toByteArray(StandardCharsets.UTF_8))
            bytes.joinToString(separator = "") { "%02x".format(it) }
        } catch (exception: NoSuchAlgorithmException) {
            password.hashCode().toString()
        }
    }

    private companion object {
        const val PREFS_NAME = "StarrySkyTeaHouse_session"
        const val KEY_CURRENT_USERNAME = "current_username"
        const val USER_PREFIX = "user."
    }
}
