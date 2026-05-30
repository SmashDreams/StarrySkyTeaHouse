package com.bird.starryskyteahouse.session

import android.net.Uri

object SessionContract {
    const val AUTHORITY = "com.bird.starryskyteahouse.provider"
    const val READ_PERMISSION = "com.bird.starryskyteahouse.permission.READ_SESSION"
    const val CONTENT_URI_BASE = "content://$AUTHORITY"

    object Session {
        const val PATH = "session"
        const val CONTENT_URI_STRING = "$CONTENT_URI_BASE/$PATH"
        val CONTENT_URI: Uri = Uri.parse(CONTENT_URI_STRING)
        const val CONTENT_TYPE = "vnd.android.cursor.item/vnd.com.bird.starryskyteahouse.session"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_LOGGED_IN = "logged_in"
    }
}
