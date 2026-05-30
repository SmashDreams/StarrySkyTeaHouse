package com.bird.starryskyteahouse

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.bird.starryskyteahouse.databinding.ActivityMainBinding
import com.bird.starryskyteahouse.media.TeaMusic
import com.bird.starryskyteahouse.main.MainEvent
import com.bird.starryskyteahouse.main.MainScreenRenderer
import com.bird.starryskyteahouse.main.MainToast
import com.bird.starryskyteahouse.main.MainViewModel
import com.bird.starryskyteahouse.main.MainViewModelFactory
import com.bird.starryskyteahouse.session.SessionContract
import com.bird.starryskyteahouse.ui.applySystemBarPadding
import com.bird.starryskyteahouse.ui.setTeaClickListener
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mViewModel: MainViewModel
    private lateinit var mTeaMusic: TeaMusic
    private lateinit var mScreenRenderer: MainScreenRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mTeaMusic = TeaMusic.getInstance(this)
        mViewModel = ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]
        applySystemBarInsets()
        mScreenRenderer = MainScreenRenderer(this, mBinding)
        bindActions()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        mTeaMusic.playBackground()
        mViewModel.refresh()
    }

    override fun onPause() {
        mTeaMusic.stopBackground()
        super.onPause()
    }

    private fun bindActions() {
        mBinding.loginButton.setTeaClickListener {
            mViewModel.login(
                mScreenRenderer.username,
                mScreenRenderer.password,
                mScreenRenderer.rememberPassword
            )
        }
        mBinding.registerButton.setTeaClickListener {
            mViewModel.openRegisterPage()
        }
        mBinding.logoutButton.setTeaClickListener {
            mViewModel.logout()
        }
        mBinding.gameActionButton.setTeaClickListener {
            mViewModel.handleGameAction()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { mViewModel.uiState.collect { state -> mScreenRenderer.render(state) } }
                launch { mViewModel.events.collect { handleEvent(it) } }
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
        mBinding.root.applySystemBarPadding()
    }
}
