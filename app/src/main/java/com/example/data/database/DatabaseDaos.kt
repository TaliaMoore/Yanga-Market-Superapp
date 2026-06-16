package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VibePostDao {
    @Query("SELECT * FROM vibe_posts ORDER BY timestamp DESC")
    fun getAllVibePosts(): Flow<List<VibePostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVibePost(vibePost: VibePostEntity)

    @Query("DELETE FROM vibe_posts WHERE id = :id")
    suspend fun deleteVibePostById(id: String)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: SavedBookingEntity)

    @Query("DELETE FROM saved_bookings WHERE id = :id")
    suspend fun deleteBooking(id: String)
}
