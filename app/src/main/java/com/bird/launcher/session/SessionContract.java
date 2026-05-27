package com.bird.launcher.session;

import android.net.Uri;

public final class SessionContract {
    public static final String AUTHORITY = "com.bird.launcher.provider";
    public static final String CONTENT_URI_BASE = "content://" + AUTHORITY;

    private SessionContract() {
    }

    public static final class Session {
        public static final String PATH = "session";
        public static final String CONTENT_URI_STRING = CONTENT_URI_BASE + "/" + PATH;
        public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
        public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.com.bird.launcher.session";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_LOGGED_IN = "logged_in";

        private Session() {
        }
    }
}
