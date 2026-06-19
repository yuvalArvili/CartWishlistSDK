package com.example.cartwishlist.storage

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsStorageProvider(context: Context) : StorageProvider {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "cartwishlist_sdk_prefs"
    }
}
