package com.example.cartwishlist.analytics

import com.example.cartwishlist.model.AnalyticsSnapshot
import com.example.cartwishlist.model.ProductStat
import com.example.cartwishlist.storage.StorageProvider
import com.google.gson.Gson

class AnalyticsManager(private val storage: StorageProvider) {

    private val gson = Gson()

    private data class State(
        val cartAdds: Int = 0,
        val cartClears: Int = 0,
        val cartShares: Int = 0,
        val wishlistAdds: Int = 0,
        val cartProductCounts: Map<String, Int> = emptyMap(),
        val wishlistProductCounts: Map<String, Int> = emptyMap(),
        val productNames: Map<String, String> = emptyMap()
    )

    private fun load(): State {
        val json = storage.getString(KEY) ?: return State()
        return gson.fromJson(json, State::class.java) ?: State()
    }

    private fun save(state: State) = storage.saveString(KEY, gson.toJson(state))

    fun recordCartAdd(productId: String, productName: String) {
        val s = load()
        val counts = s.cartProductCounts.toMutableMap().also { it[productId] = (it[productId] ?: 0) + 1 }
        val names = s.productNames.toMutableMap().also { it[productId] = productName }
        save(s.copy(cartAdds = s.cartAdds + 1, cartProductCounts = counts, productNames = names))
    }

    fun recordCartClear() {
        val s = load(); save(s.copy(cartClears = s.cartClears + 1))
    }

    fun recordCartShare() {
        val s = load(); save(s.copy(cartShares = s.cartShares + 1))
    }

    fun recordWishlistAdd(productId: String, productName: String) {
        val s = load()
        val counts = s.wishlistProductCounts.toMutableMap().also { it[productId] = (it[productId] ?: 0) + 1 }
        val names = s.productNames.toMutableMap().also { it[productId] = productName }
        save(s.copy(wishlistAdds = s.wishlistAdds + 1, wishlistProductCounts = counts, productNames = names))
    }

    fun getSnapshot(): AnalyticsSnapshot {
        val s = load()
        fun topList(counts: Map<String, Int>) = counts.entries
            .sortedByDescending { it.value }.take(5)
            .map { ProductStat(it.key, s.productNames[it.key] ?: it.key, it.value) }
        return AnalyticsSnapshot(
            cartAdds = s.cartAdds,
            cartClears = s.cartClears,
            cartShares = s.cartShares,
            wishlistAdds = s.wishlistAdds,
            topWishlistedProducts = topList(s.wishlistProductCounts),
            topCartedProducts = topList(s.cartProductCounts)
        )
    }

    fun reset() = storage.remove(KEY)

    companion object {
        private const val KEY = "sdk_analytics"
    }
}
