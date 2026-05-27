package com.bird.launcher.session;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class SessionProvider extends ContentProvider {
    private static final int MATCH_SESSION = 1;
    private static final String[] DEFAULT_COLUMNS = {
            SessionContract.Session.COLUMN_USERNAME,
            SessionContract.Session.COLUMN_LOGGED_IN
    };
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(SessionContract.AUTHORITY, SessionContract.Session.PATH, MATCH_SESSION);
    }

    private SessionStore sessionStore;

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }
        sessionStore = new SessionStore(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (URI_MATCHER.match(uri) != MATCH_SESSION) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        String[] columns = resolveProjection(projection);
        MatrixCursor cursor = new MatrixCursor(columns);
        Map<String, Object> values = createSessionValues();
        Object[] row = new Object[columns.length];
        for (int index = 0; index < columns.length; index++) {
            row[index] = values.get(columns[index]);
        }
        cursor.addRow(row);
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        if (URI_MATCHER.match(uri) == MATCH_SESSION) {
            return SessionContract.Session.CONTENT_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("SessionProvider is read-only");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private String[] resolveProjection(String[] projection) {
        String[] columns = projection == null ? DEFAULT_COLUMNS : projection;
        for (String column : columns) {
            if (!SessionContract.Session.COLUMN_USERNAME.equals(column)
                    && !SessionContract.Session.COLUMN_LOGGED_IN.equals(column)) {
                throw new IllegalArgumentException("Unsupported projection column: " + column);
            }
        }
        return columns;
    }

    private Map<String, Object> createSessionValues() {
        Map<String, Object> values = new HashMap<>();
        values.put(SessionContract.Session.COLUMN_USERNAME, sessionStore.getCurrentUsername());
        values.put(SessionContract.Session.COLUMN_LOGGED_IN, sessionStore.isLoggedIn() ? 1 : 0);
        return values;
    }
}
