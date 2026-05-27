package com.bird.StarrySkyTeaHouse

import android.content.ActivityNotFoundException
import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bird.StarrySkyTeaHouse.session.SessionContract
import com.bird.StarrySkyTeaHouse.session.SessionStore
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var sessionStore: SessionStore
    private lateinit var gameEntryManager: GameEntryManager
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var currentUser: TextView
    private lateinit var recordsContainer: LinearLayout
    private lateinit var gameStatus: TextView
    private lateinit var rememberPasswordCheckBox: CheckBox
    private lateinit var gameActionButton: Button
    private val recordsExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets()

        sessionStore = SessionStore(this)
        gameEntryManager = GameEntryManager(this)
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        currentUser = findViewById(R.id.current_user)
        recordsContainer = findViewById(R.id.records_container)
        gameStatus = findViewById(R.id.game_status)
        rememberPasswordCheckBox = findViewById(R.id.remember_password_checkbox)
        gameActionButton = findViewById(R.id.game_action_button)
        findViewById<Button>(R.id.login_button).setOnClickListener { login() }
        findViewById<Button>(R.id.register_button).setOnClickListener { openRegisterPage() }
        findViewById<Button>(R.id.logout_button).setOnClickListener { logout() }
        gameActionButton.setOnClickListener { handleGameAction() }
        restoreLoginInputs()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        restoreLoginInputs()
        refreshUi()
    }

    override fun onDestroy() {
        recordsExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun login() {
        if (sessionStore.isLoggedIn()) {
            Toast.makeText(this, R.string.already_logged_in, Toast.LENGTH_SHORT).show()
            return
        }

        if (sessionStore.login(getUsernameInput(), getPasswordInput(), rememberPasswordCheckBox.isChecked)) {
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
            notifySessionChanged()
            refreshUi()
        } else {
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRegisterPage() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun logout() {
        sessionStore.logout()
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
        notifySessionChanged()
        refreshUi()
    }

    private fun handleGameAction() {
        if (gameEntryManager.isGameInstalled()) {
            if (!gameEntryManager.openGame()) {
                Toast.makeText(this, R.string.game_open_failed, Toast.LENGTH_SHORT).show()
            }
            return
        }

        try {
            gameEntryManager.installBundledGame()
            Toast.makeText(this, R.string.game_install_started, Toast.LENGTH_SHORT).show()
        } catch (exception: IOException) {
            Toast.makeText(this, R.string.game_install_asset_missing, Toast.LENGTH_SHORT).show()
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(this, R.string.game_install_no_handler, Toast.LENGTH_SHORT).show()
        } catch (exception: SecurityException) {
            Toast.makeText(this, R.string.game_install_permission_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreLoginInputs() {
        val lastUsername = sessionStore.getLastLoginUsername()
        if (!lastUsername.isNullOrEmpty()) {
            usernameInput.setText(lastUsername)
            usernameInput.setSelection(usernameInput.text.length)
        }
        rememberPasswordCheckBox.isChecked = sessionStore.isRememberPasswordEnabled()
        if (rememberPasswordCheckBox.isChecked) {
            passwordInput.setText(sessionStore.getRememberedPassword().orEmpty())
        } else {
            passwordInput.text.clear()
        }
    }

    private fun refreshUi() {
        refreshGameEntryUi()

        val username = sessionStore.getCurrentUsername()
        if (username.isNullOrEmpty()) {
            currentUser.setText(R.string.current_user_logged_out)
            showRecordsMessage(R.string.records_login_first)
            return
        }

        currentUser.text = getString(R.string.current_user_logged_in, username)
        showRecordsMessage(R.string.records_loading)
        loadGameRecordsAsync(username)
    }

    private fun refreshGameEntryUi() {
        if (gameEntryManager.isGameInstalled()) {
            gameStatus.setText(R.string.game_status_installed)
            gameActionButton.setText(R.string.game_open)
        } else {
            gameStatus.setText(R.string.game_status_not_installed)
            gameActionButton.setText(R.string.game_install)
        }
    }

    private fun loadGameRecordsAsync(username: String) {
        recordsExecutor.execute {
            val recordsState = loadGameRecords(username)
            runOnUiThread {
                if (username == sessionStore.getCurrentUsername()) {
                    renderRecords(recordsState)
                }
            }
        }
    }

    private fun loadGameRecords(username: String): RecordsState {
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
            )?.use { cursor ->
                val records = parseRecords(cursor)
                if (records.isEmpty()) RecordsState.Message(R.string.records_empty) else RecordsState.Records(records)
            } ?: RecordsState.Message(R.string.records_missing_game)
        } catch (exception: RuntimeException) {
            RecordsState.Message(R.string.records_missing_game)
        }
    }

    private fun parseRecords(cursor: Cursor): List<GameRecord> {
        val records = mutableListOf<GameRecord>()
        while (cursor.moveToNext()) {
            records += GameRecord(
                level = getInt(cursor, COLUMN_LEVEL),
                elapsedSeconds = getInt(cursor, COLUMN_ELAPSED_SECONDS),
                remainingSeconds = getInt(cursor, COLUMN_REMAINING_SECONDS),
                completed = getInt(cursor, COLUMN_COMPLETED) == 1
            )
        }
        return records
    }

    private fun renderRecords(state: RecordsState) {
        when (state) {
            is RecordsState.Message -> showRecordsMessage(state.messageRes)
            is RecordsState.Records -> showRecordCards(state.records)
        }
    }

    private fun showRecordsMessage(@StringRes messageRes: Int) {
        recordsContainer.removeAllViews()
        recordsContainer.addView(
            createText(getString(messageRes), R.color.tea_mist, 14f).apply {
                setBackgroundResource(R.drawable.bg_records_panel)
                gravity = Gravity.CENTER
                minHeight = dp(104)
                setPadding(dp(16), dp(16), dp(16), dp(16))
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun showRecordCards(records: List<GameRecord>) {
        recordsContainer.removeAllViews()
        addSummaryRow(GameRecordSummary.from(records))
        records.forEachIndexed { index, record -> addRecordCard(record, index > 0) }
    }

    private fun addSummaryRow(summary: GameRecordSummary) {
        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        recordsContainer.addView(
            summaryRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        addSummaryItem(summaryRow, getString(R.string.records_summary_completed), summary.completedCount.toString())
        addSummaryItem(summaryRow, getString(R.string.records_summary_highest), getString(R.string.records_level_badge, summary.highestLevel))
        addSummaryItem(summaryRow, getString(R.string.records_summary_latest), getString(R.string.records_level_badge, summary.latestLevel))
    }

    private fun addSummaryItem(parent: LinearLayout, label: String, value: String) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_record_summary)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            addView(createText(value, R.color.tea_gold, 15f, bold = true))
            addView(createText(label, R.color.tea_muted, 11f))
        }
        parent.addView(
            item,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            }
        )
    }

    private fun addRecordCard(record: GameRecord, hasTopMargin: Boolean) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_record_item)
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }

        val badge = createText(getString(R.string.records_level_badge, record.level), R.color.tea_gold, 15f, bold = true).apply {
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_record_badge)
        }
        card.addView(badge, LinearLayout.LayoutParams(dp(72), dp(58)))

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        val statusText = if (record.completed) getString(R.string.record_completed) else getString(R.string.record_uncompleted)
        val statusBackground = if (record.completed) R.drawable.bg_record_status_complete else R.drawable.bg_record_status_pending
        content.addView(
            createText(statusText, R.color.tea_mist, 13f, bold = true).apply {
                setBackgroundResource(statusBackground)
                setPadding(dp(10), dp(5), dp(10), dp(5))
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )

        val metrics = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        metrics.addView(createMetric(getString(R.string.records_elapsed_label), getString(R.string.records_seconds, record.elapsedSeconds)))
        metrics.addView(createMetric(getString(R.string.records_remaining_label), getString(R.string.records_seconds, record.remainingSeconds)))
        content.addView(
            metrics,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(10)
            }
        )

        card.addView(content, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        recordsContainer.addView(
            card,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = if (hasTopMargin) dp(10) else dp(14)
            }
        )
    }

    private fun createMetric(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_record_metric)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            addView(createText(label, R.color.tea_muted, 11f))
            addView(createText(value, R.color.tea_mist, 13f, bold = true))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(8)
            }
        }
    }

    private fun createText(text: String, colorRes: Int, sizeSp: Float, bold: Boolean = false): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(this@MainActivity, colorRes))
            textSize = sizeSp
            includeFontPadding = true
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }
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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private sealed class RecordsState {
        data class Message(@param:StringRes val messageRes: Int) : RecordsState()
        data class Records(val records: List<GameRecord>) : RecordsState()
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
