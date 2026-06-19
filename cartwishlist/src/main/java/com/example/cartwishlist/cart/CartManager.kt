package com.example.cartwishlist.cart

import com.example.cartwishlist.analytics.AnalyticsManager
import com.example.cartwishlist.model.CartItem
import com.example.cartwishlist.model.Product
import com.example.cartwishlist.storage.StorageProvider
import com.example.cartwishlist.wishlist.WishlistManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Base64

class CartManager(private val storage: StorageProvider) {

    private val gson = Gson()
    private var wishlistManager: WishlistManager? = null
    internal var analytics: AnalyticsManager? = null

    internal fun attachWishlistManager(wishlist: WishlistManager) {
        wishlistManager = wishlist
    }

    private fun loadItems(): MutableList<CartItem> {
        val json = storage.getString(KEY_CART) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<CartItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveItems(items: List<CartItem>) {
        storage.saveString(KEY_CART, gson.toJson(items))
    }

    fun addItem(product: Product, quantity: Int = 1) {
        require(quantity > 0) { "quantity must be > 0" }
        val items = loadItems()
        val index = items.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            items[index] = items[index].copy(quantity = items[index].quantity + quantity)
        } else {
            items.add(CartItem(product, quantity))
        }
        saveItems(items)
        analytics?.recordCartAdd(product.id, product.name)
    }

    fun removeItem(productId: String) {
        saveItems(loadItems().filter { it.product.id != productId })
    }

    fun updateQuantity(productId: String, quantity: Int) {
        require(quantity > 0) { "quantity must be > 0" }
        val items = loadItems()
        val index = items.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            items[index] = items[index].copy(quantity = quantity)
            saveItems(items)
        }
    }

    fun getItems(): List<CartItem> = loadItems()

    fun getTotalPrice(): Double = loadItems().sumOf { it.totalPrice }

    fun getItemCount(): Int = loadItems().sumOf { it.quantity }

    fun clearCart() {
        storage.remove(KEY_CART)
        analytics?.recordCartClear()
    }

    fun shareCart(): String {
        analytics?.recordCartShare()
        val json = gson.toJson(loadItems())
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toByteArray(Charsets.UTF_8))
    }

    fun loadSharedCart(shareCode: String) {
        val json = String(Base64.getUrlDecoder().decode(shareCode), Charsets.UTF_8)
        val type = object : TypeToken<List<CartItem>>() {}.type
        val items: List<CartItem> = gson.fromJson(json, type) ?: emptyList()
        saveItems(items)
    }

    fun moveToWishlist(productId: String) {
        val wishlist = checkNotNull(wishlistManager) {
            "WishlistManager not attached — call CartWishlistSdk.init() first"
        }
        val item = loadItems().firstOrNull { it.product.id == productId } ?: return
        wishlist.addItem(item.product)
        removeItem(productId)
    }

    companion object {
        private const val KEY_CART = "cart_items"
    }
}
