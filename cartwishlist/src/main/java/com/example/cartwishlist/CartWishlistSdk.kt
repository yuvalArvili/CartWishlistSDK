package com.example.cartwishlist

import android.content.Context
import com.example.cartwishlist.analytics.AnalyticsManager
import com.example.cartwishlist.cart.CartManager
import com.example.cartwishlist.db.AppDatabase
import com.example.cartwishlist.network.RetrofitClient
import com.example.cartwishlist.storage.RoomStorageProvider
import com.example.cartwishlist.storage.SharedPrefsStorageProvider
import com.example.cartwishlist.storage.StorageProvider
import com.example.cartwishlist.sync.SyncWorker
import com.example.cartwishlist.wishlist.WishlistManager

object CartWishlistSdk {

    private lateinit var _cart: CartManager
    private lateinit var _wishlist: WishlistManager
    private lateinit var _analytics: AnalyticsManager
    private var _sdkKey: String = ""
    private var _clientId: String = ""
    private var _deviceId: String = ""

    val cart: CartManager       get() = checkInitialized(_cart)
    val wishlist: WishlistManager get() = checkInitialized(_wishlist)
    val analytics: AnalyticsManager get() = checkInitialized(_analytics)
    val sdkKey: String          get() = _sdkKey

    /**
     * @param context       Application context.
     * @param sdkKey        API key shown in the web portal. Empty string disables network sync.
     * @param serverUrl     Base URL of the FastAPI backend, e.g. "http://10.0.2.2:8000/".
     *                      Null = local-only mode (SharedPreferences, no sync).
     * @param clientId      Stable ID identifying this merchant/app installation.
     * @param deviceId      Stable ID identifying this specific device (e.g. Settings.Secure.ANDROID_ID).
     */
    fun init(
        context: Context,
        sdkKey: String = "",
        serverUrl: String? = null,
        storeId: String = "default",
        clientId: String = "default_client",
        deviceId: String = "default_device",
    ) {
        _sdkKey   = sdkKey
        _clientId = clientId
        _deviceId = deviceId

        val storage: StorageProvider = if (serverUrl != null) {
            val db = AppDatabase.getInstance(context)
            RetrofitClient.init(serverUrl, sdkKey, storeId)
            SyncWorker.enqueueRecurring(context, clientId, deviceId)
            RoomStorageProvider(db)
        } else {
            SharedPrefsStorageProvider(context.applicationContext)
        }

        _analytics = AnalyticsManager(storage)
        _cart      = CartManager(storage).also { it.analytics = _analytics }
        _wishlist  = WishlistManager(storage).also { it.analytics = _analytics }
        _wishlist.attachCartManager(_cart)
        _cart.attachWishlistManager(_wishlist)
    }

    /** Trigger an immediate sync (e.g. call this after a checkout). */
    fun syncNow(context: Context) {
        SyncWorker.enqueueOnce(context, _clientId, _deviceId)
    }

    private fun <T> checkInitialized(value: T): T {
        check(::_cart.isInitialized) {
            "CartWishlistSdk must be initialized — call CartWishlistSdk.init(context) first"
        }
        return value
    }
}
