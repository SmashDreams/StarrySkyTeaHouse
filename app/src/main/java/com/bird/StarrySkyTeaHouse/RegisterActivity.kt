package com.bird.StarrySkyTeaHouse

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bird.StarrySkyTeaHouse.session.SessionContract
import com.bird.StarrySkyTeaHouse.session.SessionStore
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {
    private lateinit var sessionStore: SessionStore
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var rememberPasswordCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        applySystemBarInsets()

        sessionStore = SessionStore(this)
        usernameInput = findViewById(R.id.register_username_input)
        passwordInput = findViewById(R.id.register_password_input)
        confirmPasswordInput = findViewById(R.id.register_confirm_password_input)
        rememberPasswordCheckBox = findViewById(R.id.register_remember_password)
        findViewById<MaterialButton>(R.id.register_submit_button).setOnClickListener { registerAndLogin() }
        findViewById<MaterialButton>(R.id.register_back_button).setOnClickListener { finish() }
    }

    private fun registerAndLogin() {
        val password = passwordInput.text.toString()
        if (password != confirmPasswordInput.text.toString()) {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show()
            return
        }

        if (sessionStore.register(usernameInput.text.toString(), password, rememberPasswordCheckBox.isChecked)) {
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show()
            contentResolver.notifyChange(SessionContract.Session.CONTENT_URI, null)
            finish()
        } else {
            Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show()
        }
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
}
