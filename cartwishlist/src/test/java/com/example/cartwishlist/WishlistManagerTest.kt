package com.example.cartwishlist

import com.example.cartwishlist.cart.CartManager
import com.example.cartwishlist.model.Product
import com.example.cartwishlist.wishlist.WishlistManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WishlistManagerTest {

    private lateinit var wishlist: WishlistManager
    private lateinit var cart: CartManager

    private val shoes = Product("p2", "Running Shoes", 89.99)
    private val hat = Product("p3", "Winter Hat", 19.99)

    @Before
    fun setUp() {
        val storage = InMemoryStorageProvider()
        cart = CartManager(storage)
        wishlist = WishlistManager(storage)
        wishlist.attachCartManager(cart)
    }

    @Test
    fun addItem_productIsStored() {
        wishlist.addItem(shoes)
        assertEquals(1, wishlist.getItems().size)
        assertEquals(shoes, wishlist.getItems().first().product)
    }

    @Test
    fun addItem_duplicateIsIgnored() {
        wishlist.addItem(shoes)
        wishlist.addItem(shoes)
        assertEquals(1, wishlist.getItems().size)
    }

    @Test
    fun addItem_recordsTimestamp() {
        val before = System.currentTimeMillis()
        wishlist.addItem(shoes)
        val after = System.currentTimeMillis()
        val addedAt = wishlist.getItems().first().addedAt
        assertTrue(addedAt in before..after)
    }

    @Test
    fun removeItem_productIsRemoved() {
        wishlist.addItem(shoes)
        wishlist.removeItem(shoes.id)
        assertTrue(wishlist.getItems().isEmpty())
    }

    @Test
    fun removeItem_nonexistentId_doesNothing() {
        wishlist.addItem(shoes)
        wishlist.removeItem("nonexistent")
        assertEquals(1, wishlist.getItems().size)
    }

    @Test
    fun contains_returnsTrueForExistingProduct() {
        wishlist.addItem(shoes)
        assertTrue(wishlist.contains(shoes.id))
    }

    @Test
    fun contains_returnsFalseForMissingProduct() {
        assertFalse(wishlist.contains("nonexistent"))
    }

    @Test
    fun moveToCart_itemAddedToCartAndRemovedFromWishlist() {
        wishlist.addItem(shoes)
        wishlist.moveToCart(shoes.id)

        assertFalse(wishlist.contains(shoes.id))
        assertEquals(1, cart.getItems().size)
        assertEquals(shoes, cart.getItems().first().product)
        assertEquals(1, cart.getItems().first().quantity)
    }

    @Test
    fun moveToCart_nonexistentId_doesNothing() {
        wishlist.addItem(shoes)
        wishlist.moveToCart("nonexistent")

        assertEquals(1, wishlist.getItems().size)
        assertTrue(cart.getItems().isEmpty())
    }

    @Test
    fun clearWishlist_emptiesAllItems() {
        wishlist.addItem(shoes)
        wishlist.addItem(hat)
        wishlist.clearWishlist()
        assertTrue(wishlist.getItems().isEmpty())
    }
}
