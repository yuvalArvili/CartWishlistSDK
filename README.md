# Cart & Wishlist Android SDK

An Android library that provides out-of-the-box shopping cart and wishlist functionality for Android applications. Host apps integrate the SDK with a single `init` call and gain persistent, offline-capable cart and wishlist management with no backend required.

---

## Project Structure

```
CartWishlistSDK/
├── app/                          # Demo application
│   └── src/main/java/com/example/shopping_cart_sdk/
│       ├── MainActivity.kt
│       └── ui/
│           ├── home/HomeFragment.kt          # Cart demo
│           ├── dashboard/DashboardFragment.kt # Wishlist demo
│           └── notifications/NotificationsFragment.kt # Share demo
│
├── cartwishlist/                 # SDK library module
│   └── src/main/java/com/example/cartwishlist/
│       ├── CartWishlistSdk.kt    # Public entry point (singleton)
│       ├── cart/
│       │   └── CartManager.kt
│       ├── wishlist/
│       │   └── WishlistManager.kt
│       ├── model/
│       │   ├── Product.kt
│       │   ├── CartItem.kt
│       │   └── WishlistItem.kt
│       └── storage/
│           ├── StorageProvider.kt
│           └── SharedPrefsStorageProvider.kt
│
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

---

## Features

### Shopping Cart
| Method | Description |
|---|---|
| `addItem(product, quantity)` | Add a product; accumulates quantity if already present |
| `removeItem(productId)` | Remove a product by ID |
| `updateQuantity(productId, quantity)` | Set exact quantity for an existing item |
| `getItems()` | Return all cart items |
| `getTotalPrice()` | Sum of `price × quantity` for all items |
| `getItemCount()` | Total unit count across all items |
| `clearCart()` | Remove all items |
| `shareCart()` | Encode cart as a Base64 URL-safe share code |
| `loadSharedCart(code)` | Import a cart from a share code |
| `moveToWishlist(productId)` | Move a cart item into the wishlist |

### Wishlist
| Method | Description |
|---|---|
| `addItem(product)` | Save a product (duplicates ignored) |
| `removeItem(productId)` | Remove a product by ID |
| `getItems()` | Return all wishlist items with timestamps |
| `contains(productId)` | Check whether a product is saved |
| `clearWishlist()` | Remove all items |
| `moveToCart(productId)` | Move a saved product into the cart |

### Cart Sharing
`shareCart()` serialises the current cart to JSON, encodes it with Base64 URL-safe encoding, and returns a compact string. The recipient calls `loadSharedCart(code)` on their device to import the cart. No server required.

---

## Architecture

```
CartWishlistSdk (singleton)
├── CartManager     ──► StorageProvider (interface)
└── WishlistManager ──►     └── SharedPrefsStorageProvider (default)
         │
         └── (CartManager injected for moveToCart)
```

**Metadata Injection pattern** — the SDK stores no product catalogue. The host app passes product details (name, price, imageUrl) as a `Product` object at the moment of add, which is serialised alongside the item. Cart retrieval returns full product data instantly without querying the host's backend.

---

## Installation

Add the `:cartwishlist` module as a dependency in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":cartwishlist"))
}
```

---

## Usage

### 1. Initialize the SDK

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CartWishlistSdk.init(this)
    }
}
```

### 2. Cart operations

```kotlin
val product = Product(id = "p1", name = "Blue T-Shirt", price = 29.99, imageUrl = "https://…")

CartWishlistSdk.cart.addItem(product, quantity = 2)
CartWishlistSdk.cart.updateQuantity("p1", 3)
CartWishlistSdk.cart.removeItem("p1")

val items: List<CartItem> = CartWishlistSdk.cart.getItems()
val total: Double        = CartWishlistSdk.cart.getTotalPrice()
val count: Int           = CartWishlistSdk.cart.getItemCount()

CartWishlistSdk.cart.clearCart()
```

### 3. Wishlist operations

```kotlin
CartWishlistSdk.wishlist.addItem(product)

val saved: Boolean           = CartWishlistSdk.wishlist.contains("p1")
val items: List<WishlistItem> = CartWishlistSdk.wishlist.getItems()

CartWishlistSdk.wishlist.moveToCart("p1")   // moves to cart, removes from wishlist
CartWishlistSdk.wishlist.removeItem("p1")
CartWishlistSdk.wishlist.clearWishlist()
```

### 4. Cart sharing

```kotlin
// Sender
val code: String = CartWishlistSdk.cart.shareCart()
// Share `code` via clipboard, QR, deep-link, etc.

// Recipient
CartWishlistSdk.cart.loadSharedCart(code)
```

### 5. Custom storage backend

Implement `StorageProvider` to swap in any persistence layer:

```kotlin
class RoomStorageProvider(db: AppDatabase) : StorageProvider {
    override fun saveString(key: String, value: String) { /* Room write */ }
    override fun getString(key: String): String?        { /* Room read  */ return null }
    override fun remove(key: String)                    { /* Room delete */ }
    override fun clear()                                { /* Room clear  */ }
}
```

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | API 26 (Android 8.0) |
| Compile SDK | API 35 |
| Build system | Gradle 8 with Kotlin DSL |
| Serialization | Gson 2.10.1 |
| Encoding | `java.util.Base64` (URL-safe) |
| Storage | Android SharedPreferences |

---

## Development Status

| Feature | Status |
|---|---|
| Data models (`Product`, `CartItem`, `WishlistItem`) | Done |
| `StorageProvider` interface + `SharedPrefsStorageProvider` | Done |
| `CartManager` (add, remove, update, total, count, clear) | Done |
| `CartManager.shareCart()` / `loadSharedCart()` | Done |
| `WishlistManager` (add, remove, contains, clear) | Done |
| `WishlistManager.moveToCart()` | Done |
| `CartManager.moveToWishlist()` | Done |
| Unit tests (`CartManagerTest`, `WishlistManagerTest`) | Done |
| Demo app (Cart, Wishlist, Share tabs) | Done |
