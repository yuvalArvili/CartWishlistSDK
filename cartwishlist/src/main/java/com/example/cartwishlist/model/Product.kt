package com.example.cartwishlist.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String = "",
    val description: String = ""
)
