package com.example.cartwishlist.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String = "",
    val productDescription: String = "",
    val quantity: Int,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String = "",
    val productDescription: String = "",
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val productId: String,
    val productName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
)
