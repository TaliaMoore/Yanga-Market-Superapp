package com.example.domain.model

import java.util.UUID

// --- 1. FOOD & FRUITS ---
class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    price: Double, // Constructor parameter name matching client construction sites
    val category: String, // e.g., Burgers, Rice, Desserts, Exotic Fruits
    val description: String,
    val rating: Double = 4.5,
    val deliveryTimeMin: Int = 25,
    val isFruit: Boolean = false
) {
    // Private encapsulated backing field for clean information hiding
    private var foodPrice: Double = price

    // Explicit Java-style accessor (getter) for information hiding
    fun fetchPrice(): Double = foodPrice
    
    // Controlled mutator for FoodItem price to ensure proper information hiding
    fun updatePrice(newPrice: Double) {
        if (newPrice >= 0.0) {
            this.foodPrice = newPrice
        }
    }
    
    // Read-only compatibility property for rendering integration layers
    val price: Double get() = fetchPrice()
}

// --- COMPOSITION OF ADDRESSES ---
/**
 * Core component for composition, forming a 'has-a' relationship
 * where other entities can include name & address.
 */
class NameAndAddress(
    private var nameVal: String,
    private var addressVal: String
) {
    fun getName(): String = nameVal
    fun getAddress(): String = addressVal

    fun setName(name: String) {
        if (name.isNotBlank()) {
            this.nameVal = name
        }
    }

    fun setAddress(address: String) {
        if (address.isNotBlank()) {
            this.addressVal = address
        }
    }
}

// --- 0. BASE CLASS (JAVA-STYLE OBJECT-ORIENTED DESIGN) ---
/**
 * Abstract parent class representing a core entity in our superapp.
 * Fulfills clean Java-style Object-Oriented principles by encapsulating key identity details in private fields.
 */
abstract class SuperAppEntity(
    private val idVal: String,
    private val nameVal: String,
    private val locationVal: String
) {
    // Encapsulated getters
    fun fetchId(): String = idVal
    fun fetchName(): String = nameVal
    fun fetchLocation(): String = locationVal
}

// --- 2. RETAIL SHOPS ---
class RetailShop(
    val id: String = UUID.randomUUID().toString(),
    val nameAndAddress: NameAndAddress, // This establishes the composition 'has-a' relationship!
    val specialty: String, // e.g., Fashion, Electronics, Supermarket
    val distanceKm: Double,
    val items: List<RetailItem>
) : SuperAppEntity(id, nameAndAddress.getName(), nameAndAddress.getAddress()) {

    // Secondary constructor supporting backward-compatible direct initialization
    constructor(
        id: String = UUID.randomUUID().toString(),
        name: String,
        specialty: String,
        distanceKm: Double,
        items: List<RetailItem>,
        location: String = "Lagos Shopping Complex"
    ) : this(
        id = id,
        nameAndAddress = NameAndAddress(name, location),
        specialty = specialty,
        distanceKm = distanceKm,
        items = items
    )

    // Delegations maintaining perfect UI/database binding compatibility
    val name: String get() = nameAndAddress.getName()
    val location: String get() = nameAndAddress.getAddress()
}

// Convenient alias matching user instruction
typealias Shop = RetailShop

// Controlled, private-backed shop item implementation matching prompt instructions
class RetailItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    price: Double, // Constructor parameter name matching catalog initializers
    val category: String,
    val inStock: Boolean = true
) {
    // Private encapsulated backing field for clean information hiding
    private var itemPrice: Double = price

    // Explicit Java-style accessor (getter) for information hiding
    fun fetchPrice(): Double = itemPrice
    
    // Controlled mutator with safety/information hiding checks
    fun updatePrice(newPrice: Double) {
        if (newPrice >= 0.0) {
            this.itemPrice = newPrice
        }
    }
    
    // Read-only public getter property for compose-bindings safety
    val price: Double get() = fetchPrice()
}

// Convenient alias matching the user's specific request
typealias ShopItem = RetailItem

// --- 3. RESTAURANTS ---
class Restaurant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val cuisine: String, // e.g., Nigerian, Pan-Asian, Italian
    val rating: Double,
    val address: String, // serves as location
    val hasTableBooking: Boolean = true,
    tablePrice: Double = 1500.0, // Constructor parameter name matching restaurant initializers
    val isTableReserved: Boolean = false
) : SuperAppEntity(id, name, address) {

    // Default constructor with placeholder values
    constructor() : this(
        id = UUID.randomUUID().toString(),
        name = "Placeholder Restaurant",
        cuisine = "General Cuisine",
        rating = 4.0,
        address = "123 Placeholder Street",
        hasTableBooking = false,
        tablePrice = 0.0,
        isTableReserved = false
    )

    // Overloaded constructor requiring all mandatory data points for instantiation
    constructor(
        name: String,
        cuisine: String,
        rating: Double,
        address: String
    ) : this(
        id = UUID.randomUUID().toString(),
        name = name,
        cuisine = cuisine,
        rating = rating,
        address = address,
        hasTableBooking = true,
        tablePrice = 1500.0,
        isTableReserved = false
    )

    // Private encapsulated backing field for proper information hiding
    private var bookingPrice: Double = tablePrice

    // Explicit Java-style accessor (getter)
    fun fetchTablePrice(): Double = bookingPrice
    
    // Controlled mutator with validation
    fun updateTablePrice(newPrice: Double) {
        if (newPrice >= 0.0) {
            this.bookingPrice = newPrice
        }
    }
    
    // Compatibility property
    val tablePrice: Double get() = fetchTablePrice()
}

// --- 4. EVENTS (MODELED AFTER CARLY'S CATERING CASE STUDY) ---
class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String, // serves as name
    val host: String,
    val date: String,
    val time: String,
    val venue: String, // serves as location
    val rsvpCount: Int = 0,
    val isRsvped: Boolean = false,
    price: Double = -1.0, // normal parameter to allow legacy initialization
    initialGuests: Int = 0,
    initialEventNumber: String? = null
) : SuperAppEntity(id, title, venue) {

    companion object {
        const val PRICE_PER_GUEST = 35.0 // standard rate in Carly's Catering case study guidelines ($35/₦35 per guest)
        const val CUTOFF_LARGE_EVENT = 50 // cutoff for large vs small events
    }

    // Private encapsulated backing fields for clean Object-Oriented design
    private var eventNumber: String = initialEventNumber ?: ("E" + id.take(4).uppercase())
    private var numberOfGuests: Int = if (initialGuests > 0) initialGuests else (if (rsvpCount > 0) rsvpCount else 15)
    private var calculatedPrice: Double = if (price >= 0.0) {
        price
    } else {
        // Automatically computed based on guest count & PRICE_PER_GUEST
        numberOfGuests * PRICE_PER_GUEST
    }

    // Standard properties required across app layers
    val name: String get() = title
    val location: String get() = venue

    // Encapsulated getters & setters for the Carly's Catering model
    fun getEventNumber(): String = eventNumber
    
    fun setEventNumber(num: String) {
        // Normalizing event number formatting
        if (num.length >= 4) {
            this.eventNumber = num.uppercase()
        } else {
            this.eventNumber = num
        }
    }

    fun getNumberOfGuests(): Int = numberOfGuests

    /**
     * Set guests and automatically recalulate overall event price.
     * Core price calculation logic based on guest count as required by Carly's Catering!
     */
    fun setGuests(guests: Int) {
        if (guests >= 0) {
            this.numberOfGuests = guests
            this.calculatedPrice = guests * PRICE_PER_GUEST
        }
    }

    fun getCalculatedPrice(): Double = calculatedPrice

    /**
     * Set/Update calculated price through a controlled mutator method.
     */
    fun setCalculatedPrice(newPrice: Double) {
        if (newPrice >= 0.0) {
            this.calculatedPrice = newPrice
        }
    }

    /**
     * Custom business method determining event capacity scope.
     */
    fun isLargeEvent(): Boolean = numberOfGuests >= CUTOFF_LARGE_EVENT

    // Legacy adapter property matching 'event.price' field to keep screen bindings perfectly intact
    val price: Double get() = getCalculatedPrice()
}

// --- 5. HOSPITALS ---
class Hospital(
    val id: String = UUID.randomUUID().toString(),
    val nameAndAddress: NameAndAddress, // This establishes the composition 'has-a' relationship!
    val distanceKm: Double,
    val specialties: List<String>,
    openHours: String = "24/7" // Constructor parameter name
) : SuperAppEntity(id, nameAndAddress.getName(), nameAndAddress.getAddress()) {

    // Secondary constructor supporting backward-compatible direct initialization
    constructor(
        id: String = UUID.randomUUID().toString(),
        name: String,
        location: String,
        distanceKm: Double,
        specialties: List<String>,
        openHours: String = "24/7"
    ) : this(
        id = id,
        nameAndAddress = NameAndAddress(name, location),
        distanceKm = distanceKm,
        specialties = specialties,
        openHours = openHours
    )

    // Default constructor with placeholder values
    constructor() : this(
        id = UUID.randomUUID().toString(),
        name = "Placeholder Hospital",
        location = "Default Hospital Address",
        distanceKm = 0.0,
        specialties = listOf("General Wellness"),
        openHours = "24/7"
    )

    // Overloaded constructor requiring all mandatory data points for instantiation
    constructor(
        name: String,
        location: String,
        distanceKm: Double,
        specialties: List<String>
    ) : this(
        id = UUID.randomUUID().toString(),
        name = name,
        location = location,
        distanceKm = distanceKm,
        specialties = specialties,
        openHours = "24/7"
    )

    // Delegations maintaining perfect UI/database binding compatibility
    val name: String get() = nameAndAddress.getName()
    val location: String get() = nameAndAddress.getAddress()

    // Private encapsulated backing field for clean information hiding
    private var openHoursVal: String = openHours
    
    // Explicit Java-style accessor (getter)
    fun fetchOpenHours(): String = openHoursVal
    
    // Controlled mutator
    fun updateOpenHours(newHours: String) {
        if (newHours.isNotBlank()) {
            this.openHoursVal = newHours
        }
    }
    
    // Compatibility property
    val openHours: String get() = fetchOpenHours()
}

data class HospitalBooking(
    val bookingId: String = UUID.randomUUID().toString().take(8),
    val hospitalId: String,
    val hospitalName: String,
    val selectedService: String,
    val bookDate: String,
    val bookTime: String,
    val currentStatus: String = "CONFIRMED"
)

// --- 6. LET'S SHARE VIBES (COMMUNITY) ---
data class VibePost(
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val content: String,
    val vibeCount: Int = 0, // Likes / vibe checks
    val isVibeChecked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val comments: List<VibeComment> = emptyList()
)

data class VibeComment(
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- 7. SECURE WALLET ---
data class WalletState(
    val balance: Double = 5000.0, // Starting bonus!
    val walletPin: String = "1234",
    val transactions: List<WalletTransaction> = emptyList()
)

data class WalletTransaction(
    val id: String = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val securityHash: String // Generated security verification for ledger audits
)

enum class TransactionType {
    FUND,     // Money into wallet
    PAYMENT,  // Money paid out
    REFUND    // Money refunded
}

// --- 8. CUSTOMER CLASS ---
/**
 * Customer class that models state information such as a shopping cart
 * (using List or Array implementation) and holds a active reference to a wallet.
 */
class Customer(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    // shoppingCart as a List
    var shoppingCart: List<RetailItem> = emptyList(),
    // reference to Wallet (represented by WalletState)
    private var wallet: WalletState = WalletState()
) {
    // Secondary constructor accepting shopping cart as an Array
    constructor(
        id: String = UUID.randomUUID().toString(),
        name: String,
        shoppingCartArray: Array<RetailItem>,
        wallet: WalletState = WalletState()
    ) : this(
        id = id,
        name = name,
        shoppingCart = shoppingCartArray.toList(),
        wallet = wallet
    )

    // Getter/Accessor for wallet
    fun getWallet(): WalletState = wallet

    // Mutator to update wallet
    fun updateWallet(newWallet: WalletState) {
        this.wallet = newWallet
    }

    // Methods to manage shopping cart
    fun addToCart(item: RetailItem) {
        this.shoppingCart = this.shoppingCart + item
    }

    fun removeFromCart(item: RetailItem) {
        this.shoppingCart = this.shoppingCart.filter { it.id != item.id }
    }

    fun clearCart() {
        this.shoppingCart = emptyList()
    }

    // Python-style simulation: Customer places order to an Employee and receives an item in return
    fun placeOrder(employee: Employee, requestName: String, price: Double, isFood: Boolean): Any {
        return if (isFood) {
            employee.processFoodOrder(requestName, price)
        } else {
            val item = employee.processProductOrder(requestName, price)
            addToCart(item) // Dynamically track state update
            item
        }
    }

    // Python-style simulation: Customer places order to a automated System and receives an item in return
    fun placeOrder(system: OrderSystem, requestName: String, price: Double, isFood: Boolean): Any {
        return if (isFood) {
            system.processFoodOrder(requestName, price)
        } else {
            val item = system.processProductOrder(requestName, price)
            addToCart(item) // Dynamically track state update
            item
        }
    }
}

// --- 9. EMPLOYEE / SYSTEM CLASSES FOR PY-STYLE RELATIONSHIP ---
class Employee(val name: String) {
    fun processFoodOrder(request: String, price: Double): FoodItem {
        return FoodItem(
            name = request,
            price = price,
            category = "Employee Processed",
            description = "Smoky local food $request freshly prepared by representative $name"
        )
    }

    fun processProductOrder(request: String, price: Double): RetailItem {
        return RetailItem(
            name = request,
            price = price,
            category = "Employee Processed"
        )
    }
}

class OrderSystem {
    fun processFoodOrder(request: String, price: Double): FoodItem {
        return FoodItem(
            name = request,
            price = price,
            category = "System Auto-Dispatched",
            description = "Auto-dispatched high-speed $request delivery from central hub"
        )
    }

    fun processProductOrder(request: String, price: Double): RetailItem {
        return RetailItem(
            name = request,
            price = price,
            category = "System Auto-Dispatched"
        )
    }
}

// --- 10. LUNCH/ORDER CONTAINER CLASS FOR TRANSACTION LIFECYCLE ---
/**
 * Container class that embeds both a Customer and an Employee to manage the lifecycle
 * of a single food delivery transaction.
 */
class LunchOrder(
    val customer: Customer,
    val employee: Employee,
    val foodName: String,
    val price: Double
) {
    private var status: OrderStatus = OrderStatus.PENDING
    private var deliveredFoodItem: FoodItem? = null

    fun getStatus(): OrderStatus = status
    fun getDeliveredFoodItem(): FoodItem? = deliveredFoodItem

    /**
     * Advances the lifecycle state of the food delivery transaction.
     */
    fun advanceLifecycle() {
        status = when (status) {
            OrderStatus.PENDING -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.IN_TRANSIT
            OrderStatus.IN_TRANSIT -> {
                // When delivering, employee generates the actual food item
                deliveredFoodItem = employee.processFoodOrder(foodName, price)
                OrderStatus.DELIVERED
            }
            else -> OrderStatus.DELIVERED
        }
    }

    /**
     * Allows custom status transitions if needed during external events.
     */
    fun updateStatus(newStatus: OrderStatus) {
        this.status = newStatus
    }
}

// --- 11. ENUMERATED ORDER STATUS ---
/**
 * System of enumerated types to track order statuses consistently.
 */
enum class OrderStatus {
    PENDING,
    PREPARING,
    IN_TRANSIT,
    DELIVERED,
    REFUNDED
}

// --- 12. PRODUCT HIERARCHY ---
/**
 * Product superclass for modeling a generic store item containing id, name, and price.
 */
open class Product(
    open val id: String = UUID.randomUUID().toString(),
    open val name: String,
    open val price: Double
)

/**
 * Fruit subclass of Product.
 */
class Fruit(
    id: String = UUID.randomUUID().toString(),
    name: String,
    price: Double,
    val sweetnessLevel: Int = 3, // scale of 1-5 or similar
    val origin: String = "Local Orchard"
) : Product(id, name, price)

/**
 * Meal subclass of Product containing 'preparationTime' in minutes as an extra attribute.
 */
class Meal(
    id: String = UUID.randomUUID().toString(),
    name: String,
    price: Double,
    val preparationTime: Int, // Extra attribute representation in minutes
    val servingsCount: Int = 1,
    val description: String = ""
) : Product(id, name, price)




