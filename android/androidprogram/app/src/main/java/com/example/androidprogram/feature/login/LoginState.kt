package com.example.androidprogram.feature.login

data class LoginState(
    val phone: String = "",
    val code: String = "",
    val sendingCode: Boolean = false,
    val verifying: Boolean = false,
    val error: String? = null,
    val isLoginMode: Boolean = true,
    val canSendCode: Boolean = false,
    val canSubmit: Boolean = false
)

