package com.example.androidprogram.feature.login

sealed class LoginIntent {
    data class PhoneChanged(val phone: String) : LoginIntent()
    object SendCode : LoginIntent()
    data class CodeChanged(val code: String) : LoginIntent()
    object Submit : LoginIntent()
    object ToggleMode : LoginIntent()
}

