package com.bird.starryskyteahouse.main

import com.bird.starryskyteahouse.databinding.ActivityRegisterBinding
import com.bird.starryskyteahouse.ui.setTeaClickListener

class RegisterScreenController(
    private val mBinding: ActivityRegisterBinding
) {
    val username: String
        get() = mBinding.registerUsernameInput.text.toString()

    val password: String
        get() = mBinding.registerPasswordInput.text.toString()

    val confirmPassword: String
        get() = mBinding.registerConfirmPasswordInput.text.toString()

    val rememberPassword: Boolean
        get() = mBinding.registerRememberPassword.isChecked

    fun bindActions(
        onSubmit: () -> Unit,
        onBack: () -> Unit
    ) {
        mBinding.registerSubmitButton.setTeaClickListener {
            onSubmit()
        }
        mBinding.registerBackButton.setTeaClickListener {
            onBack()
        }
    }
}
