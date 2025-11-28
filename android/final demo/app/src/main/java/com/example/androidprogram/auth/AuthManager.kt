package com.example.androidprogram.auth

import android.content.SharedPreferences

class AuthManager(private val prefs: SharedPreferences) {
    fun isLoggedIn(): Boolean = prefs.getBoolean("logged_in", false)

    fun getPhone(): String? = prefs.getString("phone", null)

    fun login(phone: String) {
        prefs.edit().putBoolean("logged_in", true).putString("phone", phone).apply()
    }

    fun logout() {
        prefs.edit().putBoolean("logged_in", false).remove("phone").apply()
    }
}

