# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in
# the Android SDK tools/proguard directory.

# Keep SDK public API
-keep public class com.example.cartwishlist.CartWishlistSdk { *; }
-keep public class com.example.cartwishlist.model.** { *; }
-keep public interface com.example.cartwishlist.storage.StorageProvider { *; }
