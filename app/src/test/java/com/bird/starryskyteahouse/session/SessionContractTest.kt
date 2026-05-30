package com.bird.starryskyteahouse.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionContractTest {
    @Test
    fun sessionConstantsMatchProviderContract() {
        assertEquals("com.bird.starryskyteahouse.provider", SessionContract.AUTHORITY)
        assertEquals("com.bird.starryskyteahouse.permission.READ_SESSION", SessionContract.READ_PERMISSION)
        assertEquals("session", SessionContract.Session.PATH)
        assertEquals("content://com.bird.starryskyteahouse.provider/session", SessionContract.Session.CONTENT_URI_STRING)
        assertEquals("username", SessionContract.Session.COLUMN_USERNAME)
        assertEquals("logged_in", SessionContract.Session.COLUMN_LOGGED_IN)
    }
}
