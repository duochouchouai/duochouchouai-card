package com.example.androidprogram.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(stateHolder: LoginViewModel, onLoginSuccess: () -> Unit) {
    val state by stateHolder.state.collectAsState()
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (state.isLoginMode) "登录" else "注册")
            OutlinedTextField(
                value = state.phone,
                onValueChange = { stateHolder.dispatch(LoginIntent.PhoneChanged(it)) },
                label = { Text("手机号") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                isError = state.canSendCode.not(),
                supportingText = { if (state.canSendCode.not()) Text("请输入11位有效手机号") else Text("格式正确") },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { stateHolder.dispatch(LoginIntent.ToggleMode) }) {
                Text(if (state.isLoginMode) "切换到注册" else "切换到登录")
            }
            Button(onClick = { stateHolder.dispatch(LoginIntent.SendCode) }, enabled = state.canSendCode && !state.sendingCode) {
                if (state.sendingCode) CircularProgressIndicator() else Text("发送验证码")
            }
            OutlinedTextField(
                value = state.code,
                onValueChange = { stateHolder.dispatch(LoginIntent.CodeChanged(it)) },
                label = { Text("验证码") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                isError = state.code.length != 6,
                supportingText = { if (state.code.length != 6) Text("请输入6位验证码") else Text("格式正确") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { stateHolder.dispatch(LoginIntent.Submit); onLoginSuccess() }, enabled = state.canSubmit && !state.verifying) {
                if (state.verifying) CircularProgressIndicator() else Text(if (state.isLoginMode) "登录" else "注册")
            }
        }
    }
}
