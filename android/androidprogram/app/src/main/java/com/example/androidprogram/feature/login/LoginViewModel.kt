package com.example.androidprogram.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidprogram.auth.AuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: AuthManager) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun dispatch(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneChanged -> {
                val phone = intent.phone
                _state.update {
                    val canSend = phone.matches(Regex("^1\\d{10}$"))
                    it.copy(phone = phone, canSendCode = canSend, error = null)
                }
            }
            LoginIntent.SendCode -> {
                if (!_state.value.canSendCode) return
                viewModelScope.launch {
                    _state.update { it.copy(sendingCode = true, error = null) }
                    delay(500)
                    _state.update { it.copy(sendingCode = false) }
                }
            }
            is LoginIntent.CodeChanged -> {
                val code = intent.code
                _state.update {
                    val canSubmit = it.canSendCode && code.length == 6
                    it.copy(code = code, canSubmit = canSubmit)
                }
            }
            LoginIntent.Submit -> {
                if (!_state.value.canSubmit) return
                viewModelScope.launch {
                    _state.update { it.copy(verifying = true, error = null) }
                    delay(500)
                    auth.login(_state.value.phone)
                    _state.update { it.copy(verifying = false) }
                }
            }
            LoginIntent.ToggleMode -> {
                _state.update { it.copy(isLoginMode = !it.isLoginMode) }
            }
        }
    }
}

