package com.example.mobileappdevelopment.api

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"

    private var sharedPreferences: SharedPreferences? = null
    private var token: String? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        token = sharedPreferences?.getString(KEY_TOKEN, null)
    }

    fun saveToken(newToken: String) {
        token = newToken
        sharedPreferences?.edit()?.putString(KEY_TOKEN, newToken)?.apply()
    }

    fun getToken(): String? = token

    fun clearToken() {
        token = null
        sharedPreferences?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun hasToken(): Boolean = !token.isNullOrEmpty()
}