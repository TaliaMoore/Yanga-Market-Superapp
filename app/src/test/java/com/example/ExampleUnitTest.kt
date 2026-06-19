package com.example

import com.example.domain.model.Event
import com.example.domain.model.DinnerEvent
import com.example.domain.model.FoodItem
import com.example.domain.model.RetailItem
import com.example.domain.model.Restaurant
import com.example.domain.model.Hospital
import com.example.domain.model.RetailShop
import com.example.domain.model.NameAndAddress
import com.example.domain.model.Customer
import com.example.domain.model.WalletState
import com.example.domain.model.Employee
import com.example.domain.model.OrderSystem
import com.example.domain.model.LunchOrder
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.Fruit
import com.example.domain.model.Meal
import com.example.domain.model.HospitalDirectoryService
import com.example.domain.model.DnsSrvRecord
import com.example.domain.model.ServiceDiscoveryRegistry
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Local unit tests to ensure business logic accuracy across entities.
 */
class ExampleUnitTest {
  
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFoodItemAccessorsAndMutators() {
    val burger = FoodItem(
      name = "Suya Spiced Beef Burger",
      price = 1900.0,
      category = "Burgers",
      description = "Flame-grilled suya burger"
    )

    // Accessor
    assertEquals(1900.0, burger.fetchPrice(), 0.001)

    // Valid mutator call
    burger.updatePrice(2500.0)
    assertEquals(2500.0, burger.fetchPrice(), 0.001)

    // Invalid mutator boundary test (should be ignored)
    burger.updatePrice(-500.0)
    assertEquals(2500.0, burger.fetchPrice(), 0.001)
  }

  @Test
  fun testRetailItemAccessorsAndMutators() {
    val shopItem = RetailItem(
      name = "Ankara Playful Summer Dress",
      price = 4500.0,
      category = "Clothing"
    )

    // Accessor
    assertEquals(4500.0, shopItem.fetchPrice(), 0.001)

    // Valid mutator call
    shopItem.updatePrice(4900.0)
    assertEquals(4900.0, shopItem.fetchPrice(), 0.001)

    // Invalid mutator boundary test (should keep old price)
    shopItem.updatePrice(-100.0)
    assertEquals(4900.0, shopItem.fetchPrice(), 0.001)
  }

  @Test
  fun testRestaurantAccessorsAndMutators() {
    val biniRestaurant = Restaurant(
      name = "Bini Culinary Haven",
      cuisine = "Nigerian Traditional",
      rating = 4.8,
      address = "12 Oba Market Rd, Benin",
      hasTableBooking = true,
      tablePrice = 1500.0
    )

    // Accessor
    assertEquals(1500.0, biniRestaurant.fetchTablePrice(), 0.001)

    // Valid mutator call
    biniRestaurant.updateTablePrice(2000.0)
    assertEquals(2000.0, biniRestaurant.fetchTablePrice(), 0.001)

    // Invalid mutator test (should be ignored)
    biniRestaurant.updateTablePrice(-1500.0)
    assertEquals(2000.0, biniRestaurant.fetchTablePrice(), 0.001)
  }

  @Test
  fun testHospitalAccessorsAndMutators() {
    val lagosHospital = Hospital(
      name = "Eko Diagnostic & Specialist",
      location = "Ikoyi, Lagos",
      distanceKm = 2.4,
      specialties = listOf("Cardiology", "Paediatrics"),
      openHours = "08:00 - 22:00"
    )

    // Accessors
    assertEquals("Eko Diagnostic & Specialist", lagosHospital.fetchName())
    assertEquals("08:00 - 22:00", lagosHospital.fetchOpenHours())

    // Valid mutator call
    lagosHospital.updateOpenHours("24/7")
    assertEquals("24/7", lagosHospital.fetchOpenHours())

    // Invalid blank mutator test (should be ignored)
    lagosHospital.updateOpenHours("")
    assertEquals("24/7", lagosHospital.fetchOpenHours())
  }

  @Test
  fun testHospitalDirectoryService() {
    val jsonString = """
      [
        {
          "name": "St. Nicholas Premium Hospital",
          "address": "Campus Square, Lagos Island",
          "distanceKm": 1.1,
          "specialties": ["General Wellness", "Pediatrics", "Cardiology"],
          "openHours": "24/7"
        },
        {
          "name": "Evercare Hospital Lekki",
          "address": "Lekki Phase 1, Lagos",
          "distanceKm": 4.8,
          "specialties": ["MRI & Lab Radiography", "Immunization", "Ophthalmology"],
          "openHours": "12:00 - 20:00"
        },
        {
          "name": "Ikeja Medical Center",
          "address": "11 Toyin St, Ikeja",
          "distanceKm": 7.5,
          "specialties": ["General Consultation", "Dentistry"],
          "openHours": "24/7"
        }
      ]
    """.trimIndent()

    val directory = HospitalDirectoryService()
    directory.loadHospitals(jsonString)

    val all = directory.getAllHospitals()
    assertEquals(3, all.size)

    // Verify first entry details
    val stNick = all[0]
    assertEquals("St. Nicholas Premium Hospital", stNick.fetchName())
    assertEquals("Campus Square, Lagos Island", stNick.fetchLocation())
    assertEquals(1.1, stNick.distanceKm, 0.001)
    assertEquals(listOf("General Wellness", "Pediatrics", "Cardiology"), stNick.specialties)
    assertEquals("24/7", stNick.fetchOpenHours())

    // 1. Search by Hospital Name (Case-insensitive)
    val nameSearch = directory.searchByName("evercare")
    assertEquals(1, nameSearch.size)
    assertEquals("Evercare Hospital Lekki", nameSearch[0].fetchName())

    // 2. Search by Location/Address
    val locationSearch = directory.searchByLocation("ikeja")
    assertEquals(1, locationSearch.size)
    assertEquals("Ikeja Medical Center", locationSearch[0].fetchName())

    // 3. Search by Services/Specialties
    val serviceSearch = directory.searchByServices("pediatric")
    assertEquals(1, serviceSearch.size)
    assertEquals("St. Nicholas Premium Hospital", serviceSearch[0].fetchName())

    val serviceSearch2 = directory.searchByServices("General")
    assertEquals(2, serviceSearch2.size) // St Nicholas and Ikeja Medical Center

    // 4. Multi-field lookup combines searches
    val querySearch1 = directory.lookup("Lagos Island") // matches location
    assertEquals(1, querySearch1.size)
    assertEquals("St. Nicholas Premium Hospital", querySearch1[0].fetchName())

    val querySearch2 = directory.lookup("Dentistry") // matches specialty
    assertEquals(1, querySearch2.size)
    assertEquals("Ikeja Medical Center", querySearch2[0].fetchName())

    val emptySearch = directory.lookup("") // empty query returns all entries
    assertEquals(3, emptySearch.size)
  }
  
  @Test
  fun testServiceDiscoveryAndSrvDynamicResolution() {
    val registry = ServiceDiscoveryRegistry()

    // Create a set of container SRV records representing hospital microservices pods
    // Structure: service, protocol, domain, priority, weight, port, target, isHealthy
    val podMain = DnsSrvRecord(
        service = "hospital",
        protocol = "http",
        domain = "yanga.market",
        priority = 10,
        weight = 80,
        port = 8081,
        target = "hospital-pod-main.yanga.local",
        isHealthy = true
    )
    val podBackup = DnsSrvRecord(
        service = "hospital",
        protocol = "http",
        domain = "yanga.market",
        priority = 10,
        weight = 20,
        port = 8082,
        target = "hospital-pod-backup.yanga.local",
        isHealthy = true
    )
    val podFailover = DnsSrvRecord(
        service = "hospital",
        protocol = "http",
        domain = "yanga.market",
        priority = 20, // higher priority number = lower precedence fallback
        weight = 100,
        port = 8083,
        target = "hospital-pod-failover.yanga.local",
        isHealthy = true
    )

    // Register all instances
    registry.register(podMain)
    registry.register(podBackup)
    registry.register(podFailover)

    // Verify raw DNS string serialization
    assertEquals("_hospital._http.yanga.market. 3600 IN SRV 10 80 8081 hospital-pod-main.yanga.local", podMain.toDnsString())
    assertEquals("http://hospital-pod-main.yanga.local:8081", podMain.getAddressUrl())

    // 1. Resolve SRV entries. Should return records sorted by priority ascending, then weight descending.
    val resolvedList = registry.resolveSrv("hospital", "http")
    assertEquals(3, resolvedList.size)
    // First priority is 10 (podMain and podBackup) sorted with higher weight first (podMain weight 80 comes before podBackup weight 20)
    assertEquals(10, resolvedList[0].priority)
    assertEquals("hospital-pod-main.yanga.local", resolvedList[0].target)
    assertEquals(10, resolvedList[1].priority)
    assertEquals("hospital-pod-backup.yanga.local", resolvedList[1].target)
    // Third is priority 20
    assertEquals(20, resolvedList[2].priority)
    assertEquals("hospital-pod-failover.yanga.local", resolvedList[2].target)

    // 2. Discover endpoint selects automatically from highest priority group (priority = 10)
    val chosenEndpoint = registry.discoverEndpoint("hospital", "http")
    assertNotNull(chosenEndpoint)
    assertTrue(chosenEndpoint!!.target == "hospital-pod-main.yanga.local" || chosenEndpoint.target == "hospital-pod-backup.yanga.local")

    // 3. Mark podMain as unhealthy (down for maintenance/restart)
    registry.updateHealth("hospital-pod-main.yanga.local", 8081, isHealthy = false)

    // Resolve now should skip podMain
    val resolvedListPostFailure = registry.resolveSrv("hospital", "http")
    assertEquals(2, resolvedListPostFailure.size)
    assertEquals("hospital-pod-backup.yanga.local", resolvedListPostFailure[0].target)
    assertEquals("hospital-pod-failover.yanga.local", resolvedListPostFailure[1].target)

    // Dynamic endpoint lookup should now yield podBackup (only remaining healthy priority 10 candidate)
    val endpointPostFailure = registry.discoverEndpoint("hospital", "http")
    assertNotNull(endpointPostFailure)
    assertEquals("hospital-pod-backup.yanga.local", endpointPostFailure!!.target)

    // 4. Mark podBackup as unhealthy as well (whole primary deployment unavailable)
    registry.updateHealth("hospital-pod-backup.yanga.local", 8082, isHealthy = false)

    // Dynamic endpoint lookup should fall back seamlessly to priority 20 group
    val endpointFallback = registry.discoverEndpoint("hospital", "http")
    assertNotNull(endpointFallback)
    assertEquals("hospital-pod-failover.yanga.local", endpointFallback!!.target)

    // 5. Unregister test
    registry.unregister("hospital-pod-failover.yanga.local", 8083)
    val emptyEndpoint = registry.discoverEndpoint("hospital", "http")
    assertNull(emptyEndpoint)
  }

  @Test
  fun testFacilityRent2DArrayInquiry() {
    val directory = com.example.domain.model.FacilityRentDirectory()
    val rawGrid = directory.fetchRawGrid()

    // Enforce 2D array coordinates verification (4 Floors x 3 Sections)
    assertEquals(4, rawGrid.size)
    assertEquals(3, rawGrid[0].size)

    // Check Ground floor section A details (Floor Index 0, Section Index 0)
    val gA = rawGrid[0][0]
    assertEquals(0, gA.floorIndex)
    assertEquals(0, gA.sectionIndex)
    assertEquals("F1-A", gA.locationCode)
    assertEquals(120000.0, gA.rentAmt, 0.001)
    // Formula: (floorIndex + sectionIndex) % 2 != 0
    // (0 + 0) % 2 != 0 -> 0 != 0 is False, so isAvailable is false
    assertFalse(gA.isAvailable)

    // Check Level 2 section B details (Floor Index 1, Section Index 1)
    val level2B = rawGrid[1][1]
    assertEquals(1, level2B.floorIndex)
    assertEquals(1, level2B.sectionIndex)
    assertEquals("F2-B", level2B.locationCode)
    // rentAmt = 120000.0 * floorLabel + (sectionIndex * 45000.0) -> 120000 * 2 + 1 * 45000 = 285000.0
    assertEquals(285000.0, level2B.rentAmt, 0.001)
    // (1 + 1) % 2 != 0 -> 2 % 2 != 0 is False, so isAvailable is false
    assertFalse(level2B.isAvailable)

    // Check Level 2 section C details (Floor Index 1, Section Index 2)
    val level2C = rawGrid[1][2]
    // (1 + 2) % 2 != 0 -> 3 % 2 != 0 is True, so isAvailable is true
    assertTrue(level2C.isAvailable)

    // Verify lookup by location code
    val foundByCode = directory.findByLocationCode("F2-B")
    assertNotNull(foundByCode)
    assertEquals("F2-B", foundByCode!!.locationCode)

    // Case insensitive/no-dash lookup
    val foundByNoDash = directory.findByLocationCode("f2b")
    assertNotNull(foundByNoDash)
    assertEquals("F2-B", foundByNoDash!!.locationCode)

    // Verify lookup by floor level number (1..4)
    val floor2Spaces = directory.findByFloorNumber(2)
    assertEquals(3, floor2Spaces.size)
    assertEquals("F2-A", floor2Spaces[0].locationCode)
    assertEquals("F2-B", floor2Spaces[1].locationCode)
    assertEquals("F2-C", floor2Spaces[2].locationCode)
  }

  @Test
  fun testCompositionNameAndAddress() {
    // 1. Create Address Component
    val addressComponent = NameAndAddress(
      nameVal = "Ikeja HighStreet Marketplace",
      addressVal = "Kona Block 12, Allen Avenue, Lagos"
    )

    // 2. Embed into Shop ('has-a' relationship)
    val shop = RetailShop(
      nameAndAddress = addressComponent,
      specialty = "Ankara Fabrics",
      distanceKm = 0.8,
      items = emptyList()
    )

    // Verify correct references and delegations
    assertEquals("Ikeja HighStreet Marketplace", shop.name)
    assertEquals("Kona Block 12, Allen Avenue, Lagos", shop.location)
    assertEquals(shop.nameAndAddress, addressComponent)

    // Verify information hiding and mutators on the composite object
    addressComponent.setName("Ikeja Premium Fabrics")
    addressComponent.setAddress("Suite 4, Toyin St, Ikeja")
    
    // Dynamic delegation asserts
    assertEquals("Ikeja Premium Fabrics", shop.name)
    assertEquals("Suite 4, Toyin St, Ikeja", shop.location)

    // Invalid mutator checks
    addressComponent.setName("") // blank should be ignored
    assertEquals("Ikeja Premium Fabrics", shop.name)
  }

  @Test
  fun testEventCarlysCateringLogic() {
    // 1. Instantiation with defaults
    val event = Event(
      title = "Carly's Annual Gala Banquet",
      host = "Carly's Premium Catering Service",
      date = "December 15, 2026",
      time = "19:00",
      venue = "Grand Royal Ballroom, Lagos"
    )

    // Verify correct parent class inheritance (SuperAppEntity)
    assertEquals("Carly's Annual Gala Banquet", event.fetchName())
    assertEquals("Grand Royal Ballroom, Lagos", event.fetchLocation())

    // Verify Carly's custom attributes
    assertTrue(event.getEventNumber().startsWith("E"))
    assertEquals(15, event.getNumberOfGuests()) // Default fallback
    assertEquals(15 * 35.0, event.getCalculatedPrice(), 0.001)
    assertFalse(event.isLargeEvent()) // 15 < 50

    // 2. Testing dynamic guest recalculation (Set Guests)
    event.setGuests(45)
    assertEquals(45, event.getNumberOfGuests())
    assertEquals(45 * 35.0, event.getCalculatedPrice(), 0.001)
    assertFalse(event.isLargeEvent()) // 45 < 50

    event.setGuests(75)
    assertEquals(75, event.getNumberOfGuests())
    assertEquals(75 * 32.0, event.getCalculatedPrice(), 0.001)
    assertTrue(event.isLargeEvent()) // 75 >= 50

    // 3. Testing custom event number setting and getter/setter validation
    event.setEventNumber("c123")
    assertEquals("C123", event.getEventNumber()) // auto-capitalized by Carly's policy
    
    // Testing negative boundary case
    event.setGuests(-5)
    assertEquals(75, event.getNumberOfGuests()) // Should ignore invalid negative values and retain 75

    // --- DinnerEvent Subclass Selection Logic Tests ---
    val dinner = DinnerEvent(
        title = "Executive Dinner Banquet",
        host = "Yanga Corporate",
        date = "2026-11-25",
        time = "19:00",
        venue = "Lekki Palace Hall",
        initialGuests = 60
    )

    // Check menu arrays are non-empty
    assertTrue(dinner.entrees.isNotEmpty())
    assertTrue(dinner.sideDishes.isNotEmpty())
    assertTrue(dinner.desserts.isNotEmpty())

    // Initial state: nothing selected
    assertNull(dinner.getSelectedEntree())
    assertEquals(0, dinner.getSelectedSides().size)
    assertNull(dinner.getSelectedDessert())
    assertFalse(dinner.isDinnerSelectionComplete())

    // Select entree
    assertTrue(dinner.selectEntree("Beef Suya Steak"))
    assertEquals("Beef Suya Steak", dinner.getSelectedEntree())
    assertFalse(dinner.selectEntree("Caviar and Golden Rice")) // Not in available list

    // Select side dishes (Should fail on 1 side or 3 sides, succeed on exactly 2 sides)
    assertFalse(dinner.selectSides(arrayOf("Jollof Rice"))) 
    assertFalse(dinner.selectSides(arrayOf("Jollof Rice", "Fried Plantain (Dodo)", "Yam Fries")))
    assertFalse(dinner.selectSides(arrayOf("Jollof Rice", "Macaroni Salad"))) // Macaroni salad doesn't exist
    
    assertTrue(dinner.selectSides(arrayOf("Jollof Rice", "Fried Plantain (Dodo)")))
    assertArrayEquals(arrayOf("Jollof Rice", "Fried Plantain (Dodo)"), dinner.getSelectedSides())

    // Select dessert
    assertTrue(dinner.selectDessert("Puff Puff with Ice Cream"))
    assertEquals("Puff Puff with Ice Cream", dinner.getSelectedDessert())
    assertFalse(dinner.selectDessert("Golden Gelato")) // Not in list

    // Selection now is complete and valid
    assertTrue(dinner.isDinnerSelectionComplete())

    // --- Phone Number Field, Formatting & Digit-Stripping Validation ---
    // Test default empty string
    val phoneEventDefault = Event(
        title = "Secret Gala Session",
        host = "Yanga Secret",
        date = "2026-07-01",
        time = "20:00",
        venue = "Main Hall"
    )
    assertEquals("", phoneEventDefault.getContactPhoneNumber())

    // Test constructor initialization formatting
    val phoneEventInitialized = Event(
        title = "Gala Banquet Party",
        host = "Yanga Party",
        date = "2026-07-02",
        time = "18:00",
        venue = "Vantage Hall",
        initialContactPhone = "9208729182"
    )
    assertEquals("(920) 872-9182", phoneEventInitialized.getContactPhoneNumber())

    // Test setter stripping non-digits
    phoneEventInitialized.setContactPhoneNumber("920-ABC--872-xyz-9182")
    assertEquals("(920) 872-9182", phoneEventInitialized.getContactPhoneNumber())

    // Test non-10-digit raw output fallback
    phoneEventInitialized.setContactPhoneNumber("123-456")
    assertEquals("123456", phoneEventInitialized.getContactPhoneNumber())

    // --- Overloaded Constructors Validation ---
    // 1. Default constructor
    val defaultEvent = Event()
    assertEquals("A000", defaultEvent.getEventNumber())
    assertEquals(0, defaultEvent.getNumberOfGuests())
    assertEquals(0.0, defaultEvent.getCalculatedPrice(), 0.001)

    // 2. Designated constructor (requires specific event number and guest count)
    val designatedEventLarge = Event("M987", 65)
    assertEquals("M987", designatedEventLarge.getEventNumber())
    assertEquals(65, designatedEventLarge.getNumberOfGuests())
    // 65 >= 50 triggers the $32 rate
    assertEquals(65 * 32.0, designatedEventLarge.getCalculatedPrice(), 0.001)

    val designatedEventSmall = Event("K12", 20)
    // Should pad K12 to K120 to enforce four characters
    assertEquals("K120", designatedEventSmall.getEventNumber())
    assertEquals(20, designatedEventSmall.getNumberOfGuests())
    // 20 < 50 triggers the $35 rate
    assertEquals(20 * 35.0, designatedEventSmall.getCalculatedPrice(), 0.001)
  }

  @Test
  fun testOverloadedConstructorsForHospitalAndRestaurant() {
    // 1. Hospital - Default constructor
    val defaultHospital = Hospital()
    assertEquals("Placeholder Hospital", defaultHospital.fetchName())
    assertEquals("Default Hospital Address", defaultHospital.fetchLocation())
    assertEquals(0.0, defaultHospital.distanceKm, 0.001)
    assertEquals(listOf("General Wellness"), defaultHospital.specialties)
    assertEquals("24/7", defaultHospital.fetchOpenHours())

    // 2. Hospital - Required mandatory data points constructor
    val mandatoryHospital = Hospital(
      name = "Lekki Women's Clinic",
      location = "Plot 5, block 11, Lekki",
      distanceKm = 1.5,
      specialties = listOf("Gynecology", "Obstetrics")
    )
    assertEquals("Lekki Women's Clinic", mandatoryHospital.fetchName())
    assertEquals("Plot 5, block 11, Lekki", mandatoryHospital.fetchLocation())
    assertEquals(1.5, mandatoryHospital.distanceKm, 0.001)
    assertEquals(listOf("Gynecology", "Obstetrics"), mandatoryHospital.specialties)
    assertEquals("24/7", mandatoryHospital.fetchOpenHours())

    // 3. Restaurant - Default constructor
    val defaultRestaurant = Restaurant()
    assertEquals("Placeholder Restaurant", defaultRestaurant.fetchName())
    assertEquals("General Cuisine", defaultRestaurant.cuisine)
    assertEquals(4.0, defaultRestaurant.rating, 0.001)
    assertEquals("123 Placeholder Street", defaultRestaurant.fetchLocation())
    assertFalse(defaultRestaurant.hasTableBooking)
    assertEquals(0.0, defaultRestaurant.fetchTablePrice(), 0.001)

    // 4. Restaurant - Required mandatory data points constructor
    val mandatoryRestaurant = Restaurant(
      name = "The Lagos Grill House",
      cuisine = "Barbecue & Steaks",
      rating = 4.7,
      address = "5 Admiralty Way, Lekki"
    )
    assertEquals("The Lagos Grill House", mandatoryRestaurant.fetchName())
    assertEquals("Barbecue & Steaks", mandatoryRestaurant.cuisine)
    assertEquals(4.7, mandatoryRestaurant.rating, 0.001)
    assertEquals("5 Admiralty Way, Lekki", mandatoryRestaurant.fetchLocation())
    assertTrue(mandatoryRestaurant.hasTableBooking)
    assertEquals(1500.0, mandatoryRestaurant.fetchTablePrice(), 0.001)
  }

  @Test
  fun testCustomerStateAndWalletReference() {
    // 1. Prepare initial items
    val item1 = RetailItem(name = "Elegant Handwoven Basket", price = 3200.0, category = "Home Decor")
    val item2 = RetailItem(name = "Beaded Maasai Choker", price = 1800.0, category = "Jewelry")

    // 2. Create customer with custom wallet
    val customWallet = WalletState(balance = 12000.0, walletPin = "9988")
    val customer = Customer(
      name = "Chidi Okafor",
      shoppingCart = listOf(item1),
      wallet = customWallet
    )

    // Asserts
    assertEquals("Chidi Okafor", customer.name)
    assertEquals(1, customer.shoppingCart.size)
    assertEquals(item1, customer.shoppingCart[0])
    assertEquals(customWallet, customer.getWallet())
    assertEquals(12000.0, customer.getWallet().balance, 0.001)

    // 3. Test dynamic cart addition/removal
    customer.addToCart(item2)
    assertEquals(2, customer.shoppingCart.size)
    assertEquals(item2, customer.shoppingCart[1])

    customer.removeFromCart(item1)
    assertEquals(1, customer.shoppingCart.size)
    assertEquals(item2, customer.shoppingCart[0])

    customer.clearCart()
    assertTrue(customer.shoppingCart.isEmpty())

    // 4. Test wallet mutability
    val updatedWallet = WalletState(balance = 15000.0, walletPin = "5544")
    customer.updateWallet(updatedWallet)
    assertEquals(updatedWallet, customer.getWallet())
    assertEquals(15000.0, customer.getWallet().balance, 0.001)

    // 5. Test Secondary Constructor with Array
    val arrayItems = arrayOf(item1, item2)
    val customerWithArray = Customer(
      name = "Adebayo Alao",
      shoppingCartArray = arrayItems,
      wallet = customWallet
    )
    assertEquals("Adebayo Alao", customerWithArray.name)
    assertEquals(2, customerWithArray.shoppingCart.size)
    assertEquals(item1, customerWithArray.shoppingCart[0])
    assertEquals(item2, customerWithArray.shoppingCart[1])
  }

  @Test
  fun testPythonStyleOrderingProcessSimulation() {
    // 1. Instantiations
    val customer = Customer(name = "Kofi Mensah")
    val employee = Employee(name = "Chioma")
    val system = OrderSystem()

    // 2. Interaction 1: Customer orders food from Employee Chioma
    val foodResult = customer.placeOrder(
      employee = employee,
      requestName = "Fried Rice & Turkey",
      price = 3500.0,
      isFood = true
    )

    // Verify received food item
    assertTrue(foodResult is FoodItem)
    val food = foodResult as FoodItem
    assertEquals("Fried Rice & Turkey", food.name)
    assertEquals(3500.0, food.fetchPrice(), 0.001)
    assertTrue(food.description.contains("Chioma"))

    // 3. Interaction 2: Customer orders product from Employee Chioma
    val productResult = customer.placeOrder(
      employee = employee,
      requestName = "Designer Dashiki Outerwear",
      price = 12500.0,
      isFood = false
    )

    // Verify received and registered product item in state cart
    assertTrue(productResult is RetailItem)
    val product = productResult as RetailItem
    assertEquals("Designer Dashiki Outerwear", product.name)
    assertEquals(12500.0, product.fetchPrice(), 0.001)
    assertEquals(1, customer.shoppingCart.size)
    assertEquals(product, customer.shoppingCart[0])

    // 4. Interaction 3: Customer orders food from automated system
    val systemFoodResult = customer.placeOrder(
      system = system,
      requestName = "Eba & Okro Soup",
      price = 2200.0,
      isFood = true
    )

    // Verify received system-dispatched food item
    assertTrue(systemFoodResult is FoodItem)
    val systemFood = systemFoodResult as FoodItem
    assertEquals("Eba & Okro Soup", systemFood.name)
    assertEquals(2200.0, systemFood.fetchPrice(), 0.001)
    assertTrue(systemFood.description.contains("Auto-dispatched"))

    // 5. Interaction 4: Customer orders product from automated system
    val systemProductResult = customer.placeOrder(
      system = system,
      requestName = "Lagos Metro Card",
      price = 500.0,
      isFood = false
    )

    // Verify received system-dispatched product item and cart state sync
    assertTrue(systemProductResult is RetailItem)
    val systemProduct = systemProductResult as RetailItem
    assertEquals("Lagos Metro Card", systemProduct.name)
    assertEquals(500.0, systemProduct.fetchPrice(), 0.001)
    assertEquals(2, customer.shoppingCart.size)
    assertEquals(systemProduct, customer.shoppingCart[1])
  }

  @Test
  fun testLunchOrderLifecycle() {
    val customer = Customer(name = "Dayo Coker")
    val employee = Employee(name = "Tunde")
    val lunchOrder = LunchOrder(
      customer = customer,
      employee = employee,
      foodName = "Amala & Gbegiri & Ewedu",
      price = 2800.0
    )

    // Initial state
    assertEquals(customer, lunchOrder.customer)
    assertEquals(employee, lunchOrder.employee)
    assertEquals("Amala & Gbegiri & Ewedu", lunchOrder.foodName)
    assertEquals(2800.0, lunchOrder.price, 0.001)
    assertEquals(OrderStatus.PENDING, lunchOrder.getStatus())
    assertNull(lunchOrder.getDeliveredFoodItem())

    // Advance lifecycle: PENDING -> PREPARING
    lunchOrder.advanceLifecycle()
    assertEquals(OrderStatus.PREPARING, lunchOrder.getStatus())
    assertNull(lunchOrder.getDeliveredFoodItem())

    // Advance lifecycle: PREPARING -> IN_TRANSIT
    lunchOrder.advanceLifecycle()
    assertEquals(OrderStatus.IN_TRANSIT, lunchOrder.getStatus())
    assertNull(lunchOrder.getDeliveredFoodItem())

    // Advance lifecycle: IN_TRANSIT -> DELIVERED
    lunchOrder.advanceLifecycle()
    assertEquals(OrderStatus.DELIVERED, lunchOrder.getStatus())
    
    // Delivered item check
    val foodItem = lunchOrder.getDeliveredFoodItem()
    assertNotNull(foodItem)
    assertEquals("Amala & Gbegiri & Ewedu", foodItem?.name)
    assertEquals(2800.0, foodItem?.fetchPrice() ?: 0.0, 0.001)
    assertTrue(foodItem?.description?.contains("Tunde") == true)

    // Further advancements keep it in DELIVERED state
    lunchOrder.advanceLifecycle()
    assertEquals(OrderStatus.DELIVERED, lunchOrder.getStatus())

    // Test explicit status update
    lunchOrder.updateStatus(OrderStatus.REFUNDED)
    assertEquals(OrderStatus.REFUNDED, lunchOrder.getStatus())
  }

  @Test
  fun testProductHierarchyAndMealPreparationTime() {
    // 1. Test generic Product class (superclass)
    val genericProduct = Product(
      name = "Wireless Keyboard",
      price = 4500.0
    )
    assertNotNull(genericProduct.id)
    assertEquals("Wireless Keyboard", genericProduct.name)
    assertEquals(4500.0, genericProduct.price, 0.001)

    // 2. Test Fruit subclass
    val freshMango: Product = Fruit(
      name = "Ogbomosho Mango",
      price = 450.0,
      sweetnessLevel = 5,
      origin = "Ogbomosho Farm"
    )
    assertTrue(freshMango is Product)
    assertEquals("Ogbomosho Mango", freshMango.name)
    assertEquals(450.0, freshMango.price, 0.001)
    assertEquals(5, (freshMango as Fruit).sweetnessLevel)
    assertEquals("Ogbomosho Farm", freshMango.origin)

    // 3. Test Meal subclass with preparationTime
    val partyJollof: Product = Meal(
      name = "Party Jollof Rice with Chicken & Dodo",
      price = 3500.0,
      preparationTime = 15, // 15 mins prep time
      servingsCount = 2,
      description = "Classic smoky Nigerian firewood party jollof"
    )
    assertTrue(partyJollof is Product)
    assertEquals("Party Jollof Rice with Chicken & Dodo", partyJollof.name)
    assertEquals(3500.0, partyJollof.price, 0.001)
    assertEquals(15, (partyJollof as Meal).preparationTime) // Verified preparation time attribute
    assertEquals(2, partyJollof.servingsCount)
    assertEquals("Classic smoky Nigerian firewood party jollof", partyJollof.description)
  }

  @Test
  fun testShellTerminalInquiryAndAddRemoveCases() {
    val service = HospitalDirectoryService()
    
    // Setup initial records
    val h1 = Hospital(id = "h1", name = "Lagoon Hospital", location = "Lagos Island", distanceKm = 1.5, specialties = listOf("Cardiology"), openHours = "24/7")
    val h2 = Hospital(id = "h2", name = "St Nicholas", location = "Lafiaji", distanceKm = 2.0, specialties = listOf("Emergency"), openHours = "24/7")
    service.setHospitals(listOf(h1, h2))

    // Emulate command parsing cases (Kotlin when statement) mimicking executeTerminalCommand logic
    fun executeMockCommand(cmd: String): Pair<String, List<Hospital>> {
      val trimmed = cmd.trim()
      val parts = trimmed.split(" ", limit = 2)
      val action = parts[0]
      val arguments = if (parts.size > 1) parts[1] else ""

      return when (action) {
        "1" -> {
          try {
            val m = service.lookup(arguments)
            val resultStr = if (m.isEmpty()) "No matches" else "Found ${m.size} matches"
            Pair(resultStr, service.getAllHospitals())
          } catch (e: NoSuchElementException) {
            Pair("Lookup: ${e.message}", service.getAllHospitals())
          }
        }
        "2" -> {
          val csv = arguments.split(",")
          val name = csv[0].trim()
          val loc = csv[1].trim()
          val dist = csv[2].trim().toDoubleOrNull() ?: 1.0
          val specs = csv[3].trim().split(";").map { it.trim() }

          val newH = Hospital(UUID.randomUUID().toString(), name, loc, dist, specs)
          service.addHospital(newH)
          Pair("Success added", service.getAllHospitals())
        }
         "3" -> {
          val removed = service.removeHospitalByName(arguments)
          val resultStr = if (removed) "Removed successfully" else "No hospital found to remove"
          Pair(resultStr, service.getAllHospitals())
        }
        else -> Pair("Invalid command", service.getAllHospitals())
      }
    }

    // 1. Test case '1': lookup (matching)
    val (lookStr, lookList) = executeMockCommand("1 Nicholas")
    assertEquals("Found 1 matches", lookStr)
    assertEquals(2, lookList.size)

    // 1b. Test case '1': lookup (not matching / descriptive error scenario)
    val (failLookStr, _) = executeMockCommand("1 NonExistentLagosClinic")
    assertTrue(failLookStr.contains("does not match any active hospital name"))
    assertTrue(failLookStr.contains("NonExistentLagosClinic"))

    // 2. Test case '2': add
    val (addStr, addList) = executeMockCommand("2 Reddington Clinic, Lekki, 3.5, Pediatrics;Emergency")
    assertEquals("Success added", addStr)
    assertEquals(3, addList.size)
    assertTrue(addList.any { it.name == "Reddington Clinic" })

    // 3. Test case '3': remove
    val (remStr, remList) = executeMockCommand("3 Reddington Clinic")
    assertEquals("Removed successfully", remStr)
    assertEquals(2, remList.size)
    assertFalse(remList.any { it.name == "Reddington Clinic" })
  }

  @Test
  fun testWalletPurseCoinConversion() {
    // Test that the conversion correctly tracks 1 gold = 100 silver pieces
    val state = com.example.domain.model.WalletState(balance = 12345.0)
    val purse = state.purse

    // 12345 silver pieces should be equivalent to 123 gold pieces and 45 silver pieces
    assertEquals(123, purse.goldCoins)
    assertEquals(45, purse.silverCoins)
    assertEquals(12345, purse.toTotalSilverPieces())

    // Direct instantiation mapping
    val customPurse = com.example.domain.model.CoinPurse(5, 78)
    assertEquals(578, customPurse.toTotalSilverPieces())
    
    val recreated = com.example.domain.model.CoinPurse.fromValue(578.0)
    assertEquals(5, recreated.goldCoins)
    assertEquals(78, recreated.silverCoins)

    // Verify performPurchase method
    // 5 gold, 78 silver = 578 silver pieces. Subtracting 120 silver pieces leaves 458 silver pieces = 4 gold, 58 silver.
    val updatedPurse = customPurse.performPurchase(120.0)
    assertEquals(4, updatedPurse.goldCoins)
    assertEquals(58, updatedPurse.silverCoins)
    assertEquals(458, updatedPurse.toTotalSilverPieces())

    // Try a purchase directly on WalletState
    val originalWallet = com.example.domain.model.WalletState(balance = 12345.0)
    val updatedWallet = originalWallet.performPurchase(345.0) // 12345 - 345 = 12000 => 120 Gold and 0 Silver
    assertEquals(120, updatedWallet.purse.goldCoins)
    assertEquals(0, updatedWallet.purse.silverCoins)
    assertEquals(12000.0, updatedWallet.balance, 0.001)

    // Verify exception for insufficient funds
    assertThrows(IllegalArgumentException::class.java) {
      customPurse.performPurchase(1000.0)
    }
  }

  @Test
  fun testBillingCalculations() {
    val billing = com.example.domain.model.Billing()

    // 1. Single item price scenario
    val bill1 = billing.computeBill(125.753)
    assertEquals(125.75, bill1, 0.0)

    // 2. Item price with quantity scenario
    val bill2 = billing.computeBill(45.50, 3)
    assertEquals(136.50, bill2, 0.0)

    // 3. Item price with quantity minus coupon scenario
    val bill3 = billing.computeBill(99.99, 2, 15.50)
    assertEquals(184.48, bill3, 0.0) // 99.99 * 2 = 199.98 - 15.50 = 184.48

    // 4. Over-valued coupon scenario (should floor at 0.0)
    val bill4 = billing.computeBill(10.00, 2, 50.00)
    assertEquals(0.0, bill4, 0.0)
  }
}


