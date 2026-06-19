package com.example.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- Room Entities for persistence ---

@Entity(tableName = "vibe_posts")
data class VibePostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val content: String,
    val vibeCount: Int,
    val isVibeChecked: Boolean,
    val timestamp: Long,
    val commentsJson: String // Comments list serialized to JSON string
)

@Entity(
    tableName = "wallet_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val customerId: String? = null,
    val type: String,
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val securityHash: String
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val quantity: Int,
    val itemType: String // "FOOD", "FRUIT", "RETAIL"
)

@Entity(tableName = "saved_bookings")
data class SavedBookingEntity(
    @PrimaryKey val id: String, // can be eventId, hospitalBookingId, or restaurantId
    val bookingType: String,    // "EVENT", "HOSPITAL", "RESTAURANT"
    val title: String,
    val subtitle: String,       // e.g., location, selected service, cuisine
    val dateOrTime: String,
    val price: Double,
    val extraDetails: String    // JSON or custom string representation
)

// --- Moshi Type Converters for Room ---
class DatabaseConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value == null) return emptyList()
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(listType)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list == null) return "[]"
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(listType)
        return adapter.toJson(list)
    }
}

// --- Customer, Product, and Manufacturer relational schema entities ---

@Entity(tableName = "locales")
data class LocaleEntity(
    @PrimaryKey val zip: String,
    val city: String,
    val state: String
)

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = LocaleEntity::class,
            parentColumns = ["zip"],
            childColumns = ["zip"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val zip: String? = null
)

@Entity(tableName = "manufacturers")
data class ManufacturerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val contactEmail: String
)

@Entity(tableName = "product_categories")
data class ProductCategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ManufacturerEntity::class,
            parentColumns = ["id"],
            childColumns = ["manufacturerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double, // Matches DECIMAL representation in SQLite
    val quantity: Int,
    val manufacturerId: String,
    val categoryId: String
)

@Entity(
    tableName = "shops",
    foreignKeys = [
        ForeignKey(
            entity = LocaleEntity::class,
            parentColumns = ["zip"],
            childColumns = ["zip"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val specialty: String,
    val distanceKm: Double,
    val zip: String
)

