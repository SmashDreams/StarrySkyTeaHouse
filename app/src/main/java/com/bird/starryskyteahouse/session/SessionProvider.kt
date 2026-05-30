package com.bird.starryskyteahouse.session

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class SessionProvider : ContentProvider() {
    private lateinit var mSessionStore: SessionStore

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false
        mSessionStore = SessionStore(appContext)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        if (sUriMatcher.match(uri) != MATCH_SESSION) {
            throw IllegalArgumentException("Unsupported URI: $uri")
        }
        val columns = resolveProjection(projection)
        val values = createSessionValues()
        val cursor = MatrixCursor(columns)
        cursor.addRow(columns.map { values[it] }.toTypedArray())
        context?.contentResolver?.let { cursor.setNotificationUri(it, uri) }
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return if (sUriMatcher.match(uri) == MATCH_SESSION) {
            SessionContract.Session.CONTENT_TYPE
        } else {
            null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        throw UnsupportedOperationException("SessionProvider is read-only")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    private fun resolveProjection(projection: Array<out String>?): Array<out String> {
        val columns = projection ?: sDefaultColumns
        columns.forEach { column ->
            if (column != SessionContract.Session.COLUMN_USERNAME &&
                column != SessionContract.Session.COLUMN_LOGGED_IN
            ) {
                throw IllegalArgumentException("Unsupported projection column: $column")
            }
        }
        return columns
    }

    private fun createSessionValues(): Map<String, Any?> {
        return mapOf(
            SessionContract.Session.COLUMN_USERNAME to mSessionStore.getCurrentUsername(),
            SessionContract.Session.COLUMN_LOGGED_IN to if (mSessionStore.isLoggedIn()) 1 else 0
        )
    }

    private companion object {
        const val MATCH_SESSION = 1
        val sDefaultColumns = arrayOf(
            SessionContract.Session.COLUMN_USERNAME,
            SessionContract.Session.COLUMN_LOGGED_IN
        )
        val sUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(SessionContract.AUTHORITY, SessionContract.Session.PATH, MATCH_SESSION)
        }
    }
}
