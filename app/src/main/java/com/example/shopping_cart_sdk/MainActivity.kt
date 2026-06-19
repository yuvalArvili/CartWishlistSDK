package com.example.shopping_cart_sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import coil.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cartwishlist.model.CartItem
import com.example.cartwishlist.model.Product
import com.example.cartwishlist.model.WishlistItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SdkDemoScreen()
                }
            }
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun SdkDemoScreen(vm: MainViewModel = viewModel()) {
    val productsState by vm.productsState.collectAsStateWithLifecycle()
    val cartItems     by vm.cartItems.collectAsStateWithLifecycle()
    val wishlistItems by vm.wishlistItems.collectAsStateWithLifecycle()
    val analytics     by vm.analytics.collectAsStateWithLifecycle()
    val shareCode     by vm.shareCode.collectAsStateWithLifecycle()
    val importCode    by vm.importCode.collectAsStateWithLifecycle()
    val importStatus  by vm.importStatus.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "CartWishlist SDK Demo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                SdkDemoApp.SERVER_URL,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }

        // ── Storefront ──────────────────────────────────────────────────────
        item {
            SectionCard(title = "Storefront") {
                when (val state = productsState) {
                    is ProductsUiState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Loading products…", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    is ProductsUiState.Empty -> {
                        EmptyState(
                            message = "No products available.\nAdd items via the Portal.",
                            onRetry = vm::fetchProducts,
                        )
                    }

                    is ProductsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = vm::fetchProducts,
                        )
                    }

                    is ProductsUiState.Ready -> {
                        state.products.forEachIndexed { index, product ->
                            val inWishlist = vm.isInWishlist(product.id)
                            val inCart = cartItems.any { it.product.id == product.id }
                            ProductRow(
                                product    = product,
                                inCart     = inCart,
                                inWishlist = inWishlist,
                                onAddCart  = { vm.addToCart(product) },
                                onAddWish  = { vm.addToWishlist(product) },
                            )
                            if (index < state.products.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            }
                        }
                    }
                }
            }
        }

        // ── Cart ────────────────────────────────────────────────────────────
        item {
            SectionCard(
                title = "Cart  •  ${cartItems.sumOf { it.quantity }} item(s)  •  $${"%.2f".format(cartItems.sumOf { it.totalPrice })}"
            ) {
                if (cartItems.isEmpty()) {
                    Text("Cart is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    cartItems.forEach { item ->
                        CartItemRow(
                            item         = item,
                            onIncrease   = { vm.increaseQty(item.product.id) },
                            onDecrease   = { vm.decreaseQty(item.product.id) },
                            onRemove     = { vm.removeFromCart(item.product.id) },
                            onMoveToWish = { vm.moveToWishlist(item.product.id) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.shareCart() }, enabled = cartItems.isNotEmpty()) {
                        Text("Share Cart")
                    }
                    OutlinedButton(onClick = { vm.clearCart() }, enabled = cartItems.isNotEmpty()) {
                        Text("Clear Cart")
                    }
                }

                shareCode?.let { code ->
                    Spacer(Modifier.height(8.dp))
                    Text("Share code:", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    Text(code,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp))
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Import shared cart", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                ImportCartRow(
                    code         = importCode,
                    status       = importStatus,
                    onCodeChange = vm::onImportCodeChange,
                    onImport     = vm::importCart,
                )
            }
        }

        // ── Wishlist ────────────────────────────────────────────────────────
        item {
            SectionCard(title = "Wishlist  •  ${wishlistItems.size} item(s)") {
                if (wishlistItems.isEmpty()) {
                    Text("Wishlist is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    wishlistItems.forEach { item ->
                        WishlistItemRow(
                            item       = item,
                            onMoveCart = { vm.moveToCart(item.product.id) },
                            onRemove   = { vm.removeFromWishlist(item.product.id) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = vm::clearWishlist, enabled = wishlistItems.isNotEmpty()) {
                    Text("Clear Wishlist")
                }
            }
        }

        // ── Analytics ───────────────────────────────────────────────────────
        item {
            SectionCard(title = "Analytics") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatChip("Cart Adds",   analytics.cartAdds)
                    StatChip("Clears",      analytics.cartClears)
                    StatChip("Shares",      analytics.cartShares)
                    StatChip("Wish Adds",   analytics.wishlistAdds)
                }
                if (analytics.topCartedProducts.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Top carted", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    analytics.topCartedProducts.forEachIndexed { i, stat ->
                        Text("${i + 1}. ${stat.productName}  ×${stat.count}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp))
                    }
                }
                if (analytics.topWishlistedProducts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Top wishlisted", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    analytics.topWishlistedProducts.forEachIndexed { i, stat ->
                        Text("${i + 1}. ${stat.productName}  ×${stat.count}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = vm::resetAnalytics) { Text("Reset Analytics") }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── State composables ──────────────────────────────────────────────────────────

@Composable
private fun EmptyState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("📦", fontSize = 36.sp)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) { Text("Refresh") }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("⚠ Could not load products", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
        Text(message, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onRetry) { Text("Retry") }
    }
}

// ── Shared composables ─────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ProductRow(
    product: Product, inCart: Boolean, inWishlist: Boolean,
    onAddCart: () -> Unit, onAddWish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Product image
        if (product.imageUrl.isNotBlank()) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text("📦", fontSize = 22.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
            Text("$${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (inWishlist || inCart) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (inCart) Text("✓ In cart", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary)
                    if (inWishlist) Text("♥ Wishlisted", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(onClick = onAddWish,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)) {
                Text("Wish", fontSize = 11.sp)
            }
            Button(onClick = onAddCart,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)) {
                Text("+ Cart", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit,
    onRemove: () -> Unit, onMoveToWish: () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text("$${"%.2f".format(item.totalPrice)}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onDecrease, modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp)) {
                Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text("${item.quantity}", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
            TextButton(onClick = onIncrease, modifier = Modifier.size(32.dp),
                contentPadding = PaddingValues(0.dp)) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onMoveToWish,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text("→ Wishlist", fontSize = 11.sp)
            }
            TextButton(onClick = onRemove,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text("Remove", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun WishlistItemRow(item: WishlistItem, onMoveCart: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
            Text("$${"%.2f".format(item.product.price)}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onMoveCart,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
            Text("→ Cart", fontSize = 11.sp)
        }
        TextButton(onClick = onRemove,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
            Text("Remove", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ImportCartRow(
    code: String, status: String?,
    onCodeChange: (String) -> Unit, onImport: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = code, onValueChange = onCodeChange,
            placeholder = { Text("Paste share code", fontSize = 12.sp) },
            singleLine = true, modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); onImport() }),
        )
        Button(onClick = { keyboard?.hide(); onImport() }) { Text("Import") }
    }
    status?.let {
        Text(it, style = MaterialTheme.typography.bodySmall,
            color = if (it.startsWith("Import")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun StatChip(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
