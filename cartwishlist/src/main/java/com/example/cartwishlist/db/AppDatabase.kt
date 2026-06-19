package com.example.cartwishlist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cartwishlist.storage.KvDao
import com.example.cartwishlist.storage.KvEntry

@Database(
    entities = [
        CartItemEntity::class,
        WishlistItemEntity::class,
        AnalyticsEventEntity::class,
        KvEntry::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun analyticsEventDao(): AnalyticsEventDao
    abstract fun kvDao(): KvDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cartwishlist.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
