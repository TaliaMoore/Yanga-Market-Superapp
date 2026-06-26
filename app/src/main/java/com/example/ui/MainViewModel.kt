package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.graphql.*
import com.example.data.network.*
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val graphQLClient = MockGraphQLClient(database, application)
    
    val firebaseAuthEngine: com.example.domain.auth.FirebaseAuthEngine = com.example.domain.auth.FirebaseAuthEngineImpl()
    
    private val prefs = application.getSharedPreferences("yanga_prefs", android.content.Context.MODE_PRIVATE)
    
    // --- Global User Authentication & Location States ---
    private val _isUserAuthenticated = MutableStateFlow(prefs.getBoolean("is_authenticated", false))
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userLocation = MutableStateFlow(prefs.getString("user_location", "") ?: "")
    val userLocation: StateFlow<String> = _userLocation.asStateFlow()

    private val _userLatitude = MutableStateFlow(prefs.getFloat("user_latitude", 6.4281f).toDouble()) // Lagos Default
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(prefs.getFloat("user_longitude", 3.4219f).toDouble()) // Lagos Default
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    private val _userPhoneOrEmail = MutableStateFlow(prefs.getString("user_phone_or_email", "") ?: "")
    val userPhoneOrEmail: StateFlow<String> = _userPhoneOrEmail.asStateFlow()

    private val _checkoutPin = MutableStateFlow(prefs.getString("checkout_pin", "") ?: "")
    val checkoutPin: StateFlow<String> = _checkoutPin.asStateFlow()

    fun setCheckoutPin(pin: String) {
        _checkoutPin.value = pin
        prefs.edit().putString("checkout_pin", pin).apply()
        _successBannerMessage.value = "PIN configured successfully! 🔐"
    }

    private val _customerPresetId = MutableStateFlow(prefs.getInt("customer_preset_id", 1))
    val customerPresetId: StateFlow<Int> = _customerPresetId.asStateFlow()

    private val _customerCustomUri = MutableStateFlow(prefs.getString("customer_custom_uri", "") ?: "")
    val customerCustomUri: StateFlow<String> = _customerCustomUri.asStateFlow()

    private val _freelancePresetId = MutableStateFlow(prefs.getInt("freelance_preset_id", 2))
    val freelancePresetId: StateFlow<Int> = _freelancePresetId.asStateFlow()

    private val _freelanceCustomUri = MutableStateFlow(prefs.getString("freelance_custom_uri", "") ?: "")
    val freelanceCustomUri: StateFlow<String> = _freelanceCustomUri.asStateFlow()

    fun updateCustomerPreset(id: Int) {
        _customerPresetId.value = id
        _customerCustomUri.value = ""
        prefs.edit().putInt("customer_preset_id", id).putString("customer_custom_uri", "").apply()
        _successBannerMessage.value = "Customer profile icon updated! 🦄"
    }

    fun updateCustomerCustomUri(uri: String) {
        _customerCustomUri.value = uri
        prefs.edit().putString("customer_custom_uri", uri).apply()
        _successBannerMessage.value = "Customer profile photo uploaded successfully! 📸"
    }

    fun updateFreelancePreset(id: Int) {
        _freelancePresetId.value = id
        _freelanceCustomUri.value = ""
        prefs.edit().putInt("freelance_preset_id", id).putString("freelance_custom_uri", "").apply()
        _successBannerMessage.value = "Freelancer profile icon updated! 👦"
    }

    fun updateFreelanceCustomUri(uri: String) {
        _freelanceCustomUri.value = uri
        prefs.edit().putString("freelance_custom_uri", uri).apply()
        _successBannerMessage.value = "Freelancer profile photo uploaded successfully! 📸"
    }

    private val _loginMethod = MutableStateFlow(prefs.getString("login_method", "") ?: "") // "Google", "Email", "Phone"
    val loginMethod: StateFlow<String> = _loginMethod.asStateFlow()

    // --- High-fidelity Support Tickets & Customer Complaints State Flow ---
    private val _complaints = MutableStateFlow<List<YangaComplaint>>(
        listOf(
            YangaComplaint(
                id = "YNG-COMP-4812",
                category = "Wallet Transaction Error",
                title = "Fund wallet delay",
                details = "Tried funding 5,000 NGN via bank transfer, took 5 minutes to show up. Resolved now, just wanted to highlight.",
                timestamp = "2026-06-20 14:32",
                status = "Resolved"
            ),
            YangaComplaint(
                id = "YNG-COMP-9011",
                category = "Food Delivery Delay",
                title = "Spicy jollof arrived lukewarm",
                details = "The rider took quite some time navigating the Third Mainland Bridge, and the food was not hot when it got here.",
                timestamp = "2026-06-23 18:15",
                status = "Under Investigation"
            )
        )
    )
    val complaints: StateFlow<List<YangaComplaint>> = _complaints.asStateFlow()

    fun launchComplaint(category: String, title: String, details: String) {
        val ticketId = "YNG-COMP-${(1000..9999).random()}"
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val timestamp = sdf.format(java.util.Date())
        val newComplaint = YangaComplaint(
            id = ticketId,
            category = category,
            title = title,
            details = details,
            timestamp = timestamp,
            status = "Submitted"
        )
        _complaints.value = listOf(newComplaint) + _complaints.value
        _successBannerMessage.value = "Complaint submitted! Ticket: $ticketId. We are actively reviewing this. 🛠️"
    }

    fun hasOnboarded(): Boolean {
        return prefs.getBoolean("has_onboarded", false)
    }

    fun setOnboarded(onboarded: Boolean) {
        prefs.edit().putBoolean("has_onboarded", onboarded).apply()
    }

    fun completeProfileAndLogIn(name: String, location: String, lat: Double, lng: Double) {
        _userName.value = name
        _userLocation.value = location
        _userLatitude.value = lat
        _userLongitude.value = lng
        _isUserAuthenticated.value = true
        
        prefs.edit()
            .putBoolean("is_authenticated", true)
            .putBoolean("has_onboarded", true)
            .putString("user_name", name)
            .putString("user_location", location)
            .putFloat("user_latitude", lat.toFloat())
            .putFloat("user_longitude", lng.toFloat())
            .putString("user_phone_or_email", _userPhoneOrEmail.value)
            .putString("login_method", _loginMethod.value)
            .apply()

        _successBannerMessage.value = "Welcome to Yanga Market, $name! 🌟 Your profile and verified location are locked in securely."
    }

    fun updateUserName(name: String) {
        _userName.value = name
        prefs.edit().putString("user_name", name).apply()
        _successBannerMessage.value = "Username updated to @$name successfully! 👤"
    }

    fun setLoginDetails(identifier: String, method: String) {
        _userPhoneOrEmail.value = identifier
        _loginMethod.value = method
        if (method == "Google") {
            _userName.value = identifier.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        prefs.edit()
            .putString("user_phone_or_email", identifier)
            .putString("login_method", method)
            .putString("user_name", _userName.value)
            .apply()
    }

    fun logOutUser() {
        _isUserAuthenticated.value = false
        _userName.value = ""
        _userLocation.value = ""
        _userPhoneOrEmail.value = ""
        _loginMethod.value = ""
        
        firebaseAuthEngine.signOut()
        
        prefs.edit()
            .putBoolean("is_authenticated", false)
            .putString("user_name", "")
            .putString("user_location", "")
            .putString("user_phone_or_email", "")
            .putString("login_method", "")
            .apply()

        _successBannerMessage.value = "Log out successful. Come back soon! 👋"
    }

    fun deleteAccount() {
        _isUserAuthenticated.value = false
        _userName.value = ""
        _userLocation.value = ""
        _userPhoneOrEmail.value = ""
        _loginMethod.value = ""
        
        firebaseAuthEngine.signOut()
        
        prefs.edit()
            .putBoolean("is_authenticated", false)
            .putString("user_name", "")
            .putString("user_location", "")
            .putString("user_phone_or_email", "")
            .putString("login_method", "")
            .apply()

        _successBannerMessage.value = "Your Yanga superapp account has been permanently deleted. We are sad to see you go! 😢"
    }

    // --- Firebase Email/Password Integration ---
    fun firebaseEmailSignIn(emailInput: String, passInput: String, onResult: (Boolean) -> Unit) {
        firebaseAuthEngine.signInWithEmail(emailInput, passInput) { result ->
            when (result) {
                is com.example.domain.auth.AuthResult.Success -> {
                    setLoginDetails(result.user.email ?: emailInput, "Email")
                    _successBannerMessage.value = "Firebase Authenticated successfully! Welcome back."
                    onResult(true)
                }
                is com.example.domain.auth.AuthResult.Failure -> {
                    _errorBannerMessage.value = result.errorMessage
                    onResult(false)
                }
            }
        }
    }

    fun firebaseEmailSignUp(emailInput: String, passInput: String, onResult: (Boolean) -> Unit) {
        firebaseAuthEngine.signUpWithEmail(emailInput, passInput) { result ->
            when (result) {
                is com.example.domain.auth.AuthResult.Success -> {
                    setLoginDetails(result.user.email ?: emailInput, "Email")
                    _successBannerMessage.value = "Firebase account registered successfully! 🚀"
                    onResult(true)
                }
                is com.example.domain.auth.AuthResult.Failure -> {
                    _errorBannerMessage.value = result.errorMessage
                    onResult(false)
                }
            }
        }
    }

    // --- Firebase Phone SMS / OTP OTP Integration ---
    fun firebaseRequestOtp(phoneNumber: String, activity: android.app.Activity, onCodeSent: (String) -> Unit, onError: (String) -> Unit) {
        firebaseAuthEngine.sendOtpCode(
            phoneNumber = phoneNumber,
            activity = activity,
            onCodeSent = { verificationId ->
                _successBannerMessage.value = "Verification secure code queued with carriers! Check your message inbox."
                onCodeSent(verificationId)
            },
            onVerificationFailed = { errorMsg ->
                _errorBannerMessage.value = errorMsg
                onError(errorMsg)
            },
            onVerificationCompleted = { user ->
                setLoginDetails(user.phoneNumber ?: phoneNumber, "Phone")
                _successBannerMessage.value = "Instant Telephone handshake complete! Access authorized."
            }
        )
    }

    fun firebaseVerifyOtp(verificationId: String, smsCode: String, onResult: (Boolean) -> Unit) {
        firebaseAuthEngine.verifyOtpCode(verificationId, smsCode) { result ->
            when (result) {
                is com.example.domain.auth.AuthResult.Success -> {
                    setLoginDetails(result.user.phoneNumber ?: "Phone-Account", "Phone")
                    _successBannerMessage.value = "Secure Mobile verification successfully sealed! ✓"
                    onResult(true)
                }
                is com.example.domain.auth.AuthResult.Failure -> {
                    _errorBannerMessage.value = result.errorMessage
                    onResult(false)
                }
            }
        }
    }
    
    // --- Google OAuth 2.0 Integration removed for security flow ---
    
    // --- Secure Passport Identity, Storage Vault, and Appointment Alert Integration ---
    private val passportManager = PassportManager()
    val passportStatus = passportManager.status
    val passportProfile = passportManager.profile
    val passportVaultItems = passportManager.vaultItems
    val passportNotifications = passportManager.notifications

    fun enrollPassport(name: String, email: String, useBiometrics: Boolean) {
        viewModelScope.launch {
            passportManager.enrollYangaPassport(name, email, useBiometrics)
            _successBannerMessage.value = "Secure Yanga Passport successfully registered and authenticating! 🛂🔐"
        }
    }

    fun unlinkPassportProfile() {
        passportManager.unlinkPassport()
        _successBannerMessage.value = "Safe Sign-Out and secure credentials recycled from device keyspace. 🛡️🔑"
    }

    fun addToPassportVault(title: String, secretValue: String, category: String) {
        passportManager.addVaultItem(title, secretValue, category)
        _successBannerMessage.value = "Secure credential item added to cryptographic Passport Vault! 🔐📦"
    }

    fun deleteFromPassportVault(id: String) {
        passportManager.deleteVaultItem(id)
        _successBannerMessage.value = "Removed credential backup. 🗑️❌"
    }

    fun dismissPassportNotification(id: String) {
        passportManager.dismissNotification(id)
    }

    fun markPassportNotificationAsRead(id: String) {
        passportManager.markNotificationAsRead(id)
    }
    
    // --- Live WebSocket Realtime Stream Service ---
    val webSocketService = YangaWebSocketService.getInstance(application)
    val webSocketStatus = webSocketService.connectionState
    val webSocketLogs = webSocketService.liveNetworkLogs

    private val _activeThreads = MutableStateFlow<List<SafeVibeMessage.CreatedThread>>(emptyList())
    val activeThreads: StateFlow<List<SafeVibeMessage.CreatedThread>> = _activeThreads.asStateFlow()

    private val _registeredThreadUsers = MutableStateFlow<List<SafeVibeMessage.AddedUserToThread>>(emptyList())
    val registeredThreadUsers: StateFlow<List<SafeVibeMessage.AddedUserToThread>> = _registeredThreadUsers.asStateFlow()

    fun connectWebSocket() {
        webSocketService.connect()
    }

    fun disconnectWebSocket() {
        webSocketService.disconnect()
    }

    fun triggerCreatedThread(creator: String, title: String, description: String) {
        webSocketService.sendFrame(
            WebSocketFrame.CreateThreadCommand(
                creator = creator,
                title = title,
                description = description
            )
        )
    }

    fun triggerAddedUserToThread(threadId: String, userId: String, addedBy: String, customStatus: String) {
        webSocketService.sendFrame(
            WebSocketFrame.AddUserCommand(
                threadId = threadId,
                userId = userId,
                addedBy = addedBy,
                customStatus = customStatus
            )
        )
    }

    // --- GraphQLEmulated Static Catalogs ---
    private val _foods = MutableStateFlow<List<FoodItem>>(emptyList())
    val foods: StateFlow<List<FoodItem>> = _foods.asStateFlow()

    private val _fruits = MutableStateFlow<List<FoodItem>>(emptyList())
    val fruits: StateFlow<List<FoodItem>> = _fruits.asStateFlow()

    private val _selectedFruitFields = MutableStateFlow(listOf("name", "price", "category", "description"))
    val selectedFruitFields: StateFlow<List<String>> = _selectedFruitFields.asStateFlow()

    private val _currentFruitsQuery = MutableStateFlow("query GetFruitsOnly {\n  fruits {\n    name\n    price\n    category\n    description\n  }\n}")
    val currentFruitsQuery: StateFlow<String> = _currentFruitsQuery.asStateFlow()

    private val _retailShops = MutableStateFlow<List<RetailShop>>(emptyList())
    val retailShops: StateFlow<List<RetailShop>> = _retailShops.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // --- Services Marketplace State Flows ---
    private val _freelancers = MutableStateFlow<List<FreelancerProfile>>(emptyList())
    val freelancers: StateFlow<List<FreelancerProfile>> = _freelancers.asStateFlow()

    private val _escrowBookings = MutableStateFlow<List<EscrowProjectBooking>>(emptyList())
    val escrowBookings: StateFlow<List<EscrowProjectBooking>> = _escrowBookings.asStateFlow()

    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
    
    private val _hospitalSearchQuery = MutableStateFlow("")
    val hospitalSearchQuery: StateFlow<String> = _hospitalSearchQuery.asStateFlow()

    fun updateHospitalSearchQuery(query: String) {
        _hospitalSearchQuery.value = query
    }

    private val _hospitalSearchError = MutableStateFlow<String?>(null)
    val hospitalSearchError: StateFlow<String?> = _hospitalSearchError.asStateFlow()

    val hospitals: StateFlow<List<Hospital>> = combine(_hospitals, _hospitalSearchQuery) { list, query ->
        _hospitalSearchError.value = null
        if (query.isBlank()) {
            list
        } else {
            try {
                graphQLClient.directoryService.lookup(query)
            } catch (e: NoSuchElementException) {
                _hospitalSearchError.value = e.message
                emptyList()
            } catch (e: Exception) {
                _hospitalSearchError.value = "An error occurred: ${e.localizedMessage}"
                emptyList()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Shell-Style Terminal Console Interface ---
    private val _terminalConsoleInput = MutableStateFlow("")
    val terminalConsoleInput: StateFlow<String> = _terminalConsoleInput.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(listOf(
        "Yanga Shell Console Terminal [Active]",
        "Type `help` or tap a command preset to execute.",
        "----------------------------------------------"
    ))
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    fun updateTerminalInput(input: String) {
        _terminalConsoleInput.value = input
    }

    fun executeTerminalCommand(cmdString: String) {
        val trimmed = cmdString.trim()
        if (trimmed.isBlank()) return

        val newLogs = _terminalLogs.value.toMutableList()
        newLogs.add("$ $trimmed")

        try {
            val parts = trimmed.split(" ", limit = 2)
            val action = parts[0]
            val arguments = if (parts.size > 1) parts[1] else ""

            when (action) {
                "help" -> {
                    newLogs.add("Available Directory Operations:")
                    newLogs.add("  1 <query>              -> Look up matching medical center details.")
                    newLogs.add("  2 <name>, <location>, <dist>, <spec1;spec2> -> Add a new hospital to directory.")
                    newLogs.add("  3 <name_or_id>         -> Remove a matching hospital from directory.")
                    newLogs.add("  clear                  -> Clear console output history.")
                }
                "clear" -> {
                    _terminalLogs.value = listOf("Console history cleared. Type help or try a command preset.")
                    _terminalConsoleInput.value = ""
                    return
                }
                "1" -> {
                    if (arguments.isBlank()) {
                        newLogs.add("Result Error: Supply lookup term. Preset syntax -> 1 <query>")
                    } else {
                        try {
                            val matches = graphQLClient.directoryService.lookup(arguments)
                            newLogs.add("Lookup Success: Verified ${matches.size} candidate(s):")
                            matches.forEach { h ->
                                newLogs.add("   • Name: ${h.name} | Address: ${h.location} | Dist: ${h.distanceKm}km | Specs: ${h.specialties.joinToString()}")
                            }
                        } catch (e: NoSuchElementException) {
                            newLogs.add("Lookup: ${e.message}")
                        } catch (e: Exception) {
                            newLogs.add("Lookup Error: ${e.localizedMessage}")
                        }
                    }
                }
                "2" -> {
                    val csv = arguments.split(",")
                    if (csv.size < 4) {
                        newLogs.add("Result Error: Insufficient parameters. Preset syntax -> 2 <name>, <location>, <distanceKm>, <specs>")
                    } else {
                        val nameStr = csv[0].trim()
                        val locStr = csv[1].trim()
                        val distDbl = csv[2].trim().toDoubleOrNull() ?: 1.2
                        val specsLst = csv[3].trim().split(";").map { it.trim() }.filter { it.isNotEmpty() }

                        val addedFacility = Hospital(
                            id = UUID.randomUUID().toString(),
                            name = nameStr,
                            location = locStr,
                            distanceKm = distDbl,
                            specialties = specsLst,
                            openHours = "24/7"
                        )
                        graphQLClient.directoryService.addHospital(addedFacility)
                        _hospitals.value = graphQLClient.directoryService.getAllHospitals()
                        newLogs.add("Operation Completed Successfully: Added \"$nameStr\" to primary healthcare registry.")
                    }
                }
                "3" -> {
                    if (arguments.isBlank()) {
                        newLogs.add("Result Error: Supply hospital name to remove. Preset syntax -> 3 [name]")
                    } else {
                        val removedById = graphQLClient.directoryService.removeHospital(arguments)
                        val removedByName = if (!removedById) {
                            graphQLClient.directoryService.removeHospitalByName(arguments)
                        } else true

                        if (removedById || removedByName) {
                            _hospitals.value = graphQLClient.directoryService.getAllHospitals()
                            newLogs.add("Operation Completed Successfully: Removed \"$arguments\" from primary healthcare registry.")
                        } else {
                            newLogs.add("Operation Warning: No hospital found matching target name \"$arguments\".")
                        }
                    }
                }
                else -> {
                    newLogs.add("Command Error: Option \"$action\" unrecognized. Use preset numbers 1, 2, 3 or type 'help'.")
                }
            }
        } catch (e: Exception) {
            newLogs.add("Runtime Error: ${e.localizedMessage}")
        }

        _terminalLogs.value = newLogs
        _terminalConsoleInput.value = ""
    }

    // --- Interactive Facility Space Inquiry & Pricing Loop (2D Array) ---
    val facilityRentDirectory = com.example.domain.model.FacilityRentDirectory()

    private val _facilitySearchQuery = MutableStateFlow("")
    val facilitySearchQuery: StateFlow<String> = _facilitySearchQuery.asStateFlow()

    private val _facilitySelectedFloor = MutableStateFlow<Int?>(null) // null = all or search query
    val facilitySelectedFloor: StateFlow<Int?> = _facilitySelectedFloor.asStateFlow()

    fun updateFacilitySearchQuery(query: String) {
        _facilitySearchQuery.value = query
        if (query.isNotBlank()) {
            _facilitySelectedFloor.value = null
        }
    }

    fun selectFacilityFloor(floorNum: Int?) {
        _facilitySelectedFloor.value = floorNum
        if (floorNum != null) {
            _facilitySearchQuery.value = ""
        }
    }

    val lookedUpSections: StateFlow<List<com.example.domain.model.FacilitySection>> = combine(
        _facilitySearchQuery,
        _facilitySelectedFloor
    ) { query, selectedFloor ->
        if (selectedFloor != null) {
            facilityRentDirectory.findByFloorNumber(selectedFloor)
        } else if (query.isNotBlank()) {
            val floorParsed = query.trim().toIntOrNull()
            if (floorParsed != null && floorParsed in 1..4) {
                facilityRentDirectory.findByFloorNumber(floorParsed)
            } else {
                val exactSec = facilityRentDirectory.findByLocationCode(query)
                if (exactSec != null) {
                    listOf(exactSec)
                } else {
                    facilityRentDirectory.fetchRawGrid().flatMap { it.toList() }.filter {
                        it.servicesSubtype.contains(query, ignoreCase = true) ||
                        it.locationCode.contains(query, ignoreCase = true)
                    }
                }
            }
        } else {
            facilityRentDirectory.fetchRawGrid().flatMap { it.toList() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _quotesLibrary = MutableStateFlow<QuotesLibraryType?>(null)
    val quotesLibrary: StateFlow<QuotesLibraryType?> = _quotesLibrary.asStateFlow()

    // --- Reactive Local Flows from Room Database ---
    val cartItems: StateFlow<List<CartItemEntity>> = database.cartItemDao().getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vibePosts: StateFlow<List<VibePostEntity>> = database.vibePostDao().getAllVibePosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = database.walletTransactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedBookings: StateFlow<List<SavedBookingEntity>> = database.savedBookingDao().getSavedBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- OOP Modular Secure Wallet Engine ---
    private val secureWalletEngine: com.example.domain.wallet.WalletEngine = com.example.domain.wallet.SecureWalletEngineImpl()

    // --- Computed Reactive Wallet Balance ---
    val walletBalance: StateFlow<Double> = walletTransactions.map { txList ->
        val secureTxs = txList.map { entity ->
            com.example.domain.wallet.SecureWalletTransaction(
                id = entity.id,
                customerId = entity.customerId ?: "CUST-01",
                type = when (entity.type) {
                    "FUND" -> com.example.domain.wallet.WalletOpType.FUND
                    "PAYMENT" -> com.example.domain.wallet.WalletOpType.PAYMENT
                    else -> com.example.domain.wallet.WalletOpType.REFUND
                },
                amount = entity.amount,
                description = entity.description,
                timestamp = entity.timestamp,
                signature = entity.securityHash
            )
        }
        secureWalletEngine.calculateBalance(secureTxs, 10000.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    // --- Computed Secure Ledger Integrity Audit Report ---
    val walletAuditReport: StateFlow<com.example.domain.wallet.WalletAuditReport> = walletTransactions.map { txList ->
        val secureTxs = txList.map { entity ->
            com.example.domain.wallet.SecureWalletTransaction(
                id = entity.id,
                customerId = entity.customerId ?: "CUST-01",
                type = when (entity.type) {
                    "FUND" -> com.example.domain.wallet.WalletOpType.FUND
                    "PAYMENT" -> com.example.domain.wallet.WalletOpType.PAYMENT
                    else -> com.example.domain.wallet.WalletOpType.REFUND
                },
                amount = entity.amount,
                description = entity.description,
                timestamp = entity.timestamp,
                signature = entity.securityHash
            )
        }
        secureWalletEngine.auditLedgerIntegrity(secureTxs, 10000.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.domain.wallet.WalletAuditReport(
        isSystemAuthentic = true,
        totalTransactionsCount = 0,
        computedBalance = 10000.0,
        anomalyCount = 0,
        corruptedTxnIds = emptyList()
    ))

    // --- Unified Network Status, Banners, and Inputs ---
    private val _dashboardMarketFilter = MutableStateFlow<String>("None") // "None", "Supermarket", "Bakery"
    val dashboardMarketFilter: StateFlow<String> = _dashboardMarketFilter.asStateFlow()

    fun setDashboardMarketFilter(filter: String) {
        _dashboardMarketFilter.value = filter
    }

    private val _isGraphQLFetching = MutableStateFlow(false)
    val isGraphQLFetching: StateFlow<Boolean> = _isGraphQLFetching.asStateFlow()

    private val _vibeAuthorInput = MutableStateFlow("")
    val vibeAuthorInput: StateFlow<String> = _vibeAuthorInput.asStateFlow()

    private val _vibeContentInput = MutableStateFlow("")
    val vibeContentInput: StateFlow<String> = _vibeContentInput.asStateFlow()

    private val _vibeAttachedPhotoInput = MutableStateFlow<String?>(null)
    val vibeAttachedPhotoInput: StateFlow<String?> = _vibeAttachedPhotoInput.asStateFlow()

    fun selectAttachedPhoto(photoName: String?) {
        _vibeAttachedPhotoInput.value = photoName
    }

    private val _errorBannerMessage = MutableStateFlow<String?>(null)
    val errorBannerMessage: StateFlow<String?> = _errorBannerMessage.asStateFlow()

    private val _successBannerMessage = MutableStateFlow<String?>(null)
    val successBannerMessage: StateFlow<String?> = _successBannerMessage.asStateFlow()

    // --- Yanga Superapp Notification Center ---
    private val _notifications = MutableStateFlow<List<YangaNotification>>(listOf(
        YangaNotification(
            id = "notif_1",
            title = "Weekly Mega Draw Entry Successful! 🎟️✨",
            message = "You have successfully registered Ticket #YNG-9482 for this week's ₦50,000.00 jackpot draw. Win announcements will trigger on Friday!",
            timestamp = "Today, 14:15",
            icon = "🎟️",
            isRead = false,
            type = "DRAW"
        ),
        YangaNotification(
            id = "notif_2",
            title = "Ledger Transfer Authorized! ⚡💜",
            message = "Your direct wallet payment of ₦3,500.00 for Friday Fiesta was successfully completed. Digital hash: tx_99214b",
            timestamp = "Yesterday, 18:40",
            icon = "⚡",
            isRead = true,
            type = "TRANSACTION"
        ),
        YangaNotification(
            id = "notif_3",
            title = "Purple Peacock Engagement Bonus! 🦚💰",
            message = "A brand-new Yanga Community engagement bonus of +10 Silver Coins was credited to your coin purse! Keep sharing positive vibes on the boards.",
            timestamp = "3 days ago",
            icon = "🦚",
            isRead = false,
            type = "BONUS"
        )
    ))
    val notifications: StateFlow<List<YangaNotification>> = _notifications.asStateFlow()

    fun addNotification(title: String, message: String, type: String, icon: String) {
        val newNotification = YangaNotification(
            id = "notif_" + System.currentTimeMillis(),
            title = title,
            message = message,
            timestamp = "Just Now",
            icon = icon,
            isRead = false,
            type = type
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    // --- Yanga Coin Purse & Draw States ---
    private val _silverCoins = MutableStateFlow(prefs.getInt("silver_coins", 45))
    val silverCoins: StateFlow<Int> = _silverCoins.asStateFlow()

    private val _goldCoins = MutableStateFlow(prefs.getInt("gold_coins", 2))
    val goldCoins: StateFlow<Int> = _goldCoins.asStateFlow()

    private val _drawTickets = MutableStateFlow(prefs.getInt("draw_tickets", 0))
    val drawTickets: StateFlow<Int> = _drawTickets.asStateFlow()

    private val _drawTotalTicketsAll = MutableStateFlow(prefs.getInt("draw_total_tickets_all", 1420))
    val drawTotalTicketsAll: StateFlow<Int> = _drawTotalTicketsAll.asStateFlow()

    private val _drawTotalParticipants = MutableStateFlow(prefs.getInt("draw_total_participants", 480))
    val drawTotalParticipants: StateFlow<Int> = _drawTotalParticipants.asStateFlow()

    fun addSilverCoins(amount: Int, reason: String = "") {
        val newVal = _silverCoins.value + amount
        _silverCoins.value = newVal
        prefs.edit().putInt("silver_coins", newVal).apply()
        if (reason.isNotEmpty()) {
            _successBannerMessage.value = "You earned +$amount Silver Coins! Reason: $reason 🪙"
        }
    }

    fun addGoldCoins(amount: Int) {
        val newVal = _goldCoins.value + amount
        _goldCoins.value = newVal
        prefs.edit().putInt("gold_coins", newVal).apply()
    }

    fun convertSilverToGold() {
        if (_silverCoins.value >= 100) {
            val convertedGold = _silverCoins.value / 100
            val remainingSilver = _silverCoins.value % 100
            
            _silverCoins.value = remainingSilver
            _goldCoins.value = _goldCoins.value + convertedGold
            
            prefs.edit()
                .putInt("silver_coins", remainingSilver)
                .putInt("gold_coins", _goldCoins.value)
                .apply()
            
            _successBannerMessage.value = "Converted ${convertedGold * 100} Silver Pieces to $convertedGold Gold Pieces! 🪙✨"
        } else {
            _errorBannerMessage.value = "You need at least 100 Silver Pieces to convert to 1 Gold Piece!"
        }
    }

    fun enterDraw(ticketCount: Int = 1) {
        if (_goldCoins.value >= ticketCount) {
            _goldCoins.value = _goldCoins.value - ticketCount
            _drawTickets.value = _drawTickets.value + ticketCount
            
            // Increment total tickets in the pool randomly to simulate activity
            _drawTotalTicketsAll.value = _drawTotalTicketsAll.value + ticketCount + (1..3).random()
            _drawTotalParticipants.value = _drawTotalParticipants.value + if ((0..1).random() == 1) 1 else 0
            
            prefs.edit()
                .putInt("gold_coins", _goldCoins.value)
                .putInt("draw_tickets", _drawTickets.value)
                .putInt("draw_total_tickets_all", _drawTotalTicketsAll.value)
                .putInt("draw_total_participants", _drawTotalParticipants.value)
                .apply()
            
            _successBannerMessage.value = "Successfully entered $ticketCount ticket(s) into the Draw! Good luck! 🎟️🌟"
            addNotification(
                title = "New Draw Entry Confirmed! 🎟️✨",
                message = "You entered $ticketCount ticket(s) into the Weekly Mega Draw. Total registered tickets: ${_drawTickets.value}. May the purple peacock guide you to victory!",
                type = "DRAW",
                icon = "🎟️"
            )
        } else {
            _errorBannerMessage.value = "You do not have enough Gold Pieces! Earn more silver and convert them."
        }
    }

    fun boostVibeLikes(vibeId: String) {
        viewModelScope.launch {
            val currentPosts = database.vibePostDao().getAllVibePostsDirect()
            val match = currentPosts.find { it.id == vibeId }
            if (match != null) {
                val updated = match.copy(
                    vibeCount = match.vibeCount + 100
                )
                database.vibePostDao().insertVibePost(updated)
                addSilverCoins(1, "Post reached 100+ vibe checks!")
            }
        }
    }

    // --- Discussion Groups & selected post states ---
    private val _selectedVibePostId = MutableStateFlow<String?>(null)
    val selectedVibePostId: StateFlow<String?> = _selectedVibePostId.asStateFlow()

    private val _discussionGroups = MutableStateFlow<List<com.example.domain.model.DiscussionGroup>>(
        listOf(
            com.example.domain.model.DiscussionGroup(id = "grp-1", name = "jollof-lovers", description = "Ratings, reviews & debates about the best Lagos Jollof rice spots! 🌶️🍚", memberCount = 142, isJoined = true, category = "Food"),
            com.example.domain.model.DiscussionGroup(id = "grp-2", name = "tech-suya-lagos", description = "Super-citizens talking code, products, startup pitches & spicy beef! 🔌🍢", memberCount = 89, isJoined = false, category = "Tech"),
            com.example.domain.model.DiscussionGroup(id = "grp-3", name = "bulk-buy-deals", description = "Coordinating fruits and food crate orders to split costs 100% equally! 🍉📦", memberCount = 204, isJoined = true, category = "Market"),
            com.example.domain.model.DiscussionGroup(id = "grp-4", name = "yanga-riders", description = "Real-time traffic info, dispatcher speeds, and route updates! 🏍️⚡", memberCount = 57, isJoined = false, category = "Logistics")
        )
    )
    val discussionGroups: StateFlow<List<com.example.domain.model.DiscussionGroup>> = _discussionGroups.asStateFlow()

    fun toggleJoinGroup(groupId: String) {
        _discussionGroups.value = _discussionGroups.value.map { g ->
            if (g.id == groupId) {
                val nextJoined = !g.isJoined
                g.copy(
                    isJoined = nextJoined,
                    memberCount = if (nextJoined) g.memberCount + 1 else g.memberCount - 1
                )
            } else g
        }
        val grp = _discussionGroups.value.find { it.id == groupId }
        if (grp != null) {
            _successBannerMessage.value = if (grp.isJoined) "You joined #${grp.name}! Welcome to the vibe! 🎉" else "You left #${grp.name}."
        }
    }

    fun createDiscussionGroup(name: String, description: String, category: String) {
        if (name.isBlank() || description.isBlank()) {
            _errorBannerMessage.value = "Group name and description cannot be empty!"
            return
        }
        val normalizedName = name.trim().lowercase().replace(" ", "-").replace("#", "")
        val newGroup = com.example.domain.model.DiscussionGroup(
            name = normalizedName,
            description = description.trim(),
            category = category.trim(),
            memberCount = 1,
            isJoined = true
        )
        _discussionGroups.value = _discussionGroups.value + newGroup
        _successBannerMessage.value = "Group #$normalizedName created and joined successfully! 📣"
    }

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private val vibeCommentsAdapter = moshi.adapter<List<com.example.domain.model.VibeComment>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.domain.model.VibeComment::class.java)
    )

    fun parseComments(json: String): List<com.example.domain.model.VibeComment> {
        return try {
            vibeCommentsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeComments(comments: List<com.example.domain.model.VibeComment>): String {
        return try {
            vibeCommentsAdapter.toJson(comments)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun selectVibePost(postId: String?) {
        _selectedVibePostId.value = postId
    }

    fun addCommentToPost(postId: String, author: String, content: String) {
        if (author.trim().isEmpty() || content.trim().isEmpty()) {
            _errorBannerMessage.value = "Please enter both your name and comment!"
            return
        }

        viewModelScope.launch {
            val currentPosts = database.vibePostDao().getAllVibePostsDirect()
            val post = currentPosts.find { it.id == postId }
            if (post != null) {
                val currentComments = parseComments(post.commentsJson).toMutableList()
                currentComments.add(
                    com.example.domain.model.VibeComment(
                        author = author.trim(),
                        content = content.trim(),
                        timestamp = System.currentTimeMillis()
                    )
                )
                val updatedPost = post.copy(
                    commentsJson = serializeComments(currentComments)
                )
                database.vibePostDao().insertVibePost(updatedPost)
                _successBannerMessage.value = "Comment added successfully! 💬"
            } else {
                _errorBannerMessage.value = "Post not found."
            }
        }
    }

    /**
     * Resets visual confirmation / alert banners.
     */
    fun clearBanners() {
        _errorBannerMessage.value = null
        _successBannerMessage.value = null
    }

    fun postError(message: String) {
        _errorBannerMessage.value = message
    }

    fun postSuccess(message: String) {
        _successBannerMessage.value = message
    }

    /**
     * Executes asynchronous schema queries via YangaGraphQLService using co-located fragment requirements
     */
    fun refreshGraphQLCatalogs() {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                // Query foods using dynamic co-located GraphQL fragment
                val foodQuery = GraphQLCoLocationRegistry.buildQueryForScreen(
                    "FoodCatalogComponent",
                    "query GetFoodsAndFruits { foods { ...FoodCatalogFields } }"
                )
                val foodRes = graphQLClient.executeGraphQL(GraphQLRequest(query = foodQuery))
                val dataMap = foodRes.data as? Map<*, *>
                val foodList = (dataMap?.get("foods") as? List<*>) ?: emptyList<Any>()
                val parsedFoods = foodList.filterIsInstance<FoodItem>()
                _foods.value = parsedFoods.filter { !it.isFruit }

                // Fetch fruits using current declarative selection (or defaults)
                queryFruitsDeclaratively(_selectedFruitFields.value)

                // Query retail shops using dynamic co-located GraphQL fragment
                val retailQuery = GraphQLCoLocationRegistry.buildQueryForScreen(
                    "RetailCatalogsComponent",
                    "query GetRetailShops { shops { ...RetailFields } }"
                )
                val retailRes = graphQLClient.executeGraphQL(GraphQLRequest(query = retailQuery))
                val retailData = retailRes.data as? Map<*, *>
                val retailList = (retailData?.get("shops") as? List<*>) ?: emptyList<Any>()
                _retailShops.value = retailList.filterIsInstance<RetailShop>()

                // Query events using dynamic co-located GraphQL fragment
                val eventsQuery = GraphQLCoLocationRegistry.buildQueryForScreen(
                    "EventsEngagementComponent",
                    "query GetUpcomingEvents { events { ...EventFields } }"
                )
                val eventsRes = graphQLClient.executeGraphQL(GraphQLRequest(query = eventsQuery))
                val eventsData = eventsRes.data as? Map<*, *>
                val eventsList = (eventsData?.get("events") as? List<*>) ?: emptyList<Any>()
                _events.value = eventsList.filterIsInstance<Event>()

                // Query hospitals using dynamic co-located GraphQL fragment
                val hospitalsQuery = GraphQLCoLocationRegistry.buildQueryForScreen(
                    "HospitalServicesComponent",
                    "query GetHospitals { hospitals { ...HospitalFields } }"
                )
                val hospitalsRes = graphQLClient.executeGraphQL(GraphQLRequest(query = hospitalsQuery))
                val hospitalsData = hospitalsRes.data as? Map<*, *>
                val hospitalsList = (hospitalsData?.get("hospitals") as? List<*>) ?: emptyList<Any>()
                _hospitals.value = hospitalsList.filterIsInstance<Hospital>()

                // Query restaurants
                val restaurantsRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetRestaurants { restaurants { name cuisine rating tablePrice } }"
                ))
                val restaurantsData = restaurantsRes.data as? Map<*, *>
                val restaurantsList = (restaurantsData?.get("restaurants") as? List<*>) ?: emptyList<Any>()
                _restaurants.value = restaurantsList.filterIsInstance<Restaurant>()

                // Query Freelancer Profiles from Services Marketplace
                val freelancersRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetFreelancers { freelancers { id name title avatarEmoji rating basePrice bio category portfolioGallery serviceListings } }"
                ))
                val freelancersData = freelancersRes.data as? Map<*, *>
                val freelancersList = (freelancersData?.get("freelancers") as? List<*>) ?: emptyList<Any>()
                _freelancers.value = freelancersList.filterIsInstance<FreelancerProfile>()

                // Query Escrow Project Bookings
                val escrowRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetEscrowBookings { escrowBookings { id freelancerId freelancerName selectedService totalAmountPaidToEscrow currentStatus milestones { id title costAmount status } timestamp } }"
                ))
                val escrowData = escrowRes.data as? Map<*, *>
                val escrowList = (escrowData?.get("escrowBookings") as? List<*>) ?: emptyList<Any>()
                _escrowBookings.value = escrowList.filterIsInstance<EscrowProjectBooking>()

                // Query Quotes container library efficiently via GraphQL abstractions
                val quotesRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetQuotesLibrary { quotesLibrary(category: \"All\") { quotes { id author text category } totalCount category } }",
                    variables = mapOf("category" to "All")
                ))
                val quotesData = quotesRes.data as? Map<*, *>
                _quotesLibrary.value = quotesData?.get("quotesLibrary") as? QuotesLibraryType

            } catch (e: Exception) {
                _errorBannerMessage.value = "GraphQL Fetch Failure: ${e.localizedMessage}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    /**
     * Fetch filtered quotes container library using parameterized GraphQL operation variables
     */
    fun fetchQuotesByCategory(category: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val quotesRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "query GetQuotesLibrary { quotesLibrary(category: \"$category\") { quotes { id author text category } totalCount category } }",
                    variables = mapOf("category" to category)
                ))
                val quotesData = quotesRes.data as? Map<*, *>
                _quotesLibrary.value = quotesData?.get("quotesLibrary") as? QuotesLibraryType
            } catch (e: Exception) {
                _errorBannerMessage.value = "Failed to query quotes catalog: ${e.localizedMessage}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    /**
     * Executes dynamic, declarative GraphQL Query for fruits, specifying exactly the fields to return.
     */
    fun queryFruitsDeclaratively(fields: List<String>) {
        _selectedFruitFields.value = fields
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val fieldProjection = fields.joinToString("\n    ")
                val queryStr = "query GetFruitsOnly {\n  fruits {\n    $fieldProjection\n  }\n}"
                _currentFruitsQuery.value = queryStr

                val fruitsRes = graphQLClient.executeGraphQL(GraphQLRequest(query = queryStr))
                val dataMap = fruitsRes.data as? Map<*, *>
                val list = (dataMap?.get("fruits") as? List<*>) ?: emptyList<Any>()
                _fruits.value = list.filterIsInstance<FoodItem>()
            } catch (e: Exception) {
                _errorBannerMessage.value = "GraphQL Selective Field Fail: ${e.localizedMessage}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    private fun preseedVibesIfEmpty() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val count = database.vibePostDao().getAllVibePostsDirect().size
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
        // OOP Core Pre-deposit validation logic
        try {
            secureWalletEngine.validateDeposit(amount)
        } catch (e: Exception) {
            _errorBannerMessage.value = "Core Security Exception: ${e.message}"
            return
        }

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

    fun transferWalletFunds(amount: Double, recipientName: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val currentBalance = walletBalance.value
            
            // OOP Core Pre-payment/P2P transfer validation logic
            try {
                secureWalletEngine.validatePayment(amount, currentBalance)
            } catch (e: Exception) {
                _errorBannerMessage.value = "Core Security Exception: ${e.message}"
                _isGraphQLFetching.value = false
                return@launch
            }

            val variablesPay = mapOf(
                "amount" to amount,
                "note" to "P2P Google Transfer to $recipientName"
            )
            val payRes = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                variables = variablesPay
            ))
            if (payRes.errors != null) {
                _errorBannerMessage.value = payRes.errors.first().message
            } else {
                _successBannerMessage.value = "Successfully sent ₦${String.format("%,.2f", amount)} secure to Google-Synced Contact '$recipientName'! ⚡🔑💜"
                addNotification(
                    title = "Ledger Transfer Success! ⚡💜",
                    message = "You transferred ₦${String.format("%,.2f", amount)} to $recipientName successfully. Hash signature has been added to audit ledger.",
                    type = "TRANSACTION",
                    icon = "⚡"
                )
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

    fun checkoutCart(paymentMethod: String = "WALLET") {
        val total = cartItems.value.sumOf { it.price * it.quantity }
        val itemsSummary = cartItems.value.joinToString(", ") { "${it.quantity}x ${it.name}" }

        if (total <= 0) {
            _errorBannerMessage.value = "Your checkout basket is empty!"
            return
        }

        viewModelScope.launch {
            _isGraphQLFetching.value = true
            var success = false

            if (paymentMethod == "WALLET") {
                // Pre-transaction sufficient funds validation check
                val currentBalance = walletBalance.value
                if (currentBalance < total) {
                    val errMsg = "Insufficient Wallet Balance! Current: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Total due: ₦${String.format(java.util.Locale.US, "%,.2f", total)}. Checkout rejected."
                    android.util.Log.e("YangaMarketBilling", "TRANSACTION REJECTED: $errMsg")
                    _errorBannerMessage.value = errMsg
                    _isGraphQLFetching.value = false
                    return@launch
                }

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
                    _isGraphQLFetching.value = false
                    return@launch
                }
                success = true
            } else {
                // Other methods like BANK_TRANSFER or CASH ON DELIVERY are instant-approve simulation flows
                success = true
            }

            if (success) {
                // Submit the order through the newly implemented GraphQL ordering mutation to receive a confirmation ID!
                val orderVariables = mapOf(
                    "amount" to total,
                    "itemsSummary" to itemsSummary
                )
                val orderRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation SubmitOrder(\$amount: Float!, \$itemsSummary: String!) { submitOrder(amount: \$amount, itemsSummary: \$itemsSummary) { id status estimatedDeliveryMinutes message } }",
                    variables = orderVariables
                ))

                if (orderRes.errors != null) {
                    _errorBannerMessage.value = "Checkout completed but order registration failed: ${orderRes.errors.first().message}"
                } else {
                    val dataMap = orderRes.data as? Map<*, *>
                    val submitOrderMap = dataMap?.get("submitOrder") as? Map<*, *>
                    val confirmationId = submitOrderMap?.get("id") as? String ?: "YNG-${System.currentTimeMillis() % 1000000}"
                    val msg = submitOrderMap?.get("message") as? String ?: "Processed successfully!"

                    // For each event in the checked-out cart, add a ticket/appointment booking!
                    cartItems.value.forEach { item ->
                        if (item.itemType == "EVENT") {
                            passportManager.addAppointmentNotification(
                                bookingId = "evt-${System.currentTimeMillis()}-${item.id.take(4)}",
                                title = item.name,
                                description = "Vibes Secure Ticket ($paymentMethod Checkout)",
                                time = "Upcoming Event Date"
                            )
                        }
                    }

                    // Calculate total quantity of items being purchased
                    val totalQuantity = cartItems.value.sumOf { it.quantity }
                    addSilverCoins(totalQuantity, "Purchased $totalQuantity items")

                    // Clear cart
                    database.cartItemDao().clearCart()
                    _successBannerMessage.value = "Secured successfully via $paymentMethod! Ref ID: $confirmationId. $msg. You earned $totalQuantity Silver Pieces! 🪙"
                    addNotification(
                        title = "Checkout Order Successful! 🛒⚡",
                        message = "Your order of $totalQuantity items ($itemsSummary) totaling ₦${String.format(java.util.Locale.US, "%,.2f", total)} was processed successfully. Reference ID: $confirmationId",
                        type = "TRANSACTION",
                        icon = "🛒"
                    )
                }
            }
            _isGraphQLFetching.value = false
        }
    }

    /**
     * Charges the wallet using the PayWithWallet GraphQL mutation, then saves a SavedBookingEntity 
     * in the local Room database under the RIDE type to list on the dashboard.
     */
    fun bookRideLogistics(vehicle: String, cost: Double, address: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            // Pre-transaction sufficient funds validation check
            val currentBalance = walletBalance.value
            if (currentBalance < cost) {
                val errMsg = "Insufficient Wallet Balance! Current: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Total due: ₦${String.format(java.util.Locale.US, "%,.2f", cost)}. Ride booking rejected."
                android.util.Log.e("YangaMarketBilling", "TRANSACTION REJECTED: $errMsg")
                _errorBannerMessage.value = errMsg
                _isGraphQLFetching.value = false
                return@launch
            }

            val variables = mapOf(
                "amount" to cost,
                "note" to "Yanga Ride: Reserved $vehicle to $address"
            )
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                variables = variables
            ))
            if (res.errors == null) {
                val bookingEntity = SavedBookingEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Yanga $vehicle Transit",
                    subtitle = "Destination: $address",
                    dateOrTime = "Estimated: 12-15 mins",
                    price = cost,
                    bookingType = "RIDE",
                    extraDetails = "Lagos Express Transit Logistics"
                )
                database.savedBookingDao().insertBooking(bookingEntity)
                _successBannerMessage.value = "Yanga Ride successfully booked! 💜🛵"
            } else {
                _errorBannerMessage.value = res.errors.first().message
            }
            _isGraphQLFetching.value = false
        }
    }

    // --- COMMUNITY VIBES BOARD (LET'S SHARE VIBES) ---

    private val _vibePostAsBusiness = MutableStateFlow(false)
    val vibePostAsBusiness: StateFlow<Boolean> = _vibePostAsBusiness.asStateFlow()

    fun toggleVibePostAsBusiness() {
        _vibePostAsBusiness.value = !_vibePostAsBusiness.value
    }

    fun setVibePostAsBusiness(value: Boolean) {
        _vibePostAsBusiness.value = value
    }

    fun updateVibeInputs(author: String?, content: String?) {
        if (author != null) _vibeAuthorInput.value = author
        if (content != null) _vibeContentInput.value = content
    }

    fun submitVibe() {
        var author = userName.value.trim()
        if (author.isEmpty()) {
            author = _vibeAuthorInput.value.trim()
        }
        var authorType = "USER"
        var businessId: String? = null

        if (myFreelancerAppStatus.value == "Approved") {
            authorType = "FREELANCER"
            businessId = myFreelancerProfileId.value
        } else if (myBusinessAppStatus.value == "Approved") {
            val bizName = prefs.getString("my_biz_name", "") ?: ""
            if (_vibePostAsBusiness.value && bizName.isNotBlank()) {
                author = bizName
                authorType = myBusinessCategory.value.uppercase()
                businessId = myBusinessProfileId.value
            } else {
                authorType = "USER"
                businessId = myBusinessProfileId.value
            }
        }

        val content = _vibeContentInput.value.trim()
        val photo = _vibeAttachedPhotoInput.value

        if (author.isEmpty() || content.isEmpty()) {
            _errorBannerMessage.value = "Please enter both your name and a vibe message!"
            return
        }

        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val variables = mutableMapOf<String, Any>(
                "author" to author,
                "content" to content
            )
            if (photo != null) {
                variables["attachedPhoto"] = photo
            }
            variables["authorType"] = authorType
            if (businessId != null) {
                variables["businessId"] = businessId
            }
            val res = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PostVibe(\$author: String!, \$content: String!, \$attachedPhoto: String, \$authorType: String, \$businessId: String) { addVibe(author: \$author, content: \$content, attachedPhoto: \$attachedPhoto, authorType: \$authorType, businessId: \$businessId) { id } }",
                variables = variables
            ))

            if (res.errors != null) {
                _errorBannerMessage.value = res.errors.first().message
            } else {
                _vibeContentInput.value = ""
                _vibeAttachedPhotoInput.value = null // Reset selected photo
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
                passportManager.addAppointmentNotification(
                    bookingId = "hosp-${System.currentTimeMillis()}",
                    title = hospital.name,
                    description = "Appointment: $service",
                    time = bookingDate
                )
            }
            _isGraphQLFetching.value = false
        }
    }

    fun purchaseEventTicket(event: Event) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val exist = cartItems.value.find { it.name == event.title }
            if (exist != null) {
                database.cartItemDao().insertCartItem(exist.copy(quantity = exist.quantity + 1))
            } else {
                val entity = CartItemEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = event.title,
                    price = event.price,
                    category = "Event Ticket",
                    quantity = 1,
                    itemType = "EVENT"
                )
                database.cartItemDao().insertCartItem(entity)
            }
            _successBannerMessage.value = "Added ticket for '${event.title}' to your cart! 🎟️🛒"
            _isGraphQLFetching.value = false
        }
    }

    fun bookDinnerCateringService(title: String, eventNumber: String, guests: Int, totalPrice: Double, contactPhone: String, entree: String, sides: String, dessert: String, extraInfo: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val currentBalance = walletBalance.value
            if (currentBalance < totalPrice) {
                val errMsg = "Insufficient Wallet Balance! Current: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Total due: ₦${String.format(java.util.Locale.US, "%,.2f", totalPrice)}. Catering booking rejected."
                android.util.Log.e("YangaMarketBilling", "TRANSACTION REJECTED: $errMsg")
                _errorBannerMessage.value = errMsg
                _isGraphQLFetching.value = false
                return@launch
            }

            val variablesPay = mapOf(
                "amount" to totalPrice,
                "note" to "Carly's Dinner Catering (#$eventNumber): $title"
            )
            val payRes = graphQLClient.executeGraphQL(GraphQLRequest(
                query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                variables = variablesPay
            ))

            if (payRes.errors != null) {
                _errorBannerMessage.value = payRes.errors.first().message
            } else {
                val variablesBook = mapOf(
                    "title" to "Dinner Catering: $title",
                    "subtitle" to "Guests: $guests | Menu: $entree, $sides, $dessert",
                    "price" to totalPrice,
                    "dateOrTime" to "Contact: $contactPhone (#$eventNumber)",
                    "type" to "EVENT"
                )
                val bookRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation BookService(\$title: String!, \$subtitle: String!, \$price: Float!, \$dateOrTime: String!, \$type: String!) { book(title: \$title, subtitle: \$subtitle, price: \$price, dateOrTime: \$dateOrTime, type: \$type) { success } }",
                    variables = variablesBook
                ))

                if (bookRes.errors != null) {
                    _errorBannerMessage.value = bookRes.errors.first().message
                } else {
                    _successBannerMessage.value = "Carly's Catering Service secured reservation for $title! Pricing of ₦${String.format(java.util.Locale.US, "%,.2f", totalPrice)} processed. 🎪🍽️💜"
                }
            }
            _isGraphQLFetching.value = false
        }
    }

    fun reserveRestaurantTable(restaurant: Restaurant, bookingTime: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            val price = restaurant.tablePrice
            
            // Pre-transaction sufficient funds validation check
            val currentBalance = walletBalance.value
            if (currentBalance < price) {
                val errMsg = "Insufficient Wallet Balance! Current: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Total due: ₦${String.format(java.util.Locale.US, "%,.2f", price)}. Restaurant booking rejected."
                android.util.Log.e("YangaMarketBilling", "TRANSACTION REJECTED: $errMsg")
                _errorBannerMessage.value = errMsg
                _isGraphQLFetching.value = false
                return@launch
            }

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

    // --- SERVICES MARKETPLACE BUSINESS MUTATIONS ---

    fun hireFreelancer(
        freelancerId: String,
        selectedService: String,
        amount: Double,
        milestoneTitles: List<String>,
        milestoneAmounts: List<Double>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val variables = mapOf(
                    "freelancerId" to freelancerId,
                    "selectedService" to selectedService,
                    "amount" to amount,
                    "milestoneTitles" to milestoneTitles,
                    "milestoneAmounts" to milestoneAmounts
                )
                val res = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation HireFreelancer(\$freelancerId: String!, \$selectedService: String!, \$amount: Float!, \$milestoneTitles: [String!]!, \$milestoneAmounts: [Float!]!) { hire(freelancerId: \$freelancerId, selectedService: \$selectedService, amount: \$amount, milestoneTitles: \$milestoneTitles, milestoneAmounts: \$milestoneAmounts) { id } }",
                    variables = variables
                ))

                if (res.errors != null) {
                    _errorBannerMessage.value = res.errors.first().message
                } else {
                    _successBannerMessage.value = "Escrow Deposit Secured! You have successfully hired support for '$selectedService'. Funding locked in vault. 🔓🔒"
                    onSuccess()
                    
                    // Force refresh listing catalogs to update Escrow bookings flow
                    refreshGraphQLCatalogs()
                }
            } catch (e: Exception) {
                _errorBannerMessage.value = "Hiring Escrow Lock Failed: ${e.message}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    fun approveMilestone(bookingId: String, milestoneId: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val variables = mapOf("bookingId" to bookingId, "milestoneId" to milestoneId)
                val res = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation ApproveMilestone(\$bookingId: String!, \$milestoneId: String!) { approve(bookingId: \$bookingId, milestoneId: \$milestoneId) { success } }",
                    variables = variables
                ))

                if (res.errors != null) {
                    _errorBannerMessage.value = res.errors.first().message
                } else {
                    addSilverCoins(5, "Completed freelancer milestone")
                    _successBannerMessage.value = "Milestone Approved! Escrow funds cleared and disbursed. Plus you earned 5 Silver Pieces for freelancing status! 🤝💸🪙"
                    refreshGraphQLCatalogs()
                }
            } catch (e: Exception) {
                _errorBannerMessage.value = "Milestone Approval Failed: ${e.message}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    fun submitMilestoneForReview(bookingId: String, milestoneId: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val variables = mapOf("bookingId" to bookingId, "milestoneId" to milestoneId)
                val res = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation SubmitMilestoneForReview(\$bookingId: String!, \$milestoneId: String!) { submit(bookingId: \$bookingId, milestoneId: \$milestoneId) { success } }",
                    variables = variables
                ))

                if (res.errors != null) {
                    _errorBannerMessage.value = res.errors.first().message
                } else {
                    _successBannerMessage.value = "Milestone work submitted to client. Pending review & disbursement! 📈🛡️"
                    refreshGraphQLCatalogs()
                }
            } catch (e: Exception) {
                _errorBannerMessage.value = "Milestone Submission Failed: ${e.message}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    fun submitFreelancerReview(freelancerId: String, reviewerName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val variables = mapOf(
                    "freelancerId" to freelancerId,
                    "reviewerName" to reviewerName,
                    "rating" to rating,
                    "comment" to comment
                )
                val res = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation SubmitReview(\$freelancerId: String!, \$reviewerName: String!, \$rating: Int!, \$comment: String!) { review(freelancerId: \$freelancerId, reviewerName: \$reviewerName, rating: \$rating, comment: \$comment) { success } }",
                    variables = variables
                ))

                if (res.errors != null) {
                    _errorBannerMessage.value = res.errors.first().message
                } else {
                    var bonusMsg = ""
                    if (rating == 5) {
                        addSilverCoins(5, "Received a 5-star review")
                        bonusMsg = " Plus you earned 5 Silver Pieces! 🪙"
                    }
                    _successBannerMessage.value = "Thank you! Your verified rating of $rating Stars has been cataloged. ⭐$bonusMsg"
                    refreshGraphQLCatalogs()
                }
            } catch (e: Exception) {
                _errorBannerMessage.value = "Posting Review Failed: ${e.message}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    fun purchaseBulkBuyBox(boxName: String, price: Double, itemsSummary: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isGraphQLFetching.value = true
            try {
                val currentBalance = walletBalance.value
                if (currentBalance < price) {
                    val errMsg = "Insufficient Wallet Balance! Current: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Box price: ₦${String.format(java.util.Locale.US, "%,.2f", price)}. Please fund wallet first."
                    _errorBannerMessage.value = errMsg
                    return@launch
                }

                val variablesPay = mapOf(
                    "amount" to price,
                    "note" to "Bulk Buy Box: $boxName"
                )
                val payRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation PayWithWallet(\$amount: Float!, \$note: String!) { pay(amount: \$amount, note: \$note) { success } }",
                    variables = variablesPay
                ))
                if (payRes.errors != null) {
                    _errorBannerMessage.value = payRes.errors.first().message
                    return@launch
                }

                val variablesBook = mapOf(
                    "title" to "Bulk Box: $boxName",
                    "subtitle" to "Items: $itemsSummary",
                    "price" to price,
                    "dateOrTime" to "Same-day delivery",
                    "type" to "MARKET"
                )
                val bookRes = graphQLClient.executeGraphQL(GraphQLRequest(
                    query = "mutation BookService(\$title: String!, \$subtitle: String!, \$price: Float!, \$dateOrTime: String!, \$type: String!) { book(title: \$title, subtitle: \$subtitle, price: \$price, dateOrTime: \$dateOrTime, type: \$type) { success } }",
                    variables = variablesBook
                ))

                if (bookRes.errors == null) {
                    _successBannerMessage.value = "Ordered '$boxName' successfully! Same-day dispatch has been scheduled! 🧺📦"
                    passportManager.addAppointmentNotification(
                        bookingId = "bulk-${System.currentTimeMillis()}",
                        title = "Bulk Buy: $boxName",
                        description = itemsSummary,
                        time = "Today"
                    )

                    // Post a vibe message to public board
                    val user = if (userName.value.trim().isNotEmpty()) userName.value.trim() else "Yanga citizen"
                    val bibeContent = "🎒 Freshly purchased the '$boxName' (₦${String.format(java.util.Locale.US, "%,.0f", price)}) for bulk share and vibes! Items inside: $itemsSummary. Who is splitting? 🍉🍊📦"
                    val variablesVibe = mapOf("author" to user, "content" to bibeContent)
                    graphQLClient.executeGraphQL(GraphQLRequest(
                        query = "mutation PostVibe(\$author: String!, \$content: String!) { addVibe(author: \$author, content: \$content) { id } }",
                        variables = variablesVibe
                    ))

                    onSuccess()
                } else {
                    _errorBannerMessage.value = bookRes.errors.first().message
                }
            } catch (e: Exception) {
                _errorBannerMessage.value = "Transaction failed: ${e.message}"
            } finally {
                _isGraphQLFetching.value = false
            }
        }
    }

    // --- 6. SUPERAPP REGISTRATIONS & ADMIN APPROVAL STATE FLOWS ---
    private val _pendingEvents = MutableStateFlow<List<EventApplication>>(emptyList())
    val pendingEvents = _pendingEvents.asStateFlow()

    private val _pendingFreelancers = MutableStateFlow<List<FreelancerApplication>>(emptyList())
    val pendingFreelancers = _pendingFreelancers.asStateFlow()

    private val _pendingBusinesses = MutableStateFlow<List<BusinessApplication>>(emptyList())
    val pendingBusinesses = _pendingBusinesses.asStateFlow()

    // Statuses of our own applications
    private val _myFreelancerAppStatus = MutableStateFlow(prefs.getString("my_freelancer_app_status", "") ?: "") // "", "Pending", "Approved"
    val myFreelancerAppStatus = _myFreelancerAppStatus.asStateFlow()

    private val _myBusinessAppStatus = MutableStateFlow(prefs.getString("my_business_app_status", "") ?: "") // "", "Pending", "Approved"
    val myBusinessAppStatus = _myBusinessAppStatus.asStateFlow()

    // Approved Profile Data
    private val _myFreelancerProfileId = MutableStateFlow(prefs.getString("my_freelancer_profile_id", "") ?: "")
    val myFreelancerProfileId = _myFreelancerProfileId.asStateFlow()

    private val _myBusinessProfileId = MutableStateFlow(prefs.getString("my_business_profile_id", "") ?: "")
    val myBusinessProfileId = _myBusinessProfileId.asStateFlow()

    private val _myBusinessCategory = MutableStateFlow(prefs.getString("my_business_category", "") ?: "")
    val myBusinessCategory = _myBusinessCategory.asStateFlow()

    private val _myBusinessName = MutableStateFlow(prefs.getString("my_biz_name", "") ?: "")
    val myBusinessName = _myBusinessName.asStateFlow()

    fun initRegistrations() {
        _pendingFreelancers.value = listOf(
            FreelancerApplication(
                id = "pending-free-1",
                name = "Adebayo Salami",
                title = "UI/UX & Product Designer",
                linkedinUrl = "https://linkedin.com/in/adebayo-ui",
                githubUrl = "https://github.com/adebayoui",
                backPhotoUrl = "🌆 Lagos Sunset",
                normalPhotoUrl = "👦 Playful Avatar",
                skills = "Figma, Material 3, Prototyping, Design Systems",
                bio = "Crafting user-centered interfaces for mobile products across West Africa.",
                basePrice = 8000.0,
                category = "Creative arts"
            )
        )
        _pendingBusinesses.value = listOf(
            BusinessApplication(
                id = "pending-biz-1",
                name = "Yaba Wellness Center",
                category = "Care Center",
                cacNumber = "RC-994812",
                location = "32 Herbert Macaulay Way, Yaba",
                imageDescription = "🏥 Modern Care Building",
                services = "Post-Natal Support, Pediatric Physical Therapy, Elderly Assisted Care"
            )
        )
        _pendingEvents.value = listOf(
            EventApplication(
                id = "pending-evt-1",
                title = "Yaba Jollof & Grill Festival",
                description = "Celebrate the finest Jollof Rice styles in Lagos with live music, pepper challenges, and cold drinks!",
                isFree = true,
                couponCode = "JOLLOFVIBES",
                host = "Yaba Foodies Cohort",
                date = "July 15, 2026",
                time = "14:00",
                venue = "Yaba College of Technology Ground",
                imageDescription = "🍛 Spicy Jollof Bowl"
            )
        )
    }

    init {
        // Fetch static catalogs using GraphQL Query abstractions
        refreshGraphQLCatalogs()
        // Pre-seed default vibes community posts for immersive layout
        preseedVibesIfEmpty()
        initRegistrations()

        // Hook Real-time Web Socket threads listeners
        webSocketService.safeEmitter.onCreatedThread { thread ->
            _activeThreads.value = _activeThreads.value + thread
        }
        webSocketService.safeEmitter.onAddedUserToThread { userJoin ->
            _registeredThreadUsers.value = _registeredThreadUsers.value + userJoin
        }
    }

    // --- Freelancer Actions ---
    fun submitFreelancerApplication(
        name: String,
        title: String,
        linkedinUrl: String,
        githubUrl: String,
        backPhotoUrl: String,
        normalPhotoUrl: String,
        skills: String,
        bio: String,
        basePrice: Double,
        category: String
    ) {
        val app = FreelancerApplication(
            name = name,
            title = title,
            linkedinUrl = linkedinUrl,
            githubUrl = githubUrl,
            backPhotoUrl = backPhotoUrl,
            normalPhotoUrl = normalPhotoUrl,
            skills = skills,
            bio = bio,
            basePrice = basePrice,
            category = category
        )
        _pendingFreelancers.value = _pendingFreelancers.value + app
        _myFreelancerAppStatus.value = "Pending"
        _myFreelancerProfileId.value = app.id
        prefs.edit()
            .putString("my_freelancer_app_status", "Pending")
            .putString("my_freelancer_profile_id", app.id)
            .putString("my_free_name", name)
            .putString("my_free_title", title)
            .putString("my_free_linkedin", linkedinUrl)
            .putString("my_free_github", githubUrl)
            .putString("my_free_back", backPhotoUrl)
            .putString("my_free_normal", normalPhotoUrl)
            .putString("my_free_skills", skills)
            .putString("my_free_bio", bio)
            .putFloat("my_free_price", basePrice.toFloat())
            .putString("my_free_category", category)
            .apply()

        addNotification(
            title = "Freelancer Application Submitted! 👦📝",
            message = "Your talent application as a $title has been received. Our administrators are vetting your credentials.",
            type = "SYSTEM",
            icon = "📝"
        )
        _successBannerMessage.value = "Talent application submitted successfully! Under admin review. ⏱️"
    }

    // --- Business Actions ---
    fun submitBusinessApplication(
        name: String,
        category: String,
        cacNumber: String,
        location: String,
        imageDescription: String,
        services: String
    ) {
        val app = BusinessApplication(
            name = name,
            category = category,
            cacNumber = cacNumber,
            location = location,
            imageDescription = imageDescription,
            services = services
        )
        _pendingBusinesses.value = _pendingBusinesses.value + app
        _myBusinessAppStatus.value = "Pending"
        _myBusinessProfileId.value = app.id
        _myBusinessCategory.value = category
        _myBusinessName.value = name
        prefs.edit()
            .putString("my_business_app_status", "Pending")
            .putString("my_business_profile_id", app.id)
            .putString("my_business_category", category)
            .putString("my_biz_name", name)
            .putString("my_biz_cac", cacNumber)
            .putString("my_biz_loc", location)
            .putString("my_biz_img", imageDescription)
            .putString("my_biz_services", services)
            .apply()

        addNotification(
            title = "Business Application Submitted! 🏢📝",
            message = "Your business application for '$name' ($category) has been successfully recorded. Under admin verification.",
            type = "SYSTEM",
            icon = "📝"
        )
        _successBannerMessage.value = "Business application submitted successfully! Under admin review. ⏱️"
    }

    // --- Event Actions ---
    fun submitEventApplication(
        title: String,
        description: String,
        isFree: Boolean,
        couponCode: String,
        host: String,
        date: String,
        time: String,
        venue: String,
        imageDescription: String
    ) {
        val app = EventApplication(
            title = title,
            description = description,
            isFree = isFree,
            couponCode = couponCode,
            host = host,
            date = date,
            time = time,
            venue = venue,
            imageDescription = imageDescription
        )
        _pendingEvents.value = _pendingEvents.value + app
        addNotification(
            title = "Event Submission Received! 🎟️✨",
            message = "Your event '$title' has been submitted for verification. It will appear on the boards once approved.",
            type = "SYSTEM",
            icon = "🎟️"
        )
        _successBannerMessage.value = "Event submitted successfully! Awaiting admin review. ⏱️"
    }

    // --- Moderators State Flow ---
    private val _moderators = MutableStateFlow<Set<String>>(prefs.getStringSet("yanga_moderators", emptySet()) ?: emptySet())
    val moderators: StateFlow<Set<String>> = _moderators.asStateFlow()

    fun addModerator(email: String) {
        val current = _moderators.value.toMutableSet()
        val emailClean = email.lowercase().trim()
        if (emailClean.isNotBlank()) {
            current.add(emailClean)
            _moderators.value = current
            prefs.edit().putStringSet("yanga_moderators", current).apply()
            _successBannerMessage.value = "Moderator $emailClean added successfully! 👑"
        }
    }

    fun removeModerator(email: String) {
        val current = _moderators.value.toMutableSet()
        val emailClean = email.lowercase().trim()
        if (current.remove(emailClean)) {
            _moderators.value = current
            prefs.edit().putStringSet("yanga_moderators", current).apply()
            _successBannerMessage.value = "Moderator $emailClean removed successfully! 🗑️"
        }
    }

    fun rejectFreelancer(appId: String) {
        _pendingFreelancers.value = _pendingFreelancers.value.filter { it.id != appId }
        _successBannerMessage.value = "Freelancer application rejected/dismissed. ❌"
    }

    fun rejectBusiness(appId: String) {
        _pendingBusinesses.value = _pendingBusinesses.value.filter { it.id != appId }
        _successBannerMessage.value = "Business application rejected/dismissed. ❌"
    }

    fun rejectEvent(appId: String) {
        _pendingEvents.value = _pendingEvents.value.filter { it.id != appId }
        _successBannerMessage.value = "Event application rejected/dismissed. ❌"
    }

    fun addMarketInsight(author: String, text: String, category: String) {
        val id = "Q-${(10..99).random()}"
        val newQuote = com.example.domain.model.BusinessQuote(id, author, text, category)
        graphQLClient.quotesCatalog.add(newQuote)
        refreshGraphQLCatalogs()
        _successBannerMessage.value = "Market Insight / Vibe added successfully! 💡"
    }

    // --- Admin Action Methods ---
    fun approveFreelancer(appId: String) {
        val app = _pendingFreelancers.value.find { it.id == appId }
        if (app != null) {
            _pendingFreelancers.value = _pendingFreelancers.value.filter { it.id != appId }
            
            val newProfile = FreelancerProfile(
                id = app.id,
                name = app.name,
                title = app.title,
                avatarEmoji = if (app.normalPhotoUrl.contains("👦") || app.normalPhotoUrl.contains("avatar")) "👦" else "👤",
                rating = 5.0,
                basePrice = app.basePrice,
                bio = app.bio,
                category = app.category,
                portfolioGallery = listOf("Background: " + app.backPhotoUrl, "My First Masterpiece 🎨"),
                serviceListings = app.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { "Consulting: $it" },
                linkedinUrl = app.linkedinUrl,
                githubUrl = app.githubUrl,
                skills = app.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            )
            graphQLClient.freelancerCatalog.add(newProfile)

            if (appId == _myFreelancerProfileId.value) {
                _myFreelancerAppStatus.value = "Approved"
                prefs.edit().putString("my_freelancer_app_status", "Approved").apply()
                
                addNotification(
                    title = "Freelancer Profile Approved! 🎉👦",
                    message = "Congratulations! Your professional freelancer profile as a ${app.title} is now LIVE on the Services Marketplace.",
                    type = "SYSTEM",
                    icon = "🎉"
                )
            } else {
                addNotification(
                    title = "New Freelancer Approved! 👦🌟",
                    message = "${app.name} (${app.title}) is now available for hire on Yanga Market!",
                    type = "SYSTEM",
                    icon = "🌟"
                )
            }
            refreshGraphQLCatalogs()
            _successBannerMessage.value = "Freelancer application approved successfully! Profile is now active."
        }
    }

    fun approveBusiness(appId: String) {
        val app = _pendingBusinesses.value.find { it.id == appId }
        if (app != null) {
            _pendingBusinesses.value = _pendingBusinesses.value.filter { it.id != appId }

            val isHospitalType = app.category == "Hospital" || app.category == "Care Center" || app.category == "Pharmacy"
            val isFoodType = app.category == "Restaurant" || app.category == "Bakery" || app.category == "Confectionery"
            val isRetailType = app.category == "Retail"

            if (isHospitalType) {
                val newHosp = Hospital(
                    id = app.id,
                    name = app.name,
                    location = app.location,
                    distanceKm = 1.0,
                    specialties = app.services.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    openHours = "24/7"
                )
                graphQLClient.directoryService.addHospital(newHosp)
            } else if (isFoodType) {
                val newRest = Restaurant(
                    id = app.id,
                    name = app.name,
                    cuisine = app.category,
                    rating = 5.0,
                    address = app.location,
                    meals = app.services.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { 
                        FoodItem(name = it, price = 2500.0, category = app.category, description = "Delicious freshly prepared course offered by ${app.name}.")
                    }
                )
                graphQLClient.restaurantCatalog.add(newRest)
            } else if (isRetailType) {
                val newShop = RetailShop(
                    id = app.id,
                    nameAndAddress = NameAndAddress(app.name, app.location),
                    specialty = "General Retail",
                    distanceKm = 1.0,
                    items = app.services.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map {
                        RetailItem(name = it, price = 5000.0, category = "General")
                    }
                )
                graphQLClient.retailShopsCatalog.add(newShop)
            }

            if (appId == _myBusinessProfileId.value) {
                _myBusinessAppStatus.value = "Approved"
                prefs.edit().putString("my_business_app_status", "Approved").apply()
                
                addNotification(
                    title = "Business Profile Approved! 🎉🏢",
                    message = "Congratulations! Your business '${app.name}' is now APPROVED and live on Yanga Market. Access your merchant profile under Settings.",
                    type = "SYSTEM",
                    icon = "🎉"
                )
            } else {
                addNotification(
                    title = "New Business Registered! 🏢🌟",
                    message = "'${app.name}' (${app.category}) is now open for patrons on Yanga Market!",
                    type = "SYSTEM",
                    icon = "🌟"
                )
            }
            refreshGraphQLCatalogs()
            _successBannerMessage.value = "Business application approved! Added to directory successfully."
        }
    }

    fun approveEvent(appId: String) {
        val app = _pendingEvents.value.find { it.id == appId }
        if (app != null) {
            _pendingEvents.value = _pendingEvents.value.filter { it.id != appId }

            val newEvent = Event(
                id = app.id,
                title = app.title,
                host = app.host,
                date = app.date,
                time = app.time,
                venue = app.venue,
                rsvpCount = 0,
                isRsvped = false,
                price = if (app.isFree) 0.0 else 2500.0,
                details = app.description,
                hasFood = true,
                hasCompetition = false,
                imageResName = "img_event_festival_1782134258914"
            )
            graphQLClient.eventCatalog.add(newEvent)

            addNotification(
                title = "New Event Approved! 🎟️🎉",
                message = "The event '${app.title}' has been verified and is now live. Reserve your secure passes today!",
                type = "SYSTEM",
                icon = "🎉"
            )
            refreshGraphQLCatalogs()
            _successBannerMessage.value = "Event approved successfully! Published to the Yanga Events board."
        }
    }

    // --- Profile Editing / Portfolios / Menus Update ---
    fun updateFreelancerSkillsAndPortfolio(additionalSkills: String, portfolioItemTitle: String) {
        val currentId = _myFreelancerProfileId.value
        if (currentId.isNotEmpty()) {
            val profile = graphQLClient.freelancerCatalog.find { it.id == currentId }
            if (profile != null) {
                graphQLClient.freelancerCatalog.remove(profile)

                val updatedSkills = if (additionalSkills.isNotEmpty()) {
                    profile.skills + additionalSkills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    profile.skills
                }

                val updatedGallery = if (portfolioItemTitle.isNotEmpty()) {
                    profile.portfolioGallery + portfolioItemTitle
                } else {
                    profile.portfolioGallery
                }

                val updatedProfile = profile.copy(
                    skills = updatedSkills,
                    portfolioGallery = updatedGallery,
                    serviceListings = updatedSkills.map { "Premium Service: $it" }
                )
                graphQLClient.freelancerCatalog.add(updatedProfile)

                val savedSkills = updatedSkills.joinToString(",")
                prefs.edit().putString("my_free_skills", savedSkills).apply()

                _successBannerMessage.value = "Freelancer portfolio and skills updated successfully! 🚀"
                refreshGraphQLCatalogs()
            }
        }
    }

    fun addBusinessItem(itemName: String, itemDescription: String, itemPrice: Double) {
        val currentId = _myBusinessProfileId.value
        val category = _myBusinessCategory.value
        if (currentId.isNotEmpty()) {
            val isHospitalType = category == "Hospital" || category == "Care Center" || category == "Pharmacy"
            val isFoodType = category == "Restaurant" || category == "Bakery" || category == "Confectionery"
            val isRetailType = category == "Retail"

            if (isHospitalType) {
                val hosp = graphQLClient.directoryService.getAllHospitals().find { it.id == currentId }
                if (hosp != null) {
                    val updatedSpecialties = hosp.specialties + itemName
                    graphQLClient.directoryService.removeHospital(currentId)
                    val newHosp = Hospital(
                        id = hosp.id,
                        name = hosp.name,
                        location = hosp.location,
                        distanceKm = hosp.distanceKm,
                        specialties = updatedSpecialties,
                        openHours = hosp.openHours
                    )
                    graphQLClient.directoryService.addHospital(newHosp)
                    _successBannerMessage.value = "Health care test/service '$itemName' added successfully! 🩺"
                }
            } else if (isFoodType) {
                val rest = graphQLClient.restaurantCatalog.find { it.id == currentId }
                if (rest != null) {
                    graphQLClient.restaurantCatalog.remove(rest)
                    val newMeal = FoodItem(
                        name = itemName,
                        price = itemPrice,
                        category = category,
                        description = itemDescription
                    )
                    val updatedMeals = rest.meals + newMeal
                    val newRest = Restaurant(
                        id = rest.id,
                        name = rest.name,
                        cuisine = rest.cuisine,
                        rating = rest.rating,
                        address = rest.address,
                        meals = updatedMeals,
                        distanceKm = rest.distanceKm,
                        isTableReserved = rest.isTableReserved,
                        hasTableBooking = rest.hasTableBooking
                    )
                    graphQLClient.restaurantCatalog.add(newRest)
                    _successBannerMessage.value = "New menu course '$itemName' added successfully! 🍔"
                }
            } else if (isRetailType) {
                val shop = graphQLClient.retailShopsCatalog.find { it.id == currentId }
                if (shop != null) {
                    graphQLClient.retailShopsCatalog.remove(shop)
                    val newItem = RetailItem(
                        name = itemName,
                        price = itemPrice,
                        category = "Special"
                    )
                    val updatedItems = shop.items + newItem
                    val newShop = RetailShop(
                        id = shop.id,
                        nameAndAddress = shop.nameAndAddress,
                        specialty = shop.specialty,
                        distanceKm = shop.distanceKm,
                        items = updatedItems
                    )
                    graphQLClient.retailShopsCatalog.add(newShop)
                    _successBannerMessage.value = "Store item '$itemName' successfully listed in retail shop! 🛒"
                }
            }
            refreshGraphQLCatalogs()
        }
    }
}

data class EventApplication(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isFree: Boolean,
    val couponCode: String,
    val host: String,
    val date: String,
    val time: String,
    val venue: String,
    val imageDescription: String,
    val status: String = "Pending"
)

data class FreelancerApplication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val title: String,
    val linkedinUrl: String,
    val githubUrl: String,
    val backPhotoUrl: String,
    val normalPhotoUrl: String,
    val skills: String,
    val bio: String,
    val basePrice: Double,
    val category: String,
    val status: String = "Pending"
)

data class BusinessApplication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val cacNumber: String,
    val location: String,
    val imageDescription: String,
    val services: String,
    val status: String = "Pending"
)

data class YangaComplaint(
    val id: String,
    val category: String,
    val title: String,
    val details: String,
    val timestamp: String,
    val status: String
)

data class YangaNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val icon: String, // Emoji representation
    val isRead: Boolean = false,
    val type: String // "BONUS", "TRANSACTION", "DRAW", "SYSTEM"
)
