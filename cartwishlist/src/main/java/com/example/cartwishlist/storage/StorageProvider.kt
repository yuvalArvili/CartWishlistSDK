package com.example.cartwishlist.storage

interface StorageProvider {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clear()
}
