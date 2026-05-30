package com.bird.starryskyteahouse

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bird.starryskyteahouse.databinding.ActivityRegisterBinding
import com.bird.starryskyteahouse.media.TeaMusic
import com.bird.starryskyteahouse.main.RegisterEvent
import com.bird.starryskyteahouse.main.RegisterScreenController
import com.bird.starryskyteahouse.main.RegisterToast
import com.bird.starryskyteahouse.main.RegisterViewModel
import com.bird.starryskyteahouse.main.RegisterViewModelFactory
import com.bird.starryskyteahouse.session.SessionContract
import com.bird.starryskyteahouse.ui.applySystemBarPadding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var mViewModel: RegisterViewModel
    private lateinit var mTeaMusic: TeaMusic
    private lateinit var mScreenController: RegisterScreenController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mTeaMusic = TeaMusic.getInstance(this)
        mViewModel = ViewModelProvider(this, RegisterViewModelFactory(this))[RegisterViewModel::class.java]
        mBinding.root.applySystemBarPadding()
        mScreenController = RegisterScreenController(mBinding)
        mScreenController.bindActions(
            onSubmit = { registerAndLogin() },
            onBack = { finish() }
        )
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        mTeaMusic.playBackground()
    }

    override fun onPause() {
        mTeaMusic.stopBackground()
        super.onPause()
    }

    private fun registerAndLogin() {
        mViewModel.registerAndLogin(
            mScreenController.username,
            mScreenController.password,
            mScreenController.confirmPassword,
            mScreenController.rememberPassword
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.events.collect { handleEvent(it) }
            }
        }
    }

    private fun handleEvent(event: RegisterEvent) {
        when (event) {
            RegisterEvent.Finish -> finish()
            is RegisterEvent.ShowToast -> {
                Toast.makeText(this, event.toast.toStringRes(), Toast.LENGTH_SHORT).show()
                if (event.toast == RegisterToast.RegisterSuccess) {
                    contentResolver.notifyChange(SessionContract.Session.CONTENT_URI, null)
                }
            }
        }
    }

    private fun RegisterToast.toStringRes(): Int {
        return when (this) {
            RegisterToast.PasswordMismatch -> R.string.password_mismatch
            RegisterToast.RegisterSuccess -> R.string.register_success
            RegisterToast.RegisterFailed -> R.string.register_failed
        }
    }
}
