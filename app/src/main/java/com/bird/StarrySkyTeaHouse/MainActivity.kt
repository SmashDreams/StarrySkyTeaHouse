package com.bird.StarrySkyTeaHouse

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
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
import androidx.lifecycle.lifecycleScope
import com.bird.StarrySkyTeaHouse.records.GameRecord
import com.bird.StarrySkyTeaHouse.records.GameRecordLoadResult
import com.bird.StarrySkyTeaHouse.records.GameRecordRepository
import com.bird.StarrySkyTeaHouse.records.GameRecordSummary
import com.bird.StarrySkyTeaHouse.session.SessionContract
import com.bird.StarrySkyTeaHouse.session.SessionStore
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var mSessionStore: SessionStore
    private lateinit var mGameEntryManager: GameEntryManager
    private lateinit var mGameRecordRepository: GameRecordRepository
    private lateinit var mUsernameInput: EditText
    private lateinit var mPasswordInput: EditText
    private lateinit var mCurrentUser: TextView
    private lateinit var mRecordsContainer: LinearLayout
    private lateinit var mGameStatus: TextView
    private lateinit var mRememberPasswordCheckBox: CheckBox
    private lateinit var mGameActionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets()

        mSessionStore = SessionStore(this)
        mGameEntryManager = GameEntryManager(this)
        mGameRecordRepository = GameRecordRepository(contentResolver)
        mUsernameInput = findViewById(R.id.username_input)
        mPasswordInput = findViewById(R.id.password_input)
        mCurrentUser = findViewById(R.id.current_user)
        mRecordsContainer = findViewById(R.id.records_container)
        mGameStatus = findViewById(R.id.game_status)
        mRememberPasswordCheckBox = findViewById(R.id.remember_password_checkbox)
        mGameActionButton = findViewById(R.id.game_action_button)
        findViewById<Button>(R.id.login_button).setOnClickListener { login() }
        findViewById<Button>(R.id.register_button).setOnClickListener { openRegisterPage() }
        findViewById<Button>(R.id.logout_button).setOnClickListener { logout() }
        mGameActionButton.setOnClickListener { handleGameAction() }
        restoreLoginInputs()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        restoreLoginInputs()
        refreshUi()
    }

    private fun login() {
        if (mSessionStore.isLoggedIn()) {
            Toast.makeText(this, R.string.already_logged_in, Toast.LENGTH_SHORT).show()
            return
        }

        if (mSessionStore.login(getUsernameInput(), getPasswordInput(), mRememberPasswordCheckBox.isChecked)) {
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
        mSessionStore.logout()
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
        notifySessionChanged()
        refreshUi()
    }

    private fun handleGameAction() {
        if (mGameEntryManager.isGameInstalled()) {
            if (!mGameEntryManager.openGame()) {
                Toast.makeText(this, R.string.game_open_failed, Toast.LENGTH_SHORT).show()
            }
            return
        }

        try {
            mGameEntryManager.installBundledGame()
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
        val lastUsername = mSessionStore.getLastLoginUsername()
        if (!lastUsername.isNullOrEmpty()) {
            mUsernameInput.setText(lastUsername)
            mUsernameInput.setSelection(mUsernameInput.text.length)
        }
        mRememberPasswordCheckBox.isChecked = mSessionStore.isRememberPasswordEnabled()
        if (mRememberPasswordCheckBox.isChecked) {
            mPasswordInput.setText(mSessionStore.getRememberedPassword().orEmpty())
        } else {
            mPasswordInput.text.clear()
        }
    }

    private fun refreshUi() {
        refreshGameEntryUi()

        val username = mSessionStore.getCurrentUsername()
        if (username.isNullOrEmpty()) {
            mCurrentUser.setText(R.string.current_user_logged_out)
            showRecordsMessage(R.string.records_login_first)
            return
        }

        mCurrentUser.text = getString(R.string.current_user_logged_in, username)
        showRecordsMessage(R.string.records_loading)
        loadGameRecordsAsync(username)
    }

    private fun refreshGameEntryUi() {
        if (mGameEntryManager.isGameInstalled()) {
            mGameStatus.setText(R.string.game_status_installed)
            mGameActionButton.setText(R.string.game_open)
        } else {
            mGameStatus.setText(R.string.game_status_not_installed)
            mGameActionButton.setText(R.string.game_install)
        }
    }

    private fun loadGameRecordsAsync(username: String) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                mGameRecordRepository.loadForUsername(username)
            }
            if (username == mSessionStore.getCurrentUsername()) {
                renderRecords(result.toRecordsState())
            }
        }
    }

    private fun GameRecordLoadResult.toRecordsState(): RecordsState {
        return when (this) {
            GameRecordLoadResult.Empty -> RecordsState.Message(R.string.records_empty)
            GameRecordLoadResult.Unavailable -> RecordsState.Message(R.string.records_missing_game)
            is GameRecordLoadResult.Records -> RecordsState.Records(records)
        }
    }

    private fun renderRecords(state: RecordsState) {
        when (state) {
            is RecordsState.Message -> showRecordsMessage(state.messageRes)
            is RecordsState.Records -> showRecordCards(state.records)
        }
    }

    private fun showRecordsMessage(@StringRes messageRes: Int) {
        mRecordsContainer.removeAllViews()
        mRecordsContainer.addView(
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
        mRecordsContainer.removeAllViews()
        addSummaryRow(GameRecordSummary.from(records))
        records.forEachIndexed { index, record -> addRecordCard(record, index > 0) }
    }

    private fun addSummaryRow(summary: GameRecordSummary) {
        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        mRecordsContainer.addView(
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
        mRecordsContainer.addView(
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

    private fun getUsernameInput(): String = mUsernameInput.text.toString()

    private fun getPasswordInput(): String = mPasswordInput.text.toString()

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
}
