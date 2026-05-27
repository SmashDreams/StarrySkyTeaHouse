package com.bird.StarrySkyTeaHouse

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bird.StarrySkyTeaHouse.session.SessionContract
import com.bird.StarrySkyTeaHouse.session.SessionStore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var sessionStore: SessionStore
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var currentUser: TextView
    private lateinit var recordsView: TextView
    private val recordsExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets()

        sessionStore = SessionStore(this)
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        currentUser = findViewById(R.id.current_user)
        recordsView = findViewById(R.id.records_view)
        findViewById<Button>(R.id.login_button).setOnClickListener { login() }
        findViewById<Button>(R.id.register_button).setOnClickListener { register() }
        findViewById<Button>(R.id.logout_button).setOnClickListener { logout() }
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    override fun onDestroy() {
        recordsExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun register() {
        if (sessionStore.register(getUsernameInput(), getPasswordInput())) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
            notifySessionChanged()
            refreshUi()
        } else {
            Toast.makeText(this, "注册失败：用户名不能为空，密码至少3位，且用户名不可重复", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login() {
        if (sessionStore.login(getUsernameInput(), getPasswordInput())) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
            notifySessionChanged()
            refreshUi()
        } else {
            Toast.makeText(this, "登录失败：请检查用户名和密码", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        sessionStore.logout()
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
        notifySessionChanged()
        refreshUi()
    }

    private fun refreshUi() {
        val username = sessionStore.getCurrentUsername()
        if (username.isNullOrEmpty()) {
            currentUser.setText(R.string.current_user_logged_out)
            recordsView.setText(R.string.records_login_first)
            return
        }

        currentUser.text = "当前登录：$username"
        recordsView.setText(R.string.records_loading)
        loadGameRecordsAsync(username)
    }

    private fun loadGameRecordsAsync(username: String) {
        recordsExecutor.execute {
            val records = loadGameRecords(username)
            runOnUiThread {
                if (username == sessionStore.getCurrentUsername()) {
                    recordsView.text = records
                }
            }
        }
    }

    private fun loadGameRecords(username: String): String {
        val projection = arrayOf(
            COLUMN_USERNAME,
            COLUMN_LEVEL,
            COLUMN_ELAPSED_SECONDS,
            COLUMN_REMAINING_SECONDS,
            COLUMN_COMPLETED
        )
        return try {
            contentResolver.query(
                GAME_RESULTS_URI,
                projection,
                "$COLUMN_USERNAME=?",
                arrayOf(username),
                null
            )?.use { cursor -> formatRecords(cursor) } ?: getString(R.string.records_missing_game)
        } catch (exception: RuntimeException) {
            getString(R.string.records_missing_game)
        }
    }

    private fun formatRecords(cursor: Cursor): String {
        val builder = StringBuilder()
        while (cursor.moveToNext()) {
            val level = getInt(cursor, COLUMN_LEVEL)
            val elapsed = getInt(cursor, COLUMN_ELAPSED_SECONDS)
            val remaining = getInt(cursor, COLUMN_REMAINING_SECONDS)
            val completed = getInt(cursor, COLUMN_COMPLETED) == 1
            builder.append("第")
                .append(level)
                .append("关  ")
                .append(if (completed) "已通关" else "未通关")
                .append("  用时")
                .append(elapsed)
                .append("秒  剩余")
                .append(remaining)
                .append("秒\n")
        }
        return if (builder.isEmpty()) getString(R.string.records_empty) else builder.toString()
    }

    private fun getInt(cursor: Cursor, columnName: String): Int {
        val index = cursor.getColumnIndex(columnName)
        return if (index >= 0) cursor.getInt(index) else 0
    }

    private fun getUsernameInput(): String = usernameInput.text.toString()

    private fun getPasswordInput(): String = passwordInput.text.toString()

    private fun notifySessionChanged() {
        contentResolver.notifyChange(SessionContract.Session.CONTENT_URI, null)
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.root)
        val baseLeft = root.paddingLeft
        val baseTop = root.paddingTop
        val baseRight = root.paddingRight
        val baseBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val systemBars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                baseLeft + systemBars.left,
                baseTop + systemBars.top,
                baseRight + systemBars.right,
                baseBottom + systemBars.bottom
            )
            windowInsets
        }
    }

    private companion object {
        val GAME_RESULTS_URI: Uri = Uri.parse("content://com.bird.starryskysudoku.provider/results")
        const val COLUMN_USERNAME = "username"
        const val COLUMN_LEVEL = "level"
        const val COLUMN_ELAPSED_SECONDS = "elapsed_seconds"
        const val COLUMN_REMAINING_SECONDS = "remaining_seconds"
        const val COLUMN_COMPLETED = "completed"
    }
}
