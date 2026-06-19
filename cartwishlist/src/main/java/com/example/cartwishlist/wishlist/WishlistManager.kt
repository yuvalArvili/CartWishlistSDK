package com.example.cartwishlist.wishlist

import com.example.cartwishlist.analytics.AnalyticsManager
import com.example.cartwishlist.cart.CartManager
import com.example.cartwishlist.model.Product
import com.example.cartwishlist.model.WishlistItem
import com.example.cartwishlist.storage.StorageProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WishlistManager(private val storage: StorageProvider) {

    private val gson = Gson()
    private var cartManager: CartManager? = null
    internal var analytics: AnalyticsManager? = null

    internal fun attachCartManager(cart: CartManager) {
        cartManager = cart
    }

    private fun loadItems(): MutableList<WishlistItem> {
        val json = storage.getString(KEY_WISHLIST) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<WishlistItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveItems(items: List<WishlistItem>) {
        storage.saveString(KEY_WISHLIST, gson.toJson(items))
    }

    fun addItem(product: Product) {
        val items = loadItems()
        if (items.none { it.product.id == product.id }) {
            items.add(WishlistItem(product))
            saveItems(items)
            analytics?.recordWishlistAdd(product.id, product.name)
        }
    }

    fun removeItem(productId: String) {
        saveItems(loadItems().filter { it.product.id != productId })
    }

    fun getItems(): List<WishlistItem> = loadItems()

    fun contains(productId: String): Boolean = loadItems().any { it.product.id == productId }

    fun clearWishlist() = storage.remove(KEY_WISHLIST)

    fun moveToCart(productId: String) {
        val cart = checkNotNull(cartManager) {
            "CartManager not attached — call CartWishlistSdk.init() first"
        }
        val item = loadItems().firstOrNull { it.product.id == productId } ?: return
        cart.addItem(item.product)
        removeItem(productId)
    }

    companion object {
        private const val KEY_WISHLIST = "wishlist_items"
    }
}
