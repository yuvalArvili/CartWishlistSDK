package com.example.cartwishlist.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CartItemEntity>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getById(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun delete(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun deleteAll()
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items ORDER BY addedAt DESC")
    suspend fun getAll(): List<WishlistItemEntity>

    @Query("SELECT * FROM wishlist_items WHERE productId = :productId")
    suspend fun getById(productId: String): WishlistItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun delete(productId: String)

    @Query("DELETE FROM wishlist_items")
    suspend fun deleteAll()
}

@Dao
interface AnalyticsEventDao {
    @Insert
    suspend fun insert(event: AnalyticsEventEntity)

    @Query("SELECT * FROM analytics_events WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<AnalyticsEventEntity>

    @Query("UPDATE analytics_events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Int>)

    @Query("DELETE FROM analytics_events WHERE synced = 1")
    suspend fun deleteAllSynced()
}
