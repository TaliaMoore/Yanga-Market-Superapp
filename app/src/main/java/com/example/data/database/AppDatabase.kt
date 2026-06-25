package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        VibePostEntity::class,
        WalletTransactionEntity::class,
        CartItemEntity::class,
        SavedBookingEntity::class,
        CustomerEntity::class,
        ManufacturerEntity::class,
        ProductEntity::class,
        LocaleEntity::class,
        ShopEntity::class,
        ProductCategoryEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vibePostDao(): VibePostDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun savedBookingDao(): SavedBookingDao
    abstract fun customerDao(): CustomerDao
    abstract fun manufacturerDao(): ManufacturerDao
    abstract fun productDao(): ProductDao
    abstract fun localeDao(): LocaleDao
    abstract fun shopDao(): ShopDao
    abstract fun productCategoryDao(): ProductCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yanga_market_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
