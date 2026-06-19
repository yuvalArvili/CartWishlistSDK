package com.example.shopping_cart_sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartwishlist.CartWishlistSdk
import com.example.cartwishlist.model.AnalyticsSnapshot
import com.example.cartwishlist.model.CartItem
import com.example.cartwishlist.model.Product
import com.example.cartwishlist.model.WishlistItem
import com.example.cartwishlist.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductsUiState {
    object Loading : ProductsUiState()
    object Empty   : ProductsUiState()
    data class Ready(val products: List<Product>) : ProductsUiState()
    data class Error(val message: String)         : ProductsUiState()
}

class MainViewModel : ViewModel() {

    private val _productsState  = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    private val _cartItems      = MutableStateFlow<List<CartItem>>(emptyList())
    private val _wishlistItems  = MutableStateFlow<List<WishlistItem>>(emptyList())
    private val _analytics      = MutableStateFlow(CartWishlistSdk.analytics.getSnapshot())
    private val _shareCode      = MutableStateFlow<String?>(null)
    private val _importCode     = MutableStateFlow("")
    private val _importStatus   = MutableStateFlow<String?>(null)

    val productsState:  StateFlow<ProductsUiState>   = _productsState.asStateFlow()
    val cartItems:      StateFlow<List<CartItem>>     = _cartItems.asStateFlow()
    val wishlistItems:  StateFlow<List<WishlistItem>> = _wishlistItems.asStateFlow()
    val analytics:      StateFlow<AnalyticsSnapshot>  = _analytics.asStateFlow()
    val shareCode:      StateFlow<String?>            = _shareCode.asStateFlow()
    val importCode:     StateFlow<String>             = _importCode.asStateFlow()
    val importStatus:   StateFlow<String?>            = _importStatus.asStateFlow()

    init {
        fetchProducts()
        refreshCart()
    }

    // ── Products ──────────────────────────────────────────────────────────────

    fun fetchProducts() {
        viewModelScope.launch {
            _productsState.value = ProductsUiState.Loading
            try {
                val dtos = RetrofitClient.getApi().fetchProducts()
                val serverBase = SdkDemoApp.SERVER_URL.trimEnd('/')
                val products = dtos.map {
                    val fixedUrl = it.imageUrl.replace("http://localhost:8000", serverBase)
                    Product(it.id, it.name, it.price, fixedUrl, it.description)
                }
                _productsState.value =
                    if (products.isEmpty()) ProductsUiState.Empty
                    else ProductsUiState.Ready(products)
            } catch (e: Exception) {
                _productsState.value = ProductsUiState.Error(
                    e.message?.take(120) ?: "Could not reach server"
                )
            }
        }
    }

    // ── Cart ──────────────────────────────────────────────────────────────────

    fun addToCart(product: Product) {
        CartWishlistSdk.cart.addItem(product)
        refreshCart()
    }

    fun removeFromCart(productId: String) {
        CartWishlistSdk.cart.removeItem(productId)
        refreshCart()
    }

    fun increaseQty(productId: String) {
        val current = _cartItems.value.firstOrNull { it.product.id == productId }?.quantity ?: return
        CartWishlistSdk.cart.updateQuantity(productId, current + 1)
        refreshCart()
    }

    fun decreaseQty(productId: String) {
        val current = _cartItems.value.firstOrNull { it.product.id == productId }?.quantity ?: return
        if (current <= 1) CartWishlistSdk.cart.removeItem(productId)
        else CartWishlistSdk.cart.updateQuantity(productId, current - 1)
        refreshCart()
    }

    fun moveToWishlist(productId: String) {
        CartWishlistSdk.cart.moveToWishlist(productId)
        refreshCart()
    }

    fun clearCart() {
        CartWishlistSdk.cart.clearCart()
        _shareCode.value = null
        refreshCart()
    }

    fun shareCart() {
        _shareCode.value = CartWishlistSdk.cart.shareCart()
        refreshCart()
    }

    fun onImportCodeChange(code: String) { _importCode.value = code }

    fun importCart() {
        val code = _importCode.value.trim()
        if (code.isBlank()) { _importStatus.value = "Paste a share code first."; return }
        try {
            CartWishlistSdk.cart.loadSharedCart(code)
            val count = CartWishlistSdk.cart.getItemCount()
            _importStatus.value = "Imported! $count item(s) loaded."
            _importCode.value = ""
        } catch (e: Exception) {
            _importStatus.value = "Invalid code."
        }
        refreshCart()
    }

    // ── Wishlist ──────────────────────────────────────────────────────────────

    fun addToWishlist(product: Product) {
        CartWishlistSdk.wishlist.addItem(product)
        refreshCart()
    }

    fun removeFromWishlist(productId: String) {
        CartWishlistSdk.wishlist.removeItem(productId)
        refreshCart()
    }

    fun moveToCart(productId: String) {
        CartWishlistSdk.wishlist.moveToCart(productId)
        refreshCart()
    }

    fun clearWishlist() {
        CartWishlistSdk.wishlist.clearWishlist()
        refreshCart()
    }

    fun isInWishlist(productId: String): Boolean =
        CartWishlistSdk.wishlist.contains(productId)

    // ── Analytics ─────────────────────────────────────────────────────────────

    fun resetAnalytics() {
        CartWishlistSdk.analytics.reset()
        refreshCart()
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun refreshCart() {
        _cartItems.value     = CartWishlistSdk.cart.getItems()
        _wishlistItems.value = CartWishlistSdk.wishlist.getItems()
        _analytics.value     = CartWishlistSdk.analytics.getSnapshot()
    }
}
