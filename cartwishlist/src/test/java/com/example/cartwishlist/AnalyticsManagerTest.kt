package com.example.cartwishlist

import com.example.cartwishlist.analytics.AnalyticsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AnalyticsManagerTest {

    private lateinit var analytics: AnalyticsManager

    @Before
    fun setUp() {
        analytics = AnalyticsManager(InMemoryStorageProvider())
    }

    @Test
    fun initialSnapshot_allCountsAreZero() {
        val snap = analytics.getSnapshot()
        assertEquals(0, snap.cartAdds)
        assertEquals(0, snap.cartClears)
        assertEquals(0, snap.cartShares)
        assertEquals(0, snap.wishlistAdds)
        assertTrue(snap.topCartedProducts.isEmpty())
        assertTrue(snap.topWishlistedProducts.isEmpty())
    }

    @Test
    fun recordCartAdd_incrementsCount() {
        analytics.recordCartAdd("p1", "Shirt")
        analytics.recordCartAdd("p1", "Shirt")
        analytics.recordCartAdd("p2", "Shoes")
        assertEquals(3, analytics.getSnapshot().cartAdds)
    }

    @Test
    fun recordCartClear_incrementsCount() {
        analytics.recordCartClear()
        analytics.recordCartClear()
        assertEquals(2, analytics.getSnapshot().cartClears)
    }

    @Test
    fun recordCartShare_incrementsCount() {
        analytics.recordCartShare()
        assertEquals(1, analytics.getSnapshot().cartShares)
    }

    @Test
    fun recordWishlistAdd_incrementsCount() {
        analytics.recordWishlistAdd("p1", "Shirt")
        analytics.recordWishlistAdd("p2", "Shoes")
        assertEquals(2, analytics.getSnapshot().wishlistAdds)
    }

    @Test
    fun topCartedProducts_sortedByCountDescending() {
        analytics.recordCartAdd("p1", "Shirt")
        analytics.recordCartAdd("p2", "Shoes")
        analytics.recordCartAdd("p2", "Shoes")
        analytics.recordCartAdd("p3", "Watch")
        analytics.recordCartAdd("p3", "Watch")
        analytics.recordCartAdd("p3", "Watch")

        val top = analytics.getSnapshot().topCartedProducts
        assertEquals("p3", top[0].productId)
        assertEquals(3, top[0].count)
        assertEquals("p2", top[1].productId)
        assertEquals(2, top[1].count)
    }

    @Test
    fun topWishlistedProducts_returnsCorrectProductName() {
        analytics.recordWishlistAdd("p1", "Blue T-Shirt")
        analytics.recordWishlistAdd("p1", "Blue T-Shirt")

        val top = analytics.getSnapshot().topWishlistedProducts
        assertEquals(1, top.size)
        assertEquals("Blue T-Shirt", top[0].productName)
        assertEquals(2, top[0].count)
    }

    @Test
    fun topProducts_capsAtFive() {
        for (i in 1..8) {
            analytics.recordCartAdd("p$i", "Product $i")
        }
        assertTrue(analytics.getSnapshot().topCartedProducts.size <= 5)
    }

    @Test
    fun reset_clearsAllData() {
        analytics.recordCartAdd("p1", "Shirt")
        analytics.recordCartShare()
        analytics.reset()
        val snap = analytics.getSnapshot()
        assertEquals(0, snap.cartAdds)
        assertEquals(0, snap.cartShares)
    }
}
