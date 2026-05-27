package com.bird.launcher;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bird.launcher.session.SessionContract;
import com.bird.launcher.session.SessionStore;

public class MainActivity extends AppCompatActivity {
    private static final Uri GAME_RESULTS_URI = Uri.parse("content://com.bird.starryskysudoku.provider/results");
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_ELAPSED_SECONDS = "elapsed_seconds";
    private static final String COLUMN_REMAINING_SECONDS = "remaining_seconds";
    private static final String COLUMN_COMPLETED = "completed";

    private SessionStore sessionStore;
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView currentUser;
    private TextView recordsView;
    private final ExecutorService recordsExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applySystemBarInsets();

        sessionStore = new SessionStore(this);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        currentUser = findViewById(R.id.current_user);
        recordsView = findViewById(R.id.records_view);
        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);
        Button logoutButton = findViewById(R.id.logout_button);

        loginButton.setOnClickListener(view -> login());
        registerButton.setOnClickListener(view -> register());
        logoutButton.setOnClickListener(view -> logout());
        refreshUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }


    @Override
    protected void onDestroy() {
        recordsExecutor.shutdownNow();
        super.onDestroy();
    }

    private void register() {
        boolean registered = sessionStore.register(getUsernameInput(), getPasswordInput());
        if (registered) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            notifySessionChanged();
            refreshUi();
        } else {
            Toast.makeText(this, "注册失败：用户名不能为空，密码至少3位，且用户名不可重复", Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        boolean loggedIn = sessionStore.login(getUsernameInput(), getPasswordInput());
        if (loggedIn) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            notifySessionChanged();
            refreshUi();
        } else {
            Toast.makeText(this, "登录失败：请检查用户名和密码", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        sessionStore.logout();
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        notifySessionChanged();
        refreshUi();
    }

    private void refreshUi() {
        String username = sessionStore.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            currentUser.setText(R.string.current_user_logged_out);
            recordsView.setText(R.string.records_login_first);
            return;
        }

        currentUser.setText("当前登录：" + username);
        recordsView.setText(R.string.records_loading);
        loadGameRecordsAsync(username);
    }

    private void loadGameRecordsAsync(String username) {
        recordsExecutor.execute(() -> {
            String records = loadGameRecords(username);
            runOnUiThread(() -> {
                String currentUsername = sessionStore.getCurrentUsername();
                if (username.equals(currentUsername)) {
                    recordsView.setText(records);
                }
            });
        });
    }

    private String loadGameRecords(String username) {
        String[] projection = {
                COLUMN_USERNAME,
                COLUMN_LEVEL,
                COLUMN_ELAPSED_SECONDS,
                COLUMN_REMAINING_SECONDS,
                COLUMN_COMPLETED
        };
        try (Cursor cursor = getContentResolver().query(
                GAME_RESULTS_URI,
                projection,
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null
        )) {
            if (cursor == null) {
                return getString(R.string.records_missing_game);
            }
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                int level = getInt(cursor, COLUMN_LEVEL);
                int elapsed = getInt(cursor, COLUMN_ELAPSED_SECONDS);
                int remaining = getInt(cursor, COLUMN_REMAINING_SECONDS);
                boolean completed = getInt(cursor, COLUMN_COMPLETED) == 1;
                builder.append("第")
                        .append(level)
                        .append("关  ")
                        .append(completed ? "已通关" : "未通关")
                        .append("  用时")
                        .append(elapsed)
                        .append("秒  剩余")
                        .append(remaining)
                        .append("秒\n");
            }
            if (builder.length() == 0) {
                return getString(R.string.records_empty);
            }
            return builder.toString();
        } catch (RuntimeException exception) {
            return getString(R.string.records_missing_game);
        }
    }

    private int getInt(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getInt(index) : 0;
    }

    private String getUsernameInput() {
        return usernameInput.getText().toString();
    }

    private String getPasswordInput() {
        return passwordInput.getText().toString();
    }

    private void notifySessionChanged() {
        getContentResolver().notifyChange(SessionContract.Session.CONTENT_URI, null);
    }

    private void applySystemBarInsets() {
        View root = findViewById(R.id.root);
        int baseLeft = root.getPaddingLeft();
        int baseTop = root.getPaddingTop();
        int baseRight = root.getPaddingRight();
        int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    baseLeft + systemBars.left,
                    baseTop + systemBars.top,
                    baseRight + systemBars.right,
                    baseBottom + systemBars.bottom
            );
            return windowInsets;
        });
    }
}
