package com.example.data.graphql

import com.example.domain.model.*
import com.example.data.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.util.UUID

class MockGraphQLClient(private val database: AppDatabase) {

    // Seed mock data for memory-based catalogs
    val foodCatalog = listOf(
        FoodItem(name = "Jollof Rice & Spicy Chicken", price = 2200.0, category = "Meals", description = "Classic smoky Nigerian Jollof rice served with grilled peppered chicken and plantains."),
        FoodItem(name = "Yam Fries & Pepper Sauce", price = 1500.0, category = "Snacks", description = "Vibrant crispy golden yam fries served with traditional hot chili pepper sauce."),
        FoodItem(name = "Pounded Yam with Egusi Soup", price = 2800.0, category = "Traditional", description = "Classic heavy pounded yam paste served with rich, delicious Egusi (melon seed) soup with assorted meat."),
        FoodItem(name = "Suya Spiced Beef Burger", price = 1900.0, category = "Burgers", description = "Juicy flame-grilled double-patty beef burger rubbed with authentic Hausa suya spice blend."),
        FoodItem(name = "Exotic Fruit Bowl Platter", price = 1200.0, category = "Fruits", description = "Vibrant mixture of fresh Pawpaw, Mango, Pineapple, Watermelon, and Mint.", isFruit = true),
        FoodItem(name = "Sweet Pawpaw & Mango Slices", price = 950.0, category = "Fruits", description = "Succulent ripe organic pawpaw paired with premium sweet mango chunks.", isFruit = true),
        FoodItem(name = "Avocado & Banana Smoothie Pack", price = 1350.0, category = "Fruits", description = "Ready-to-blend mix of fresh green butter avocados and sweet local yellow bananas.", isFruit = true)
    )

    val retailShopsCatalog = listOf(
        RetailShop(name = "Balogun Fashion World", specialty = "Fashion & Attire", distanceKm = 1.2, items = listOf(
            RetailItem(name = "Ankara Playful Summer Dress", price = 4500.0, category = "Clothing"),
            RetailItem(name = "Custom Velvet Agbada Set", price = 18500.0, category = "Luxury"),
            RetailItem(name = "Yanga Custom Purple Socks", price = 800.0, category = "Accessories")
        )),
        RetailShop(name = "Alaba Tech Gadgets Hub", specialty = "Electronics", distanceKm = 3.6, items = listOf(
            RetailItem(name = "Playful LED RGB Headphones", price = 12000.0, category = "Audio"),
            RetailItem(name = "10000mAh Compact Powerbank", price = 6500.0, category = "Power"),
            RetailItem(name = "USB-C Braided Fast Charger", price = 2500.0, category = "Cables")
        )),
        RetailShop(name = "Mega Rite Supermarket", specialty = "Groceries & Kitchen", distanceKm = 0.5, items = listOf(
            RetailItem(name = "Yanga Premium Chocolate Bark", price = 2200.0, category = "Snack"),
            RetailItem(name = "1kg Long Grain Parboiled Rice", price = 3400.0, category = "Pantry"),
            RetailItem(name = "Fresh Organic Coconut Oil", price = 1800.0, category = "Kitchen")
        ))
    )

    val eventCatalog = listOf(
        Event(title = "Yanga Vibes Festival & Concert", host = "Yanga Entertainment", date = "June 25, 2026", time = "18:00", venue = "Eko Atlantic, VI", price = 5000.0, rsvpCount = 284),
        Event(title = "Tech & Suya Networking Night", host = "Lagos Techies Cohort", date = "June 29, 2026", time = "17:30", venue = "The Zone Tech Hub, Gbagada", price = 0.0, rsvpCount = 145),
        Event(title = "Culinary African Food Expo 2026", host = "Chow Chefs Initiative", date = "July 03, 2026", time = "11:00", venue = "Landmark Centre, Victoria Island", price = 2500.0, rsvpCount = 92)
    )

    val hospitalCatalog = listOf(
        Hospital(name = "St. Nicholas Premium Hospital", location = "Campus Square, Lagos Island", distanceKm = 1.1, specialties = listOf("General Wellness", "Pediatrics", "Post-natal Diagnostics", "Cardiology")),
        Hospital(name = "Reddington Multi-Specialist Clinic", location = "Adetokunbo Ademola St, VI", distanceKm = 2.4, specialties = listOf("Emergency General Medicine", "Dental Surgery", "Advanced Ultrasound Lab")),
        Hospital(name = "Evercare Hospital Lekki", location = "Lekki Phase 1, Lagos", distanceKm = 4.8, specialties = listOf("MRI & Lab Radiography", "Immunization", "Ophthalmology"))
    )

    val restaurantCatalog = listOf(
        Restaurant(name = "The Playful Pepper Lounge", cuisine = "Contemporary African Fusion", rating = 4.7, address = "Akin Adesola St, VI", tablePrice = 1500.0),
        Restaurant(name = "Panda Wok Express", cuisine = "Pan-Asian Style Noodles", rating = 4.2, address = "Lekki Expressway, Phase 1", tablePrice = 2000.0),
        Restaurant(name = "Bungalow Grill & Pizza", cuisine = "Gourmet Burgers & Italian", rating = 4.5, address = "Adeola Hopewell Street, VI", tablePrice = 1200.0)
    )

    /**
     * Executes a GraphQL query/mutation using mock processing.
     * Coroutine delay simulates network latency.
     */
    suspend fun executeGraphQL(request: GraphQLRequest): GraphQLResponse<Any> {
        // Enforce safe network delay
        delay(350)

        val normQuery = request.query.trim().replace("\\s+".toRegex(), " ")

        return when {
            // --- MUTATION PROTOCOLS ---
            normQuery.contains("mutation FundWallet") -> {
                val amount = (request.variables["amount"] as? Number)?.toDouble() ?: 0.0
                handleFundWalletMutation(amount)
            }
            normQuery.contains("mutation PayWithWallet") -> {
                val amount = (request.variables["amount"] as? Number)?.toDouble() ?: 0.0
                val note = (request.variables["note"] as? String) ?: "Yanga Superapp Purchase"
                handlePayMutation(amount, note)
            }
            normQuery.contains("mutation PostVibe") -> {
                val author = (request.variables["author"] as? String) ?: "AnonymousViber"
                val content = (request.variables["content"] as? String) ?: "Empty Vibe"
                handlePostVibeMutation(author, content)
            }
            normQuery.contains("mutation ReactionVibe") -> {
                val postId = (request.variables["id"] as? String) ?: ""
                handleVibeCheckingReaction(postId)
            }
            normQuery.contains("mutation BookService") -> {
                val title = (request.variables["title"] as? String) ?: "Booking"
                val subtitle = (request.variables["subtitle"] as? String) ?: ""
                val price = (request.variables["price"] as? Number)?.toDouble() ?: 0.0
                val dateOrTime = (request.variables["dateOrTime"] as? String) ?: "Today"
                val type = (request.variables["type"] as? String) ?: "RESTAURANT"
                handleCreateBookingMutation(title, subtitle, price, dateOrTime, type)
            }
            normQuery.contains("mutation DeleteBooking") -> {
                 val id = (request.variables["id"] as? String) ?: ""
                 database.savedBookingDao().deleteBooking(id)
                 successResponse(mapOf("success" to true, "deletedId" to id))
            }

            // --- QUERY PROTOCOLS ---
            normQuery.contains("query GetFoodsAndFruits") -> {
                successResponse(mapOf("foods" to foodCatalog))
            }
            normQuery.contains("query GetRetailShops") -> {
                successResponse(mapOf("shops" to retailShopsCatalog))
            }
            normQuery.contains("query GetUpcomingEvents") -> {
                successResponse(mapOf("events" to eventCatalog))
            }
            normQuery.contains("query GetHospitals") -> {
                successResponse(mapOf("hospitals" to hospitalCatalog))
            }
            normQuery.contains("query GetRestaurants") -> {
                successResponse(mapOf("restaurants" to restaurantCatalog))
            }
            else -> {
                errorResponse("GraphQL parsing error: Unrecognized operation or query format.")
            }
        }
    }

    // --- Resolver implementations ---

    private suspend fun handleFundWalletMutation(amount: Double): GraphQLResponse<Any> {
        if (amount <= 0) {
            return errorResponse("Mutation error: Deposit amount must be positive.")
        }
        val securityHash = generateLedgerSecurityHash("FUND", amount, System.currentTimeMillis())
        val txEntity = WalletTransactionEntity(
            id = "TXN-FUND-${UUID.randomUUID().toString().take(6).uppercase()}",
            type = "FUND",
            amount = amount,
            description = "Funded wallet via Secure Card Payment",
            timestamp = System.currentTimeMillis(),
            securityHash = securityHash
        )
        database.walletTransactionDao().insertTransaction(txEntity)
        return successResponse(mapOf("balanceFunded" to amount, "success" to true))
    }

    private suspend fun handlePayMutation(amount: Double, note: String): GraphQLResponse<Any> {
        val txs = database.walletTransactionDao().getAllTransactions().first()
        val currentBalance = calculateBalanceFromEntities(txs)
        if (currentBalance < amount) {
            return errorResponse("Insufficient Wallet Balance! Current: ₦$currentBalance. Charge attempt: ₦$amount. Please fund your wallet.")
        }

        val securityHash = generateLedgerSecurityHash("PAYMENT", amount, System.currentTimeMillis())
        val txEntity = WalletTransactionEntity(
            id = "TXN-PAY-${UUID.randomUUID().toString().take(6).uppercase()}",
            type = "PAYMENT",
            amount = amount,
            description = note,
            timestamp = System.currentTimeMillis(),
            securityHash = securityHash
        )
        database.walletTransactionDao().insertTransaction(txEntity)
        return successResponse(mapOf("paymentCharged" to amount, "success" to true, "note" to note))
    }

    private suspend fun handlePostVibeMutation(author: String, content: String): GraphQLResponse<Any> {
        val id = UUID.randomUUID().toString()
        val vibeEntity = VibePostEntity(
            id = id,
            author = author,
            content = content,
            vibeCount = 0,
            isVibeChecked = false,
            timestamp = System.currentTimeMillis(),
            commentsJson = "[]"
        )
        database.vibePostDao().insertVibePost(vibeEntity)
        return successResponse(vibeEntity)
    }

    private suspend fun handleVibeCheckingReaction(postId: String): GraphQLResponse<Any> {
        val currentPosts = database.vibePostDao().getAllVibePosts().first()
        val match = currentPosts.find { it.id == postId }
            ?: return errorResponse("Vibe target ID '$postId' does not exist.")

        val updated = match.copy(
            isVibeChecked = !match.isVibeChecked,
            vibeCount = if (match.isVibeChecked) match.vibeCount - 1 else match.vibeCount + 1
        )
        database.vibePostDao().insertVibePost(updated)
        return successResponse(updated)
    }

    private suspend fun handleCreateBookingMutation(
        title: String,
        subtitle: String,
        price: Double,
        dateOrTime: String,
        type: String
    ): GraphQLResponse<Any> {
        val savedId = UUID.randomUUID().toString().take(8).uppercase()
        val entity = SavedBookingEntity(
            id = savedId,
            bookingType = type,
            title = title,
            subtitle = subtitle,
            dateOrTime = dateOrTime,
            price = price,
            extraDetails = "{}"
        )
        database.savedBookingDao().insertBooking(entity)
        return successResponse(mapOf("success" to true, "bookingId" to savedId, "type" to type))
    }

    // --- Audit Log cryptographers ---

    private fun calculateBalanceFromEntities(txEntities: List<WalletTransactionEntity>): Double {
        var base = 10000.0 // Starting bonus of ₦10,000 for Yanga Market superapp test users!
        for (tx in txEntities) {
            when (tx.type) {
                "FUND" -> base += tx.amount
                "PAYMENT" -> base -= tx.amount
            }
        }
        return base
    }

    private fun generateLedgerSecurityHash(type: String, amount: Double, timestamp: Long): String {
        return try {
            val bytes = "$type-$amount-$timestamp-YANGAWALLETLOCKEDSECURITYKEY".toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            digest.fold("") { str, it -> str + "%02x".format(it) }.take(24)
        } catch (e: Exception) {
            "SECURE_HASH_SYS_ERR"
        }
    }
}
