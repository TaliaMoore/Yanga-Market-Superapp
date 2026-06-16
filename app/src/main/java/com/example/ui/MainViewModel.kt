package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.graphql.*
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val graphQLClient = MockGraphQLClient(database)

    // --- GraphQLEmulated Static Catalogs ---
    private val _foods = MutableStateFlow<List<FoodItem>>(emptyList())
    val foods: StateFlow<List<FoodItem>> = _foods.asStateFlow()

    private val _fruits = MutableStateFlow<List<FoodItem>>(emptyList())
    val fruits: StateFlow<List<FoodItem>> = _fruits.asStateFlow()

    private val _retailShops = MutableStateFlow<List<RetailShop>>(emptyList())
    val retailShops: StateFlow<List<RetailShop>> = _retailShops.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
    val hospitals: StateFlow<List<Hospital>> = _hospitals.asStateFlow()

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    // --- Reactive Local Flows from Room Database ---
    val cartItems: StateFlow<List<CartItemEntity>> = database.cartItemDao().getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vibePosts: StateFlow<List<VibePostEntity>> = database.vibePostDao().getAllVibePosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = database.walletTransactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedBookings: StateFlow<List<SavedBookingEntity>> = database.savedBookingDao().getSavedBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Computed Reactive Wallet Balance ---
    val walletBalance: StateFlow<Double> = walletTransactions.map { txList ->
        var currentBalance = 10000.0 // Starter ₦10k bonus for all new playful accounts
        for (tx in txList) {
            when (tx.type) {
                "FUND" -> currentBalance += tx.amount
                "PAYMENT" -> currentBalance -= tx.amount
            }
        }
        currentBalance
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    // --- Unified Network Status, Banners, and Inputs ---
    private val _isGraphQLFetching = MutableStateFlow(false)
    val isGraphQLFetching: StateFlow<Boolean> = _isGraphQLFetching.asStateFlow()

    private val _vibeAuthorInput = MutableStateFlow("")
    val vibeAuthorInput: StateFlow<String> = _vibeAuthorInput.asStateFlow()

    private val _vibeContentInput = MutableStateFlow("")
    val vibeContentInput: StateFlow<String> = _vibeContentInput.asStateFlow()

    private val _errorBannerMessage = MutableStateFlow<String?>(null)
    val errorBannerMessage: StateFlow<String?> = _errorBannerMessage.asStateFlow()

    private val _successBannerMessage = MutableStateFlow<String?>(null)
    val successBannerMessage: StateFlow<String?> = _successBannerMessage.asStateFlow()

    init {
        // Fetch static catalogs using GraphQL Query abstractions
        refreshGraphQLCatalogs()
        // Pre-seed default vibes community posts for immersive layout
        preseedVibesIfEmpty()
    }

    /**
     * Resets visual confirmation / alert banners.
     */
    fun clearBanners() {
        _errorBannerMessage.value = null
        _successBannerMessage.value = null
    }

    /**
     * Executes asynchronous schema queries via YangaGraphQLService
     */
    fun refreshGraphQLCatalogs() {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                // Query foods & fruits
                val foodRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetFoodsAndFruits { foods { name price category } }"
                ))
                val dataMap = foodRes.data as? Map<*, *>
                val foodList = (dataMap?.get("foods") as? List<*>) ?: emptyList<Any>()
                val parsedFoods = foodList.filterIsInstance<FoodItem>()
                _foods.value = parsedFoods.filter { !it.isFruit }
                _fruits.value = parsedFoods.filter { it.isFruit }

                // Query retail shops
                val retailRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetRetailShops { shops { name distanceKm items { name price } } }"
                ))
                val retailData = retailRes.data as? Map<*, *>
                _retailShops.value = retailData?.get("shops") as? List<RetailShop> ?: emptyList()

                // Query events
                val eventsRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetUpcomingEvents { events { title host price date venue } }"
                ))
                val eventsData = eventsRes.data as? Map<*, *>
                _events.value = eventsData?.get("events") as? List<Event> ?: emptyList()

                // Query hospitals
                val hospitalsRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetHospitals { hospitals { name specialties distanceKm } }"
                ))
                val hospitalsData = hospitalsRes.data as? Map<*, *>
                _hospitals.value = hospitalsData?.get("hospitals") as? List<Hospital> ?: emptyList()

                // Query restaurants
                val restaurantsRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetRestaurants { restaurants { name cuisine rating tablePrice } }"
                ))
                val restaurantsData = restaurantsRes.data as? Map<*, *>
                _restaurants.value = restaurantsData?.get("restaurants") as? List<Restaurant> ?: emptyList()

            } catch (e: Exception) {
                _errorBannerMessage.value = "GraphQL Fetch Failure: ${e.localizedMessage}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    private fun preseedVibesIfEmpty() {
        viewModelScope.launch {
            val count = vibePosts.value.size
            if (count == 0) {
                val preseeds = listOf(
                    VibePostEntity(
                        id = "seed-1",
                        author = "Chinedu_Yanga",
                        content = "Just ordered the spicy Jollof Rice & Suya Burger from Yanga Market. Absolute chef's kiss! Fast and playful delivery too. 🌶️🍔✨",
                        vibeCount = 18,
                        isVibeChecked = true,
                        timestamp = System.currentTimeMillis() - 7200000,
                        commentsJson = "[]"
                    ),
                    VibePostEntity(
                        id = "seed-2",
                        author = "Amara_Dev",
                        content = "RSVP'ed for the Tech & Suya Networking Night! Who else is pulling up this weekend? Let's secure our wallets with some cold drinks! 💜🚀",
                        vibeCount = 24,
                        isVibeChecked = false,
                        timestamp = System.currentTimeMillis() - 14400000,
                        commentsJson = "[]"
                    ),
                    VibePostEntity(
                        id = "seed-3",
                        author = "Tunde_Kola",
                        content = "The secure digital wallet is an absolute lifesaver. Instantly funded my balance via card, and payments for events are completely seamless. 💳🔐",
                        vibeCount = 9,
                        isVibeChecked = false,
                        timestamp = System.currentTimeMillis() - 28800000,
                        commentsJson = "[]"
                    )
                )
                for (v in preseeds) {
                    database.vibePostDao().insertVibePost(v)
                }
            }
        }
    }

    // --- WALLET CONTROLS ---

    fun fundWallet(amount: Double) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val variables = mapOf("amount" to amount)
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation FundWallet(\$amount: Float!) { fundWallet(amount: \$amount) { success } }",
                variables = variables
            ))
            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            } else {
                _successBannerMessage.value = "Funded wallet with ₦${String.format("%,.2f", amount)} successfully! 💜💳"
            }
            _isGraphQLFetching.value = false
        }
    }

    // --- CART MECHANICS ---

    fun addToCart(name: String, price: Double, category: String, itemType: String) {
        viewModelScope.launch {
            val exist = cartItems.value.find { it.name == name }
            if (exist != null) {
                database.cartItemDao().insertCartItem(exist.copy(quantity = exist.quantity + 1))
            } else {
                val entity = CartItemEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    price = price,
                    category = category,
                    quantity = 1,
                    itemType = itemType
                )
                database.cartItemDao().insertCartItem(entity)
            }
            _successBannerMessage.value = "Added $name to basket! 🛒🍿"
        }
    }

    fun modifyCartQuantity(item: CartItemEntity, delta: Int) {
        viewModelScope.launch {
            val newQuantity = item.quantity + delta
            if (newQuantity <= 0) {
                database.cartItemDao().deleteCartItem(item.id)
            } else {
                database.cartItemDao().insertCartItem(item.copy(quantity = newQuantity))
            }
        }
    }

    fun checkoutCart() {
        val total = cartItems.value.sumOf { it.price * it.quantity }
        val itemsSummary = cartItems.value.joinToString(", ") { "${it.quantity}x ${it.name}" }

        if (total <= 0) {
            _errorBannerMessage.value = "Your checkout basket is empty!"
            return
        }

        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val variables = mapOf(
                "amount" to total,
                "note" to "Order Payment: $itemsSummary"
            )
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                variables = variables
            ))

            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            } else {
                // Clear cart
                database.cartItemDao().clearCart()
                _successBannerMessage.value = "Awesome! Order paid with wallet: ₦${String.format("%,.2f", total)}! Preparations started! 🍕🛵"
            }
            _isGraphQLFetching.value = false
        }
    }

    // --- COMMUNITY VIBES BOARD (LET'S SHARE VIBES) ---

    fun updateVibeInputs(author: String?, content: String?) {
        if (author != null) _vibeAuthorInput.value = author
        if (content != null) _vibeContentInput.value = content
    }

    fun submitVibe() {
        val author = _vibeAuthorInput.value.trim()
        val content = _vibeContentInput.value.trim()

        if (author.isEmpty() || content.isEmpty()) {
            _errorBannerMessage.value = "Please enter both your name and a vibe message!"
            return
        }

        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val variables = mapOf("author" to author, "content" to content)
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PostVibe(\$author: String!, \$content: String!) { addVibe(author: \$author, content: \$content) { id } }",
                variables = variables
            ))

            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            } else {
                _vibeContentInput.value = ""
                _successBannerMessage.value = "Your vibe has been shared to the board! 💜🎉"
            }
            _isGraphQLFetching.value = false
        }
    }

    fun voteVibePost(vibeId: String) {
        viewModelScope.launch {
            val variables = mapOf("id" to vibeId)
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation ReactionVibe(\$id: String!) { react(id: \$id) { isVibeChecked } }",
                variables = variables
            ))
            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            }
        }
    }

    // --- HOSPITAL & RESTAURANT BOOKINGS, EVENT TICKETS ---

    fun bookHospitalService(hospital: Hospital, service: String, bookingDate: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val variables = mapOf(
                "title" to hospital.name,
                "subtitle" to "Specialty Appointment: $service",
                "price" to 0.0,
                "dateOrTime" to bookingDate,
                "type" to "HOSPITAL"
            )
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation BookService(\$title: String!, \$subtitle: String!, \$price: Float!, \$dateOrTime: String!, \$type: String!) { book(title: \$title, subtitle: \$subtitle, price: \$price, dateOrTime: \$dateOrTime, type: \$type) { success } }",
                variables = variables
            ))

            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            } else {
                _successBannerMessage.value = "Appointment Booked with ${hospital.name}! Confirmation details are stored locally. 🩺🏨"
            }
            _isGraphQLFetching.value = false
        }
    }

    fun purchaseEventTicket(event: Event) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            // If ticket is not free, charge wallet
            var paymentSuccess = true
            if (event.price > 0) {
                val variablesPay = mapOf(
                    "amount" to event.price,
                    "note" to "Event Entry Ticket: ${event.title}"
                )
                val payRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                    variables = variablesPay
                ))
                if (payRes.errors != null) {
                    _errorBannerMessage.value = payRes.errors.first().message
                    paymentSuccess = false
                }
            }

            if (paymentSuccess) {
                val variablesBook = mapOf(
                    "title" to event.title,
                    "subtitle" to "Hosted by ${event.host} @ ${event.venue}",
                    "price" to event.price,
                    "dateOrTime" to "${event.date} at ${event.time}",
                    "type" to "EVENT"
                )
                val bookRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation BookService(\$title: String!, \$subtitle: String!, \$price: Float!, \$dateOrTime: String!, \$type: String!) { book(title: \$title, subtitle: \$subtitle, price: \$price, dateOrTime: \$dateOrTime, type: \$type) { success } }",
                    variables = variablesBook
                ))

                if (bookRes.errors != null) {
                    _errorBannerMessage.value = bookRes.errors.first().message
                } else {
                    _successBannerMessage.value = "Success! Secured ticket for '${event.title}'! 🎫🎟️"
                }
            }
            _isGraphQLFetching.value = false
        }
    }

    fun reserveRestaurantTable(restaurant: Restaurant, bookingTime: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val price = restaurant.tablePrice
            val variablesPay = mapOf(
                "amount" to price,
                "note" to "Table Reservation: ${restaurant.name}"
            )
            val payRes = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                variables = variablesPay
            ))

            if (payRes.errors != null) {
                _errorBannerMessage.value = payRes.errors.first().message
            } else {
                val variablesBook = mapOf(
                    "title" to restaurant.name,
                    "subtitle" to "Cuisine: ${restaurant.cuisine} | table reserved",
                    "price" to price,
                    "dateOrTime" to bookingTime,
                    "type" to "RESTAURANT"
                )
                val bookRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation BookService(\$title: String!, \$subtitle: String!, \$price: Float!, \$dateOrTime: String!, \$type: String!) { book(title: \$title, subtitle: \$subtitle, price: \$price, dateOrTime: \$dateOrTime, type: \$type) { success } }",
                    variables = variablesBook
                ))

                if (bookRes.errors != null) {
                    _errorBannerMessage.value = bookRes.errors.first().message
                } else {
                    _successBannerMessage.value = "Table successfully booked at ${restaurant.name}! Coziness awaits. 🍕🍷"
                }
            }
            _isGraphQLFetching.value = false
        }
    }

    fun cancelActiveBooking(bookingId: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val variables = mapOf("id" to bookingId)
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation DeleteBooking(\$id: String!) { delete(id: \$id) { success } }",
                variables = variables
            ))
            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            } else {
                _successBannerMessage.value = "Booking successfully cancelled."
            }
            _isGraphQLFetching.value = false
        }
    }
}
