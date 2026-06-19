package com.example.cartwishlist

import com.example.cartwishlist.storage.StorageProvider

class InMemoryStorageProvider : StorageProvider {
    private val map = mutableMapOf<String, String>()
    override fun saveString(key: String, value: String) { map[key] = value }
    override fun getString(key: String): String? = map[key]
    override fun remove(key: String) { map.remove(key) }
    override fun clear() { map.clear() }
}
