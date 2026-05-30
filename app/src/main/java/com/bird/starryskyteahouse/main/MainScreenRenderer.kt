package com.bird.starryskyteahouse.main

import android.content.Context
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.bird.starryskyteahouse.R
import com.bird.starryskyteahouse.databinding.ActivityMainBinding

class MainScreenRenderer(
    private val mContext: Context,
    private val mBinding: ActivityMainBinding
) {
    private val mRecordsAdapter = RecordsAdapter(mContext)

    val username: String
        get() = mBinding.usernameInput.text.toString()

    val password: String
        get() = mBinding.passwordInput.text.toString()

    val rememberPassword: Boolean
        get() = mBinding.rememberPasswordCheckbox.isChecked

    init {
        mBinding.recordsContainer.layoutManager = LinearLayoutManager(mContext)
        mBinding.recordsContainer.adapter = mRecordsAdapter
    }

    fun render(state: MainUiState) {
        renderLoginInputs(state)
        renderCurrentUser(state)
        renderGameEntry(state.gameEntryState)
        mRecordsAdapter.render(state.recordsState)
    }

    private fun renderLoginInputs(state: MainUiState) {
        syncInputText(mBinding.usernameInput, state.lastLoginUsername.orEmpty())
        mBinding.rememberPasswordCheckbox.isChecked = state.rememberPassword
        val password = if (state.rememberPassword) state.rememberedPassword.orEmpty() else ""
        syncInputText(mBinding.passwordInput, password)
    }

    private fun syncInputText(input: EditText, value: String) {
        if (!input.hasFocus() && input.text.toString() != value) {
            input.setText(value)
            input.setSelection(input.text.length)
        }
    }

    private fun renderCurrentUser(state: MainUiState) {
        if (state.isLoggedIn && !state.currentUsername.isNullOrEmpty()) {
            mBinding.currentUser.text = mContext.getString(
                R.string.current_user_logged_in,
                state.currentUsername
            )
        } else {
            mBinding.currentUser.setText(R.string.current_user_logged_out)
        }
    }

    private fun renderGameEntry(state: GameEntryUiState) {
        when (state) {
            GameEntryUiState.Installed -> {
                mBinding.gameStatus.setText(R.string.game_status_installed)
                mBinding.gameActionButton.setText(R.string.game_open)
            }
            GameEntryUiState.NotInstalled -> {
                mBinding.gameStatus.setText(R.string.game_status_not_installed)
                mBinding.gameActionButton.setText(R.string.game_install)
            }
        }
    }
}
