package com.example.cartwishlist

import com.example.cartwishlist.cart.CartManager
import com.example.cartwishlist.model.Product
import com.example.cartwishlist.wishlist.WishlistManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartManagerTest {

    private lateinit var cart: CartManager
    private lateinit var wishlist: WishlistManager

    private val shirt = Product("p1", "Blue T-Shirt", 29.99, imageUrl = "https://example.com/shirt.jpg")
    private val shoes = Product("p2", "Running Shoes", 89.99)

    @Before
    fun setUp() {
        val storage = InMemoryStorageProvider()
        cart = CartManager(storage)
        wishlist = WishlistManager(storage)
        cart.attachWishlistManager(wishlist)
        wishlist.attachCartManager(cart)
    }

    @Test
    fun addItem_newProduct_isStored() {
        cart.addItem(shirt)
        assertEquals(1, cart.getItems().size)
        assertEquals(shirt, cart.getItems().first().product)
    }

    @Test
    fun addItem_existingProduct_accumulatesQuantity() {
        cart.addItem(shirt, 2)
        cart.addItem(shirt, 3)
        assertEquals(5, cart.getItems().first().quantity)
    }

    @Test
    fun addItem_defaultQuantityIsOne() {
        cart.addItem(shirt)
        assertEquals(1, cart.getItems().first().quantity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun addItem_zeroQuantity_throws() {
        cart.addItem(shirt, 0)
    }

    @Test
    fun removeItem_productIsRemoved() {
        cart.addItem(shirt)
        cart.removeItem(shirt.id)
        assertTrue(cart.getItems().isEmpty())
    }

    @Test
    fun removeItem_nonexistentId_doesNothing() {
        cart.addItem(shirt)
        cart.removeItem("nonexistent")
        assertEquals(1, cart.getItems().size)
    }

    @Test
    fun updateQuantity_setsNewQuantity() {
        cart.addItem(shirt, 1)
        cart.updateQuantity(shirt.id, 4)
        assertEquals(4, cart.getItems().first().quantity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateQuantity_zeroQuantity_throws() {
        cart.addItem(shirt)
        cart.updateQuantity(shirt.id, 0)
    }

    @Test
    fun getTotalPrice_returnsSumOfAllItemTotals() {
        cart.addItem(shirt, 2)   // 2 * 29.99 = 59.98
        cart.addItem(shoes, 1)   // 1 * 89.99 = 89.99
        assertEquals(149.97, cart.getTotalPrice(), 0.001)
    }

    @Test
    fun getItemCount_returnsTotalQuantityAcrossItems() {
        cart.addItem(shirt, 3)
        cart.addItem(shoes, 2)
        assertEquals(5, cart.getItemCount())
    }

    @Test
    fun clearCart_emptiesAllItems() {
        cart.addItem(shirt)
        cart.addItem(shoes)
        cart.clearCart()
        assertTrue(cart.getItems().isEmpty())
    }

    @Test
    fun shareCart_loadSharedCart_roundTripPreservesItems() {
        cart.addItem(shirt, 2)
        cart.addItem(shoes, 1)

        val code = cart.shareCart()
        assertFalse(code.isEmpty())

        val cart2 = CartManager(InMemoryStorageProvider())
        cart2.loadSharedCart(code)

        assertEquals(2, cart2.getItems().size)
        assertEquals(2, cart2.getItems().first { it.product.id == shirt.id }.quantity)
        assertEquals(1, cart2.getItems().first { it.product.id == shoes.id }.quantity)
    }

    @Test
    fun shareCart_emptyCart_producesValidCode() {
        val code = cart.shareCart()
        val cart2 = CartManager(InMemoryStorageProvider())
        cart2.loadSharedCart(code)
        assertTrue(cart2.getItems().isEmpty())
    }

    @Test
    fun moveToWishlist_itemAddedToWishlistAndRemovedFromCart() {
        cart.addItem(shirt)
        cart.moveToWishlist(shirt.id)

        assertTrue(cart.getItems().isEmpty())
        assertTrue(wishlist.contains(shirt.id))
        assertEquals(shirt, wishlist.getItems().first().product)
    }

    @Test
    fun moveToWishlist_nonexistentId_doesNothing() {
        cart.addItem(shirt)
        cart.moveToWishlist("nonexistent")

        assertEquals(1, cart.getItems().size)
        assertFalse(wishlist.contains("nonexistent"))
    }

    @Test
    fun moveToWishlist_itemAlreadyInWishlist_doesNotDuplicate() {
        wishlist.addItem(shirt)
        cart.addItem(shirt)
        cart.moveToWishlist(shirt.id)

        assertTrue(cart.getItems().isEmpty())
        assertEquals(1, wishlist.getItems().size)
    }
}
