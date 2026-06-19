package com.example.cartwishlist.network.dto

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    @SerializedName("product_id")    val productId: String,
    @SerializedName("product_name")  val productName: String,
    @SerializedName("product_price") val productPrice: Double,
    @SerializedName("product_image_url") val productImageUrl: String = "",
    @SerializedName("quantity")      val quantity: Int,
)

data class AnalyticsEventDto(
    @SerializedName("event_type")   val eventType: String,
    @SerializedName("product_id")   val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("timestamp")    val timestamp: String, // ISO-8601
)

data class SyncRequest(
    @SerializedName("client_id")         val clientId: String,
    @SerializedName("device_id")         val deviceId: String,
    @SerializedName("cart_items")        val cartItems: List<CartItemDto>,
    @SerializedName("analytics_events") val analyticsEvents: List<AnalyticsEventDto>,
)

data class ShareCartRequest(
    @SerializedName("items") val items: List<CartItemDto>,
)
