package com.bird.launcher.session;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SessionStore {
    private static final String PREFS_NAME = "launcher_session";
    private static final String KEY_CURRENT_USERNAME = "current_username";
    private static final String USER_PREFIX = "user.";

    private final SharedPreferences preferences;

    public SessionStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (!isValidCredential(normalizedUsername, password) || hasUser(normalizedUsername)) {
            return false;
        }
        return preferences.edit()
                .putString(userKey(normalizedUsername), hashPassword(password))
                .putString(KEY_CURRENT_USERNAME, normalizedUsername)
                .commit();
    }

    public boolean login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername.isEmpty() || password == null) {
            return false;
        }
        String storedHash = preferences.getString(userKey(normalizedUsername), null);
        if (storedHash == null || !storedHash.equals(hashPassword(password))) {
            return false;
        }
        return preferences.edit().putString(KEY_CURRENT_USERNAME, normalizedUsername).commit();
    }

    public void logout() {
        preferences.edit().remove(KEY_CURRENT_USERNAME).commit();
    }

    public String getCurrentUsername() {
        return preferences.getString(KEY_CURRENT_USERNAME, null);
    }

    public boolean isLoggedIn() {
        String username = getCurrentUsername();
        return username != null && !username.isEmpty();
    }

    private boolean hasUser(String username) {
        return preferences.contains(userKey(username));
    }

    private boolean isValidCredential(String username, String password) {
        return !username.isEmpty() && password != null && password.length() >= 3;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String userKey(String username) {
        return USER_PREFIX + username;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return String.valueOf(password.hashCode());
        }
    }
}
