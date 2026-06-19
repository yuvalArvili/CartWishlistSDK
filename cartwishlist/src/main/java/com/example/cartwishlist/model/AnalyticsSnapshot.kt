package com.example.cartwishlist.model

data class AnalyticsSnapshot(
    val cartAdds: Int,
    val cartClears: Int,
    val cartShares: Int,
    val wishlistAdds: Int,
    val topWishlistedProducts: List<ProductStat>,
    val topCartedProducts: List<ProductStat>
)
