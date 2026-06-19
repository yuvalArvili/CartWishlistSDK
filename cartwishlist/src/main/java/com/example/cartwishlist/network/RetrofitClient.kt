package com.example.cartwishlist.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var _api: SdkApi? = null

    fun init(baseUrl: String, sdkKey: String, storeId: String): SdkApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("X-SDK-Key", sdkKey)
                    .addHeader("X-Store-ID", storeId)
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SdkApi::class.java)
            .also { _api = it }
    }

    fun getApi(): SdkApi = checkNotNull(_api) {
        "RetrofitClient not initialized — call CartWishlistSdk.init() first"
    }
}
