package com.bird.starryskyteahouse.session

import org.junit.Assert.assertEquals
import org.junit.Test

class LoginPreferenceContractTest {
    @Test
    fun loginPreferenceKeysAreStable() {
        assertEquals("last_login_username", LoginPreferenceContract.KEY_LAST_USERNAME)
        assertEquals("remember_password", LoginPreferenceContract.KEY_REMEMBER_PASSWORD)
        assertEquals("remembered_password", LoginPreferenceContract.KEY_REMEMBERED_PASSWORD)
    }
}
