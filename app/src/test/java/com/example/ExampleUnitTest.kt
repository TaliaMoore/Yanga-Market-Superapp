package com.example

import com.example.domain.model.Event
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
import org.junit.Assert.*
import org.junit.Test

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
    assertEquals(75 * 35.0, event.getCalculatedPrice(), 0.001)
    assertTrue(event.isLargeEvent()) // 75 >= 50

    // 3. Testing custom event number setting and getter/setter validation
    event.setEventNumber("c123")
    assertEquals("C123", event.getEventNumber()) // auto-capitalized by Carly's policy
    
    // Testing negative boundary case
    event.setGuests(-5)
    assertEquals(75, event.getNumberOfGuests()) // Should ignore invalid negative values and retain 75
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
}


