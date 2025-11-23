package com.example.androidprogram

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.androidprogram.auth.AuthManager
import com.example.androidprogram.feature.login.LoginScreen
import com.example.androidprogram.feature.login.LoginViewModel
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun sendCodeEnabledWithValidPhone() {
        val sp = object : android.content.SharedPreferences {
            private val map = mutableMapOf<String, Any?>()
            override fun getAll(): MutableMap<String, *> = map
            override fun getString(key: String?, defValue: String?): String? = map[key] as String?
            override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = map[key] as MutableSet<String>?
            override fun getInt(key: String?, defValue: Int): Int = map[key] as Int? ?: defValue
            override fun getLong(key: String?, defValue: Long): Long = map[key] as Long? ?: defValue
            override fun getFloat(key: String?, defValue: Float): Float = map[key] as Float? ?: defValue
            override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as Boolean? ?: defValue
            override fun contains(key: String?): Boolean = map.containsKey(key)
            override fun edit(): android.content.SharedPreferences.Editor = object : android.content.SharedPreferences.Editor {
                override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor { map[key!!] = value; return this }
                override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor { map[key!!] = values; return this }
                override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor { map[key!!] = value; return this }
                override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor { map[key!!] = value; return this }
                override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor { map[key!!] = value; return this }
                override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor { map[key!!] = value; return this }
                override fun remove(key: String?): android.content.SharedPreferences.Editor { map.remove(key!!); return this }
                override fun clear(): android.content.SharedPreferences.Editor { map.clear(); return this }
                override fun commit(): Boolean = true
                override fun apply() {}
            }
            override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
            override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
        }
        val vm = LoginViewModel(AuthManager(sp))
        composeTestRule.setContent { LoginScreen(stateHolder = vm, onLoginSuccess = {}) }
        composeTestRule.onNodeWithText("手机号").performTextInput("13300000000")
        composeTestRule.onNodeWithText("发送验证码").assertIsEnabled()
    }
}
