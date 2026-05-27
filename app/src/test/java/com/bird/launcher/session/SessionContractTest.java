package com.bird.launcher.session;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SessionContractTest {
    @Test
    public void sessionConstantsMatchProviderContract() {
        assertEquals("com.bird.launcher.provider", SessionContract.AUTHORITY);
        assertEquals("session", SessionContract.Session.PATH);
        assertEquals("content://com.bird.launcher.provider/session", SessionContract.Session.CONTENT_URI_STRING);
        assertEquals("username", SessionContract.Session.COLUMN_USERNAME);
        assertEquals("logged_in", SessionContract.Session.COLUMN_LOGGED_IN);
    }
}
