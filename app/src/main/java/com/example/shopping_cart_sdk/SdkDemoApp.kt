package com.example.shopping_cart_sdk

import android.app.Application
import com.example.cartwishlist.CartWishlistSdk

class SdkDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CartWishlistSdk.init(
            context   = this,
            sdkKey    = "cwsk_live_a7f2d901e4b83c6f3f9a",
            serverUrl = SERVER_URL,
            storeId   = STORE_ID,
        )
    }

    companion object {
        // Emulator → 10.0.2.2  |  Physical device → your PC's LAN IP
        const val SERVER_URL = "http://10.0.2.2:8000/"
        const val STORE_ID   = "store_demo_001"
    }
}
