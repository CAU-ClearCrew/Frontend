package com.example.mobileappdevelopment.util

import android.content.Context
import android.content.SharedPreferences

object ZkKeyManager {
    private const val PREF_NAME = "zk_keys"
    private const val KEY_CUSTOM_NULLIFIER = "custom_nullifier"
    private const val KEY_SECRET = "secret"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveKeys(context: Context, customNullifier: String, secret: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_CUSTOM_NULLIFIER, customNullifier)
        editor.putString(KEY_SECRET, secret)
        editor.apply()
    }

    fun getCustomNullifier(context: Context): String? {
        return getPreferences(context).getString(KEY_CUSTOM_NULLIFIER, null)
    }

    fun getSecret(context: Context): String? {
        return getPreferences(context).getString(KEY_SECRET, null)
    }

    fun clearKeys(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
