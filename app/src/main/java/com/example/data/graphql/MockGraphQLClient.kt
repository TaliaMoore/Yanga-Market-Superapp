package com.example.data.graphql

import com.example.domain.model.*
import com.example.data.database.*
import androidx.room.withTransaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.util.UUID

class MockGraphQLClient(
    private val database: AppDatabase,
    private val context: android.content.Context? = null
) {

    // Directory Service modeled after a phonebook application
    val directoryService = HospitalDirectoryService()

    // --- SERVICES MARKETPLACE SEED CATALOG DATA ---
    val freelancerCatalog = mutableListOf(
        FreelancerProfile(
            id = "freelance-1",
            name = "Tunde Alabi",
            title = "Senior Flutter & Native Android Lead",
            avatarEmoji = "👨‍💻",
            rating = 4.8,
            basePrice = 12000.0,
            bio = "Ex-Chowdeck Engineering Lead. Specializes in real-time WebSockets, hyper-performant Compose architectures, and local Room caching subsystems.",
            category = "Software Dev",
            portfolioGallery = listOf(
                "Yanga Ride Tracking Mockups 🚗",
                "Hospital Registry Database Node 🏥",
                "High-Speed WebSocket Ledger Client 🛡️"
            ),
            serviceListings = listOf(
                "Secure In-App Wallet Gateway Setup (5 Days)",
                "Full-Screen Compose Module Architecture Refactor (2 Days)"
            ),
            reviews = listOf(
                FreelancerReview(reviewerName = "Eniola Agbeyindo", rating = 5, comment = "Brilliant architect. Restructured our superapp wallet API with perfect concurrency flows!"),
                FreelancerReview(reviewerName = "Olaide VI", rating = 4, comment = "Clean Kotlin and very reliable testing mocks.")
            )
        ),
        FreelancerProfile(
            id = "freelance-2",
            name = "Kemi Adebayo",
            title = "Exclusive Event Planner & Floral Visualist",
            avatarEmoji = "👩‍🎨",
            rating = 4.9,
            basePrice = 15000.0,
            bio = "Transforming standard spaces into spectacular African art experiences. Specializes in luxury lighting design, Ankara floral palettes, and catering orchestration.",
            category = "Event Prep",
            portfolioGallery = listOf(
                "Eko Hotel Grand Hall Velvet Gala Setup 🥂",
                "Tech & Suya Networking Night Lighting Scheme 🔌",
                "Balogun High-Fashion Exhibition Runway 👗"
            ),
            serviceListings = listOf(
                "Premium Neon Stage Setup with Floral Runways (2 Days)",
                "Complete Vendor Sourcing & Catering Oversight (5 Days)"
            ),
            reviews = listOf(
                FreelancerReview(reviewerName = "Tinuola E.", rating = 5, comment = "Organized our food festival flawlessly. Saved us ₦50,000 on vendor negotiations!")
            )
        ),
        FreelancerProfile(
            id = "freelance-3",
            name = "Chidi Okafor",
            title = "Signature Catering Specialist & Menu Designer",
            avatarEmoji = "🧑‍🍳",
            rating = 4.7,
            basePrice = 8500.0,
            bio = "Gourmet African Fusion Chef. Dedicated to traditional taste with a contemporary plated presentation. Maker of the Lagos Seafood Jollof Volcano.",
            category = "Catering",
            portfolioGallery = listOf(
                "Plated Spicy Octopus Jollof Bowl 🐙",
                "Gourmet Suya Beef Slider Towers 🍔",
                "Exotic Mango Sorbet & Pawpaw Ice Parfait 🥭"
            ),
            serviceListings = listOf(
                "Exclusive Dinner Event Curated 5-Course Plating (1 Day)",
                "Local Street-Food Live Grill Booth setup (1 Day)"
            ),
            reviews = listOf(
                FreelancerReview(reviewerName = "Wale Landmark", rating = 4, comment = "Seafood platter was spectacular, but delivery to Gbagada was nearly 20 mins slow. Great taste though!")
            )
        ),
        FreelancerProfile(
            id = "freelance-4",
            name = "Eniola Silva",
            title = "Creative Portrait Director & Brand Videographer",
            avatarEmoji = "🎥",
            rating = 4.6,
            basePrice = 9000.0,
            bio = "Capturing rich expressions and hyper-modern African aesthetics. Highly experienced with local content direction, cinematography, and social media flyers.",
            category = "Creative Arts",
            portfolioGallery = listOf(
                "Eko Atlantic City Aerial Cinematic Drone Reel 🛸",
                "Yanga Vibes Festival Midnight Teaser 🎆",
                "Alaba Tech Hub Visual Branding Kit 💻"
            ),
            serviceListings = listOf(
                "Brand Documentary Shoot and 4K Instagram Edit (2 Days)",
                "High-Converting Social Media Promotion Post Ad Flyer (1 Day)"
            ),
            reviews = listOf(
                FreelancerReview(reviewerName = "Bisi Alaba", rating = 5, comment = "Delivered the video in just 24 hours. Incredible speed!")
            )
        )
    )

    // Helper functions for Milestone serialisation
    fun serializeMilestones(list: List<ProjectMilestone>): String {
        return list.joinToString("|") { "${it.id}::${it.title}::${it.costAmount}::${it.status.name}" }
    }

    fun deserializeMilestones(str: String): List<ProjectMilestone> {
        if (str.isBlank()) return emptyList()
        return str.split("|").mapNotNull { part ->
            val segments = part.split("::")
            if (segments.size == 4) {
                ProjectMilestone(
                    id = segments[0],
                    title = segments[1],
                    costAmount = segments[2].toDoubleOrNull() ?: 0.0,
                    status = try { MilestoneStatus.valueOf(segments[3]) } catch(e: Exception) { MilestoneStatus.PENDING }
                )
            } else null
        }
    }

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

    val quotesCatalog = listOf(
        BusinessQuote("Q-01", "Balogun Merchant", "Style is the signature of your soul. Make it bold, make it loud.", "Fashion"),
        BusinessQuote("Q-02", "Alaba Tech Guru", "A powerbank in hand is worth two in the shop. Stay fully charged always.", "Electronics"),
        BusinessQuote("Q-03", "Mega Rite Grocer", "Quality is not an act, it is a habit of parboiled premium choice.", "Groceries"),
        BusinessQuote("Q-04", "Yanga CEO", "No matter how fast a cheetah runs, it cannot catch a business built on trust.", "Business"),
        BusinessQuote("Q-05", "Lagos Vibe Master", "Work hard, but make sure you have enough Jollof for the road.", "Motivation")
    )

    init {
        var loaded = false
        context?.let { ctx ->
            try {
                val jsonString = ctx.assets.open("hospitals_directory.json").bufferedReader().use { it.readText() }
                directoryService.loadHospitals(jsonString)
                loaded = directoryService.getAllHospitals().isNotEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!loaded) {
            directoryService.setHospitals(hospitalCatalog)
        }
    }

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
            normQuery.contains("mutation HireFreelancer") -> {
                val freelancerId = (request.variables["freelancerId"] as? String) ?: ""
                val selectedService = (request.variables["selectedService"] as? String) ?: ""
                val totalAmount = (request.variables["amount"] as? Number)?.toDouble() ?: 0.0
                @Suppress("UNCHECKED_CAST")
                val milestoneTitles = (request.variables["milestoneTitles"] as? List<String>) ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val milestoneAmounts = (request.variables["milestoneAmounts"] as? List<Number>)?.map { it.toDouble() } ?: emptyList()
                handleHireFreelancerMutation(freelancerId, selectedService, totalAmount, milestoneTitles, milestoneAmounts)
            }
            normQuery.contains("mutation ApproveMilestone") -> {
                val bookingId = (request.variables["bookingId"] as? String) ?: ""
                val milestoneId = (request.variables["milestoneId"] as? String) ?: ""
                handleApproveMilestoneMutation(bookingId, milestoneId)
            }
            normQuery.contains("mutation SubmitMilestoneForReview") -> {
                val bookingId = (request.variables["bookingId"] as? String) ?: ""
                val milestoneId = (request.variables["milestoneId"] as? String) ?: ""
                handleSubmitMilestoneForReviewMutation(bookingId, milestoneId)
            }
            normQuery.contains("mutation SubmitReview") -> {
                val freelancerId = (request.variables["freelancerId"] as? String) ?: ""
                val reviewerName = (request.variables["reviewerName"] as? String) ?: "Anonymous Client"
                val rating = (request.variables["rating"] as? Number)?.toInt() ?: 5
                val comment = (request.variables["comment"] as? String) ?: ""
                handleSubmitReviewMutation(freelancerId, reviewerName, rating, comment)
            }
            normQuery.contains("mutation SubmitOrder") || normQuery.contains("submitOrder") -> {
                val amount = (request.variables["amount"] as? Number)?.toDouble() ?: 0.0
                val itemsSummary = (request.variables["itemsSummary"] as? String) ?: ""
                handleSubmitOrderMutation(amount, itemsSummary)
            }

            // --- QUERY PROTOCOLS ---
            normQuery.contains("__schema") || normQuery.contains("__type") -> {
                successResponse(mapOf(
                    "__schema" to mapOf(
                        "queryType" to mapOf("name" to "Query"),
                        "mutationType" to mapOf("name" to "Mutation")
                    )
                ))
            }
            normQuery.contains("query GetFruitsOnly") || normQuery.contains("fruits") -> {
                val isExplicit = !normQuery.contains("GetFoodsAndFruits") && (normQuery.contains("name") || normQuery.contains("price") || normQuery.contains("category") || normQuery.contains("description"))
                
                val hasName = !isExplicit || normQuery.contains("name")
                val hasPrice = !isExplicit || normQuery.contains("price")
                val hasCategory = !isExplicit || normQuery.contains("category")
                val hasDesc = !isExplicit || (normQuery.contains("description") || normQuery.contains("desc"))
                
                val rawFruits = foodCatalog.filter { it.isFruit }
                val filteredFruits = rawFruits.map { original ->
                    FoodItem(
                        id = original.id,
                        name = if (hasName) original.name else "",
                        price = if (hasPrice) original.price else 0.0,
                        category = if (hasCategory) original.category else "",
                        description = if (hasDesc) original.description else "",
                        isFruit = true
                    )
                }
                successResponse(mapOf("fruits" to filteredFruits))
            }
            normQuery.contains("query GetRestaurantById") || normQuery.contains("restaurant(") -> {
                val id = (request.variables["id"] as? String) ?: ""
                val found = restaurantCatalog.find { it.id == id } ?: restaurantCatalog.firstOrNull()
                successResponse(mapOf("restaurant" to found))
            }
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
                successResponse(mapOf("hospitals" to directoryService.getAllHospitals()))
            }
            normQuery.contains("query GetRestaurants") -> {
                successResponse(mapOf("restaurants" to restaurantCatalog))
            }
            normQuery.contains("query GetQuotesLibrary") || normQuery.contains("quotesLibrary") -> {
                val category = request.variables["category"] as? String
                val filtered = if (category != null && category.isNotBlank() && category != "All") {
                    quotesCatalog.filter { it.category.equals(category, ignoreCase = true) }
                } else {
                    quotesCatalog
                }
                successResponse(mapOf("quotesLibrary" to QuotesLibraryType(quotes = filtered, totalCount = filtered.size, category = category)))
            }
            normQuery.contains("query GetQuotes") || normQuery.contains("quotes") -> {
                successResponse(mapOf("quotes" to quotesCatalog))
            }
            normQuery.contains("query GetFreelancers") -> {
                successResponse(mapOf("freelancers" to freelancerCatalog))
            }
            normQuery.contains("query GetEscrowBookings") -> {
                handleGetEscrowBookingsQuery()
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
        return try {
            database.withTransaction {
                val securityHash = generateLedgerSecurityHash("FUND", amount, System.currentTimeMillis())
                val txEntity = WalletTransactionEntity(
                    id = "TXN-FUND-${UUID.randomUUID().toString().take(6).uppercase()}",
                    customerId = "CUST-01",
                    type = "FUND",
                    amount = amount,
                    description = "Funded wallet via Secure Card Payment",
                    timestamp = System.currentTimeMillis(),
                    securityHash = securityHash
                )
                database.walletTransactionDao().insertTransaction(txEntity)
                successResponse(mapOf("balanceFunded" to amount, "success" to true))
            }
        } catch (e: Exception) {
            errorResponse("Failed to deposit funds: ${e.message}")
        }
    }

    private suspend fun handlePayMutation(amount: Double, note: String): GraphQLResponse<Any> {
        return try {
            database.withTransaction {
                val txs = database.walletTransactionDao().getAllTransactions().first()
                val currentBalance = calculateBalanceFromEntities(txs)
                if (currentBalance < amount) {
                    val errMsg = "Insufficient Wallet Balance! Current: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Charge attempt: ₦${String.format(java.util.Locale.US, "%,.2f", amount)}. Please fund your wallet."
                    android.util.Log.e("YangaMarketBilling", "TRANSACTION REJECTED: $errMsg")
                    throw IllegalStateException(errMsg)
                }

                val securityHash = generateLedgerSecurityHash("PAYMENT", amount, System.currentTimeMillis())
                val txEntity = WalletTransactionEntity(
                    id = "TXN-PAY-${UUID.randomUUID().toString().take(6).uppercase()}",
                    customerId = "CUST-01",
                    type = "PAYMENT",
                    amount = amount,
                    description = note,
                    timestamp = System.currentTimeMillis(),
                    securityHash = securityHash
                )
                database.walletTransactionDao().insertTransaction(txEntity)
                successResponse(mapOf("paymentCharged" to amount, "success" to true, "note" to note))
            }
        } catch (e: Exception) {
            if (e is IllegalStateException) {
                // Already logged above
            } else {
                android.util.Log.e("YangaMarketBilling", "Transaction system error: ${e.message}", e)
            }
            errorResponse(e.message ?: "Transaction failed due to an unknown issue.")
        }
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

    private suspend fun handleSubmitOrderMutation(amount: Double, itemsSummary: String): GraphQLResponse<Any> {
        val confirmationId = "YNG-ORD-${UUID.randomUUID().toString().take(6).uppercase()}"
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val timeStr = sdf.format(java.util.Date())

        val entity = SavedBookingEntity(
            id = confirmationId,
            bookingType = "ORDER",
            title = "Food & Fruits Superapp Order",
            subtitle = itemsSummary,
            dateOrTime = "Today, $timeStr",
            price = amount,
            extraDetails = "{\"status\": \"CONFIRMED\", \"eta\": \"25-35 mins\"}"
        )
        database.savedBookingDao().insertBooking(entity)

        return successResponse(mapOf(
            "submitOrder" to mapOf(
                "id" to confirmationId,
                "status" to "CONFIRMED",
                "estimatedDeliveryMinutes" to 30,
                "message" to "Congratulations! Your order with Yanga Market is placed under ID: $confirmationId. Fast-delivery courier is matching! 🛵⚡"
            )
        ))
    }

    // --- SERVICES MARKETPLACE RESOLVERS ---

    private suspend fun handleGetEscrowBookingsQuery(): GraphQLResponse<Any> {
        return try {
            val entities = database.savedBookingDao().getSavedBookings().first()
            val escrowBookings = entities.filter { it.bookingType == "FREELANCE_SERVICE" }.mapNotNull { ent ->
                val parts = ent.extraDetails.split("##")
                if (parts.size == 3) {
                    EscrowProjectBooking(
                        id = ent.id,
                        freelancerId = parts[0],
                        freelancerName = ent.title,
                        selectedService = ent.subtitle,
                        totalAmountPaidToEscrow = ent.price,
                        currentStatus = try { ServiceBookingStatus.valueOf(parts[2]) } catch(e: Exception) { ServiceBookingStatus.ESCROW_HELD },
                        milestones = deserializeMilestones(parts[1]),
                        timestamp = ent.dateOrTime.toLongOrNull() ?: System.currentTimeMillis()
                    )
                } else null
            }
            successResponse(mapOf("escrowBookings" to escrowBookings))
        } catch (e: Exception) {
            errorResponse("Failed to fetch ongoing escrow projects: ${e.message}")
        }
    }

    private suspend fun handleHireFreelancerMutation(
        freelancerId: String,
        selectedService: String,
        amount: Double,
        milestoneTitles: List<String>,
        milestoneAmounts: List<Double>
    ): GraphQLResponse<Any> {
        if (amount <= 0.0) {
            return errorResponse("Hiring fails: Secure Escrow Deposit amount must be positive.")
        }
        val freelancerObj = freelancerCatalog.find { it.id == freelancerId }
            ?: return errorResponse("Hiring fails: Freelancer ID '$freelancerId' not found.")

        return try {
            database.withTransaction {
                // Check if user has enough balance in wallet!
                val txs = database.walletTransactionDao().getAllTransactions().first()
                val currentBalance = calculateBalanceFromEntities(txs)
                if (currentBalance < amount) {
                    // Fail the GraphQL execution cleanly by throwing IllegalStateException so view state catches the message
                    throw IllegalStateException("Insufficient balance! Freelance project requires ₦${String.format(java.util.Locale.US, "%,.2f", amount)}, but your wallet only holds ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Please deposit funds first.")
                }

                // Deduct balance from Wallet and post a security audit ledger transaction
                val securityHash = generateLedgerSecurityHash("PAYMENT", amount, System.currentTimeMillis())
                val txEntity = WalletTransactionEntity(
                    id = "TXN-ESCR-${UUID.randomUUID().toString().take(6).uppercase()}",
                    customerId = "CUST-01",
                    type = "PAYMENT",
                    amount = amount,
                    description = "Escrow Deposit Lock: Hire ${freelancerObj.name} for '$selectedService'. Funds secured in Yanga Escrow Vault.",
                    timestamp = System.currentTimeMillis(),
                    securityHash = securityHash
                )
                database.walletTransactionDao().insertTransaction(txEntity)

                // Build project milestones
                val milestones = milestoneTitles.mapIndexed { idx, title ->
                    ProjectMilestone(
                        id = "MLS-${idx + 1}-${UUID.randomUUID().toString().take(4).uppercase()}",
                        title = title,
                        costAmount = milestoneAmounts.getOrNull(idx) ?: (amount / milestoneTitles.size),
                        status = MilestoneStatus.PENDING
                    )
                }

                val savedId = "ESCR-${UUID.randomUUID().toString().take(6).uppercase()}"
                
                // Serialize milestones string format: freelancerId + "##" + serializeMilestones(milestones) + "##" + status.name
                val extraDetString = "${freelancerId}##${serializeMilestones(milestones)}##${ServiceBookingStatus.ESCROW_HELD.name}"

                val savedEntity = SavedBookingEntity(
                    id = savedId,
                    bookingType = "FREELANCE_SERVICE",
                    title = freelancerObj.name,
                    subtitle = selectedService,
                    dateOrTime = System.currentTimeMillis().toString(),
                    price = amount,
                    extraDetails = extraDetString
                )
                database.savedBookingDao().insertBooking(savedEntity)

                val booking = EscrowProjectBooking(
                    id = savedId,
                    freelancerId = freelancerId,
                    freelancerName = freelancerObj.name,
                    selectedService = selectedService,
                    totalAmountPaidToEscrow = amount,
                    currentStatus = ServiceBookingStatus.ESCROW_HELD,
                    milestones = milestones,
                    timestamp = System.currentTimeMillis()
                )

                successResponse(mapOf("booking" to booking, "success" to true))
            }
        } catch (e: Exception) {
            errorResponse(e.message ?: "Hiring escrow transaction failed.")
        }
    }

    private suspend fun handleApproveMilestoneMutation(bookingId: String, milestoneId: String): GraphQLResponse<Any> {
        return try {
            database.withTransaction {
                val optBooking = database.savedBookingDao().getSavedBookings().first().find { it.id == bookingId && it.bookingType == "FREELANCE_SERVICE" }
                    ?: throw IllegalArgumentException("Escrow Project Booking '$bookingId' not found.")

                val parts = optBooking.extraDetails.split("##")
                if (parts.size != 3) {
                    throw IllegalStateException("Escrow Booking schema corrupted.")
                }

                val freelancerId = parts[0]
                val milestones = deserializeMilestones(parts[1]).toMutableList()
                val currentStatusStr = parts[2]

                val milestoneToApprove = milestones.find { it.id == milestoneId }
                    ?: throw IllegalArgumentException("Milestone ID '$milestoneId' does not exist in this project.")

                if (milestoneToApprove.status == MilestoneStatus.APPROVED_AND_PAID) {
                    throw IllegalStateException("Milestone already approved and disbursed.")
                }

                // Mark Approved
                milestoneToApprove.approve()

                // Check overall Status
                val allApproved = milestones.all { it.status == MilestoneStatus.APPROVED_AND_PAID }
                val nextStatus = if (allApproved) ServiceBookingStatus.FINISHED_RELEASED else ServiceBookingStatus.MILESTONE_APPROVED

                // Save back
                val updatedExtra = "${freelancerId}##${serializeMilestones(milestones)}##${nextStatus.name}"
                val updatedEntity = optBooking.copy(extraDetails = updatedExtra)
                database.savedBookingDao().insertBooking(updatedEntity)

                // Audit release transaction (zero cost representation on wallet client side ledger since escrow deposit already happened)
                val logTxnSecurityHash = generateLedgerSecurityHash("PAYMENT", 0.0, System.currentTimeMillis())
                val disbursementTxn = WalletTransactionEntity(
                    id = "TXN-RELS-${UUID.randomUUID().toString().take(6).uppercase()}",
                    customerId = "CUST-01",
                    type = "PAYMENT",
                    amount = 0.0,
                    description = "Escrow Disbursement Release: Released milestone payment (₦${String.format(java.util.Locale.US, "%,.2f", milestoneToApprove.costAmount)}) to freelancer for '${milestoneToApprove.title}'",
                    timestamp = System.currentTimeMillis(),
                    securityHash = logTxnSecurityHash
                )
                database.walletTransactionDao().insertTransaction(disbursementTxn)

                successResponse(mapOf("success" to true, "milestoneId" to milestoneId, "status" to nextStatus.name))
            }
        } catch (e: Exception) {
            errorResponse(e.message ?: "Milestone approval transaction failed.")
        }
    }

    private suspend fun handleSubmitMilestoneForReviewMutation(bookingId: String, milestoneId: String): GraphQLResponse<Any> {
        return try {
            database.withTransaction {
                val optBooking = database.savedBookingDao().getSavedBookings().first().find { it.id == bookingId && it.bookingType == "FREELANCE_SERVICE" }
                    ?: throw IllegalArgumentException("Escrow Project Booking '$bookingId' not found.")

                val parts = optBooking.extraDetails.split("##")
                if (parts.size != 3) {
                    throw IllegalStateException("Escrow Booking schema corrupted.")
                }

                val freelancerId = parts[0]
                val milestones = deserializeMilestones(parts[1]).toMutableList()
                val currentStatusStr = parts[2]

                val milestoneToSubmit = milestones.find { it.id == milestoneId }
                    ?: throw IllegalArgumentException("Milestone ID '$milestoneId' does not exist in this project.")

                if (milestoneToSubmit.status == MilestoneStatus.APPROVED_AND_PAID) {
                    throw IllegalStateException("Milestone already approved and disbursed.")
                }

                // Mark Submitted for Review
                milestoneToSubmit.submit()

                // Save back
                val updatedExtra = "${freelancerId}##${serializeMilestones(milestones)}##$currentStatusStr"
                val updatedEntity = optBooking.copy(extraDetails = updatedExtra)
                database.savedBookingDao().insertBooking(updatedEntity)

                successResponse(mapOf("success" to true, "milestoneId" to milestoneId))
            }
        } catch (e: Exception) {
            errorResponse(e.message ?: "Milestone submission failed.")
        }
    }

    private suspend fun handleSubmitReviewMutation(
        freelancerId: String,
        reviewerName: String,
        rating: Int,
        comment: String
    ): GraphQLResponse<Any> {
        val freelancerObjIdx = freelancerCatalog.indexOfFirst { it.id == freelancerId }
        if (freelancerObjIdx == -1) {
            return errorResponse("Review fails: Freelancer ID '$freelancerId' not found.")
        }

        val profile = freelancerCatalog[freelancerObjIdx]
        val newReview = FreelancerReview(
            reviewerName = reviewerName,
            rating = rating,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )
        val updatedReviews = profile.reviews + newReview
        val updatedProfile = profile.copy(
            reviews = updatedReviews,
            rating = (updatedReviews.map { it.rating }.average() * 10).toInt() / 10.0
        )
        
        // Update in-memory catalogue
        freelancerCatalog[freelancerObjIdx] = updatedProfile

        return successResponse(mapOf("success" to true, "updatedRating" to updatedProfile.rating))
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
