package com.example.cartwishlist.storage

import androidx.room.*
import com.example.cartwishlist.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class RoomStorageProvider(db: AppDatabase) : StorageProvider {

    private val dao = db.kvDao()

    override fun saveString(key: String, value: String) =
        runBlocking(Dispatchers.IO) { dao.put(KvEntry(key, value)) }

    override fun getString(key: String): String? =
        runBlocking(Dispatchers.IO) { dao.get(key) }

    override fun remove(key: String) =
        runBlocking(Dispatchers.IO) { dao.delete(key) }

    override fun clear() =
        runBlocking(Dispatchers.IO) { dao.deleteAll() }
}

@Entity(tableName = "kv_store")
data class KvEntry(
    @PrimaryKey val key: String,
    val value: String,
)

@Dao
interface KvDao {
    @Query("SELECT value FROM kv_store WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: KvEntry)

    @Query("DELETE FROM kv_store WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM kv_store")
    suspend fun deleteAll()
}
