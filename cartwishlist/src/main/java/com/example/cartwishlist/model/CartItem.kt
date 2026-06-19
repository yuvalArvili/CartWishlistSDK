package com.example.cartwishlist.model

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val totalPrice: Double get() = product.price * quantity
}
