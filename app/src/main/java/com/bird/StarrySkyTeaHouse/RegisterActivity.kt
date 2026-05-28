package com.bird.StarrySkyTeaHouse

import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bird.StarrySkyTeaHouse.main.RegisterEvent
import com.bird.StarrySkyTeaHouse.main.RegisterToast
import com.bird.StarrySkyTeaHouse.main.RegisterViewModel
import com.bird.StarrySkyTeaHouse.main.RegisterViewModelFactory
import com.bird.StarrySkyTeaHouse.session.SessionContract
import com.bird.StarrySkyTeaHouse.ui.applySystemBarPadding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var mViewModel: RegisterViewModel
    private lateinit var mUsernameInput: EditText
    private lateinit var mPasswordInput: EditText
    private lateinit var mConfirmPasswordInput: EditText
    private lateinit var mRememberPasswordCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mViewModel = ViewModelProvider(this, RegisterViewModelFactory(this))[RegisterViewModel::class.java]
        findViewById<android.view.View>(R.id.root).applySystemBarPadding()
        bindViews()
        bindActions()
        observeViewModel()
    }

    private fun bindViews() {
        mUsernameInput = findViewById(R.id.register_username_input)
        mPasswordInput = findViewById(R.id.register_password_input)
        mConfirmPasswordInput = findViewById(R.id.register_confirm_password_input)
        mRememberPasswordCheckBox = findViewById(R.id.register_remember_password)
    }

    private fun bindActions() {
        findViewById<MaterialButton>(R.id.register_submit_button).setOnClickListener {
            mViewModel.registerAndLogin(
                mUsernameInput.text.toString(),
                mPasswordInput.text.toString(),
                mConfirmPasswordInput.text.toString(),
                mRememberPasswordCheckBox.isChecked
            )
        }
        findViewById<MaterialButton>(R.id.register_back_button).setOnClickListener { finish() }
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
