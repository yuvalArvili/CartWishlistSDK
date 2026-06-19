package com.example.cartwishlist.network.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id")          val id: String,
    @SerializedName("name")        val name: String,
    @SerializedName("price")       val price: Double,
    @SerializedName("image_url")   val imageUrl: String = "",
    @SerializedName("description") val description: String = "",
)

data class SyncResponse(
    @SerializedName("synced_at") val syncedAt: String,
    @SerializedName("cart_id")   val cartId: Int,
)

data class ShareCartResponse(
    @SerializedName("short_id") val shortId: String,
)

data class SharedCartResponse(
    @SerializedName("short_id")   val shortId: String,
    @SerializedName("items")      val items: List<CartItemDto>,
    @SerializedName("created_at") val createdAt: String,
)

data class ProductStatDto(
    @SerializedName("product_id")   val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("count")        val count: Int,
)

data class AnalyticsResponse(
    @SerializedName("cart_adds")       val cartAdds: Int,
    @SerializedName("cart_clears")     val cartClears: Int,
    @SerializedName("cart_shares")     val cartShares: Int,
    @SerializedName("wishlist_adds")   val wishlistAdds: Int,
    @SerializedName("active_carts")    val activeCarts: Int,
    @SerializedName("top_wishlisted")  val topWishlisted: List<ProductStatDto>,
    @SerializedName("top_carted")      val topCarted: List<ProductStatDto>,
)
