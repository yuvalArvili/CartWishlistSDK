package com.example.cartwishlist.model

data class WishlistItem(
    val product: Product,
    val addedAt: Long = System.currentTimeMillis()
)
