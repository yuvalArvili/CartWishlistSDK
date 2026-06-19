package com.example.cartwishlist.network

import com.example.cartwishlist.network.dto.*
import retrofit2.http.*

interface SdkApi {

    @POST("api/sync")
    suspend fun sync(@Body request: SyncRequest): SyncResponse

    @POST("api/share")
    suspend fun shareCart(@Body request: ShareCartRequest): ShareCartResponse

    @GET("api/share/{shortId}")
    suspend fun getSharedCart(@Path("shortId") shortId: String): SharedCartResponse

    @GET("api/analytics")
    suspend fun getAnalytics(): AnalyticsResponse

    @GET("api/products")
    suspend fun fetchProducts(): List<ProductDto>
}
