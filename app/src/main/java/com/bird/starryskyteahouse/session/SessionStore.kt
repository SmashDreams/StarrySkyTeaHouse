package com.bird.starryskyteahouse.session

import android.content.Context
import android.content.SharedPreferences
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SessionStore private constructor(context: Context) {
    private val mPreferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun register(username: String?, password: String?, rememberPassword: Boolean = false): Boolean {
        val normalizedUsername = normalizeUsername(username)
        if (!isValidCredential(normalizedUsername, password) || hasUser(normalizedUsername)) {
            return false
        }
        val safePassword = password.orEmpty()
        return mPreferences.edit()
            .putString(userKey(normalizedUsername), hashPassword(safePassword))
            .applyLoginPreferences(normalizedUsername, safePassword, rememberPassword)
            .commit()
    }

    fun login(username: String?, password: String?, rememberPassword: Boolean = false): Boolean {
        val normalizedUsername = normalizeUsername(username)
        if (normalizedUsername.isEmpty() || password == null) return false
        val storedHash = mPreferences.getString(userKey(normalizedUsername), null) ?: return false
        if (storedHash != hashPassword(password)) return false
        return mPreferences.edit()
            .applyLoginPreferences(normalizedUsername, password, rememberPassword)
            .commit()
    }

    fun logout() {
        mPreferences.edit().remove(KEY_CURRENT_USERNAME).commit()
    }

    fun getCurrentUsername(): String? = mPreferences.getString(KEY_CURRENT_USERNAME, null)

    fun isLoggedIn(): Boolean = !getCurrentUsername().isNullOrEmpty()

    fun getLastLoginUsername(): String? {
        return mPreferences.getString(LoginPreferenceContract.KEY_LAST_USERNAME, null)
    }

    fun isRememberPasswordEnabled(): Boolean {
        return mPreferences.getBoolean(LoginPreferenceContract.KEY_REMEMBER_PASSWORD, false)
    }

    fun getRememberedPassword(): String? {
        return mPreferences.getString(LoginPreferenceContract.KEY_REMEMBERED_PASSWORD, null)
    }

    private fun SharedPreferences.Editor.applyLoginPreferences(
        username: String,
        password: String,
        rememberPassword: Boolean
    ): SharedPreferences.Editor {
        putString(KEY_CURRENT_USERNAME, username)
        putString(LoginPreferenceContract.KEY_LAST_USERNAME, username)
        putBoolean(LoginPreferenceContract.KEY_REMEMBER_PASSWORD, rememberPassword)
        if (rememberPassword) {
            putString(LoginPreferenceContract.KEY_REMEMBERED_PASSWORD, password)
        } else {
            remove(LoginPreferenceContract.KEY_REMEMBERED_PASSWORD)
        }
        return this
    }

    private fun hasUser(username: String): Boolean = mPreferences.contains(userKey(username))

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

    companion object {
        @Volatile
        private var sInstance: SessionStore? = null

        fun getInstance(context: Context): SessionStore {
            return sInstance ?: synchronized(this) {
                sInstance ?: SessionStore(context.applicationContext).also { sInstance = it }
            }
        }

        private const val PREFS_NAME = "StarrySkyTeaHouse_session"
        private const val KEY_CURRENT_USERNAME = "current_username"
        private const val USER_PREFIX = "user."
    }
}
