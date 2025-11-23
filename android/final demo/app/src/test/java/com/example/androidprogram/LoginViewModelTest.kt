package com.example.androidprogram

import com.example.androidprogram.auth.AuthManager
import com.example.androidprogram.feature.login.LoginIntent
import com.example.androidprogram.feature.login.LoginViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {
    private fun fakePrefs(): android.content.SharedPreferences = object : android.content.SharedPreferences {
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

    @Test
    fun phoneAndCodeValidation() {
        val vm = LoginViewModel(AuthManager(fakePrefs()))
        vm.dispatch(LoginIntent.PhoneChanged("13300000000"))
        assertTrue(vm.state.value.canSendCode)
        vm.dispatch(LoginIntent.CodeChanged("123456"))
        assertTrue(vm.state.value.canSubmit)
        vm.dispatch(LoginIntent.CodeChanged("12345"))
        assertFalse(vm.state.value.canSubmit)
    }
}

