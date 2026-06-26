package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VibePostDao {
    @Query("SELECT * FROM vibe_posts ORDER BY timestamp DESC")
    fun getAllVibePosts(): Flow<List<VibePostEntity>>

    @Query("SELECT * FROM vibe_posts ORDER BY timestamp DESC")
    suspend fun getAllVibePostsDirect(): List<VibePostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVibePost(vibePost: VibePostEntity)

    @Query("DELETE FROM vibe_posts WHERE id = :id")
    suspend fun deleteVibePostById(id: String)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsDirect(): List<WalletTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface SavedBookingDao {
    @Query("SELECT * FROM saved_bookings")
    fun getSavedBookings(): Flow<List<SavedBookingEntity>>

    @Query("SELECT * FROM saved_bookings")
    suspend fun getSavedBookingsDirect(): List<SavedBookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: SavedBookingEntity)

    @Query("DELETE FROM saved_bookings WHERE id = :id")
    suspend fun deleteBooking(id: String)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}

@Dao
interface ManufacturerDao {
    @Query("SELECT * FROM manufacturers ORDER BY name ASC")
    fun getAllManufacturers(): Flow<List<ManufacturerEntity>>

    @Query("SELECT * FROM manufacturers WHERE id = :id")
    suspend fun getManufacturerById(id: String): ManufacturerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManufacturer(manufacturer: ManufacturerEntity)

    @Delete
    suspend fun deleteManufacturer(manufacturer: ManufacturerEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE manufacturerId = :manufacturerId")
    fun getProductsByManufacturer(manufacturerId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface ProductCategoryDao {
    @Query("SELECT * FROM product_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ProductCategoryEntity>>

    @Query("SELECT * FROM product_categories WHERE id = :id")
    suspend fun getCategoryById(id: String): ProductCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ProductCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: ProductCategoryEntity)
}

@Dao
interface LocaleDao {
    @Query("SELECT * FROM locales ORDER BY zip ASC")
    fun getAllLocales(): Flow<List<LocaleEntity>>

    @Query("SELECT * FROM locales WHERE zip = :zip")
    suspend fun getLocaleByZip(zip: String): LocaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocale(locale: LocaleEntity)

    @Delete
    suspend fun deleteLocale(locale: LocaleEntity)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops ORDER BY name ASC")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE id = :id")
    suspend fun getShopById(id: String): ShopEntity?

    @Query("SELECT * FROM shops WHERE zip = :zip")
    fun getShopsByZip(zip: String): Flow<List<ShopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Delete
    suspend fun deleteShop(shop: ShopEntity)
}

