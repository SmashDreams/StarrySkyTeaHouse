package com.bird.StarrySkyTeaHouse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.bird.StarrySkyTeaHouse.main.GameEntryUiState
import com.bird.StarrySkyTeaHouse.main.MainEvent
import com.bird.StarrySkyTeaHouse.main.MainToast
import com.bird.StarrySkyTeaHouse.main.MainUiState
import com.bird.StarrySkyTeaHouse.main.MainViewModel
import com.bird.StarrySkyTeaHouse.main.MainViewModelFactory
import com.bird.StarrySkyTeaHouse.main.RecordsRenderer
import com.bird.StarrySkyTeaHouse.session.SessionContract
import com.bird.StarrySkyTeaHouse.ui.applySystemBarPadding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var mViewModel: MainViewModel
    private lateinit var mUsernameInput: EditText
    private lateinit var mPasswordInput: EditText
    private lateinit var mCurrentUser: TextView
    private lateinit var mGameStatus: TextView
    private lateinit var mRememberPasswordCheckBox: CheckBox
    private lateinit var mGameActionButton: Button
    private lateinit var mRecordsRenderer: RecordsRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mViewModel = ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]
        applySystemBarInsets()
        bindViews()
        bindActions()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        mViewModel.refresh()
    }

    private fun bindViews() {
        mUsernameInput = findViewById(R.id.username_input)
        mPasswordInput = findViewById(R.id.password_input)
        mCurrentUser = findViewById(R.id.current_user)
        mGameStatus = findViewById(R.id.game_status)
        mRememberPasswordCheckBox = findViewById(R.id.remember_password_checkbox)
        mGameActionButton = findViewById(R.id.game_action_button)
        mRecordsRenderer = RecordsRenderer(this, findViewById<LinearLayout>(R.id.records_container))
    }

    private fun bindActions() {
        findViewById<Button>(R.id.login_button).setOnClickListener {
            mViewModel.login(
                mUsernameInput.text.toString(),
                mPasswordInput.text.toString(),
                mRememberPasswordCheckBox.isChecked
            )
        }
        findViewById<Button>(R.id.register_button).setOnClickListener { mViewModel.openRegisterPage() }
        findViewById<Button>(R.id.logout_button).setOnClickListener { mViewModel.logout() }
        mGameActionButton.setOnClickListener { mViewModel.handleGameAction() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { mViewModel.uiState.collect { renderState(it) } }
                launch { mViewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun renderState(state: MainUiState) {
        renderLoginInputs(state)
        renderCurrentUser(state)
        renderGameEntry(state.gameEntryState)
        mRecordsRenderer.render(state.recordsState)
    }

    private fun renderLoginInputs(state: MainUiState) {
        val lastUsername = state.lastLoginUsername
        if (!lastUsername.isNullOrEmpty() && !mUsernameInput.hasFocus() && mUsernameInput.text.isEmpty()) {
            mUsernameInput.setText(lastUsername)
            mUsernameInput.setSelection(mUsernameInput.text.length)
        }
        mRememberPasswordCheckBox.isChecked = state.rememberPassword
        if (state.rememberPassword && !mPasswordInput.hasFocus() && mPasswordInput.text.isEmpty()) {
            mPasswordInput.setText(state.rememberedPassword.orEmpty())
        }
    }

    private fun renderCurrentUser(state: MainUiState) {
        if (state.isLoggedIn && !state.currentUsername.isNullOrEmpty()) {
            mCurrentUser.text = getString(R.string.current_user_logged_in, state.currentUsername)
        } else {
            mCurrentUser.setText(R.string.current_user_logged_out)
        }
    }

    private fun renderGameEntry(state: GameEntryUiState) {
        when (state) {
            GameEntryUiState.Installed -> {
                mGameStatus.setText(R.string.game_status_installed)
                mGameActionButton.setText(R.string.game_open)
            }
            GameEntryUiState.NotInstalled -> {
                mGameStatus.setText(R.string.game_status_not_installed)
                mGameActionButton.setText(R.string.game_install)
            }
        }
    }

    private fun handleEvent(event: MainEvent) {
        when (event) {
            MainEvent.OpenRegisterPage -> startActivity(Intent(this, RegisterActivity::class.java))
            is MainEvent.ShowToast -> {
                Toast.makeText(this, event.toast.toStringRes(), Toast.LENGTH_SHORT).show()
                if (event.toast == MainToast.LoginSuccess || event.toast == MainToast.LogoutSuccess) {
                    notifySessionChanged()
                }
            }
        }
    }

    private fun MainToast.toStringRes(): Int {
        return when (this) {
            MainToast.AlreadyLoggedIn -> R.string.already_logged_in
            MainToast.LoginSuccess -> R.string.login_success
            MainToast.LoginFailed -> R.string.login_failed
            MainToast.LogoutSuccess -> R.string.logout_success
            MainToast.GameOpenFailed -> R.string.game_open_failed
            MainToast.GameInstallStarted -> R.string.game_install_started
            MainToast.GameInstallAssetMissing -> R.string.game_install_asset_missing
            MainToast.GameInstallNoHandler -> R.string.game_install_no_handler
            MainToast.GameInstallPermissionFailed -> R.string.game_install_permission_failed
        }
    }

    private fun notifySessionChanged() {
        contentResolver.notifyChange(SessionContract.Session.CONTENT_URI, null)
    }

    private fun applySystemBarInsets() {
        findViewById<android.view.View>(R.id.root).applySystemBarPadding()
    }
}
