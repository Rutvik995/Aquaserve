package com.example.aquaserve.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AquaServePrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ROLE = "user_role" // New constant
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    //save the user
    fun saveUserRole(role: String) {
        prefs.edit().putString(USER_ROLE, role).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    //fetch the user
    fun fetchUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }

    //clear all stored session
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}