package com.bird.StarrySkyTeaHouse.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionContractTest {
    @Test
    fun sessionConstantsMatchProviderContract() {
        assertEquals("com.bird.StarrySkyTeaHouse.provider", SessionContract.AUTHORITY)
        assertEquals("com.bird.StarrySkyTeaHouse.permission.READ_SESSION", SessionContract.READ_PERMISSION)
        assertEquals("session", SessionContract.Session.PATH)
        assertEquals("content://com.bird.StarrySkyTeaHouse.provider/session", SessionContract.Session.CONTENT_URI_STRING)
        assertEquals("username", SessionContract.Session.COLUMN_USERNAME)
        assertEquals("logged_in", SessionContract.Session.COLUMN_LOGGED_IN)
    }
}
