package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MarketScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val foods by viewModel.foods.collectAsState()
    val fruits by viewModel.fruits.collectAsState()
    val shops by viewModel.retailShops.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    // Observe home screen deep link filter state
    val deepFilter by viewModel.dashboardMarketFilter.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Food & Fruits, 1: Retail, 2: Dine-In

    // Sorter / Filter system state
    var sortOption by remember { mutableStateOf("Default") } // "Default", "Closest", "PriceAsc", "PriceDesc"
    var viewModeOption by remember { mutableStateOf("All") } // "All", "Restaurant", "Fruit"
    var filterClosestOnly by remember { mutableStateOf(false) }

    // Synchronize deep links from the Dashboard/Home screen
    androidx.compose.runtime.LaunchedEffect(deepFilter) {
        when (deepFilter) {
            "Supermarket" -> {
                selectedTab = 1 // Retail Shops tab
            }
            "Bakery" -> {
                selectedTab = 0 // Eats & Fruits tab
                viewModeOption = "Restaurant" // Show bakeries/restaurants
            }
            else -> {
                // Keep existing choices
            }
        }
    }

    // Detailed custom sub-page state for selected restaurant
    var selectedRestaurantForMenu by remember { mutableStateOf<com.example.domain.model.Restaurant?>(null) }

    // Sorted / Filtered Collections dynamically computed
    val sortedRestaurants = remember(restaurants, sortOption) {
        when (sortOption) {
            "Closest" -> restaurants.sortedBy { it.distanceKm }
            "PriceAsc" -> restaurants.sortedBy { it.tablePrice }
            "PriceDesc" -> restaurants.sortedByDescending { it.tablePrice }
            else -> restaurants
        }
    }

    val eatsRestaurants = remember(restaurants, sortOption, filterClosestOnly, deepFilter) {
        var list = restaurants
        if (deepFilter == "Bakery") {
            list = list.filter { it.cuisine.contains("Bakery", ignoreCase = true) }
        }
        if (filterClosestOnly) {
            list = list.filter { it.distanceKm <= 1.5 }
        }
        when (sortOption) {
            "PriceAsc" -> list.sortedBy { r -> r.meals.minOfOrNull { it.price } ?: r.tablePrice }
            "PriceDesc" -> list.sortedByDescending { r -> r.meals.maxOfOrNull { it.price } ?: r.tablePrice }
            else -> list
        }
    }

    // Eats foods should only be organic fresh fruits as requested! "All these meals and snacks, it should only be fruits under that section."
    val eatsFoods = remember(fruits, sortOption) {
        val list = fruits
        when (sortOption) {
            "PriceAsc" -> list.sortedBy { it.price }
            "PriceDesc" -> list.sortedByDescending { it.price }
            else -> list
        }
    }

    val sortedFruits = remember(fruits, sortOption) {
        when (sortOption) {
            "PriceAsc" -> fruits.sortedBy { it.price }
            "PriceDesc" -> fruits.sortedByDescending { it.price }
            else -> fruits
        }
    }

    val sortedShops = remember(shops, sortOption, deepFilter) {
        var list = shops
        if (deepFilter == "Supermarket") {
            list = list.filter { it.specialty.contains("Supermarket", ignoreCase = true) }
        }
        when (sortOption) {
            "Closest" -> list.sortedBy { it.distanceKm }
            "PriceAsc" -> list.sortedBy { s -> s.items.minOfOrNull { it.price } ?: 0.0 }
            "PriceDesc" -> list.sortedByDescending { s -> s.items.maxOfOrNull { it.price } ?: 0.0 }
            else -> list
        }
    }

    // Cart Sheet State
    var showCartDialog by remember { mutableStateOf(false) }

    val cartCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.price * it.quantity }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedRestaurantForMenu != null) {
            val r = selectedRestaurantForMenu!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Back Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { selectedRestaurantForMenu = null }
                        .padding(vertical = 8.dp)
                        .testTag("menu_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Back to Market",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Restaurant Title Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = getRestaurantDrawableRes(r.name)),
                            contentDescription = r.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = r.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack
                            )
                            Text(
                                text = "Cuisine: ${r.cuisine} • ⭐ ${r.rating}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBlack.copy(alpha = 0.6f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${r.address} (${r.distanceKm}km away)",
                                    fontSize = 11.sp,
                                    color = CharcoalBlack.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Gourmet Kitchen Menu 🍲",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (r.meals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No dishes loaded for this kitchen right now.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        items(r.meals) { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = getFoodDrawableRes(meal.name)),
                                    contentDescription = meal.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = meal.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CharcoalBlack
                                    )
                                    Text(
                                        text = meal.description,
                                        fontSize = 10.sp,
                                        color = CharcoalBlack.copy(alpha = 0.6f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₦${String.format("%,.0f", meal.price)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryPurple
                                    )
                                }

                                Button(
                                    onClick = { viewModel.addToCart(meal.name, meal.price, meal.category, "FOOD") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .border(1.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                                        .testTag("add_item_${meal.name.replace(" ", "_")}"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("+ Add", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- Header ---
                YangaHeader(
                    title = "Yanga Market 🎪",
                    subtitle = "Browse delicious foods, organic fruits, retail goods and dine-in tables",
                    icon = Icons.Default.ShoppingCart,
                    onIconClick = { if (cartCount > 0) showCartDialog = true }
                )

            // --- Navigation Tabs ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = PrimaryPurple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryPurple
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Eats & Fruits 🍉", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                    selectedContentColor = PrimaryPurple,
                    unselectedContentColor = CharcoalBlack.copy(alpha = 0.5f)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Retail 👕", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                    selectedContentColor = PrimaryPurple,
                    unselectedContentColor = CharcoalBlack.copy(alpha = 0.5f)
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Dine-In 🍝", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                    selectedContentColor = PrimaryPurple,
                    unselectedContentColor = CharcoalBlack.copy(alpha = 0.5f)
                )
            }

            // --- Adaptive Sort / Filter Chips Bar ---
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. SELECT CATEGORY/VIEW MODE ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        YangaSortChip(
                            text = "Show All 🍱",
                            selected = viewModeOption == "All",
                            onClick = { viewModeOption = "All" },
                            modifier = Modifier.testTag("filter_view_all")
                        )
                        YangaSortChip(
                            text = "Restaurants Only 🏪",
                            selected = viewModeOption == "Restaurant",
                            onClick = { viewModeOption = "Restaurant" },
                            modifier = Modifier.testTag("filter_view_restaurants")
                        )
                        YangaSortChip(
                            text = "Fruits Only 🍍",
                            selected = viewModeOption == "Fruit",
                            onClick = { viewModeOption = "Fruit" },
                            modifier = Modifier.testTag("filter_view_fruits")
                        )
                    }

                    if (deepFilter != "None") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Filter:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SecondaryYellow.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { viewModel.setDashboardMarketFilter("None") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$deepFilter limit 🎯",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryPurple
                                    )
                                    Text(
                                        text = "✕",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryPurple
                                    )
                                }
                            }
                        }
                    }

                    // 2. DISTANCE FILTER AND PRICE SORT ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Options:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        YangaSortChip(
                            text = "Nearest Only (≤ 1.5km) 📍",
                            selected = filterClosestOnly,
                            onClick = { filterClosestOnly = !filterClosestOnly },
                            modifier = Modifier.testTag("filter_closest_only")
                        )
                        YangaSortChip(
                            text = "Lowest to Highest 📈",
                            selected = sortOption == "PriceAsc",
                            onClick = { sortOption = if (sortOption == "PriceAsc") "Default" else "PriceAsc" },
                            modifier = Modifier.testTag("filter_cheapest_btn")
                        )
                        YangaSortChip(
                            text = "Highest to Lowest 📉",
                            selected = sortOption == "PriceDesc",
                            onClick = { sortOption = if (sortOption == "PriceDesc") "Default" else "PriceDesc" },
                            modifier = Modifier.testTag("filter_costly_btn")
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    YangaSortChip(
                        text = "Nearest 📍",
                        selected = sortOption == "Closest",
                        onClick = { sortOption = if (sortOption == "Closest") "Default" else "Closest" },
                        modifier = Modifier.testTag("filter_closest_btn")
                    )
                    YangaSortChip(
                        text = "Lowest to Highest 📈",
                        selected = sortOption == "PriceAsc",
                        onClick = { sortOption = if (sortOption == "PriceAsc") "Default" else "PriceAsc" },
                        modifier = Modifier.testTag("filter_cheapest_btn")
                    )
                    YangaSortChip(
                        text = "Highest to Lowest 📉",
                        selected = sortOption == "PriceDesc",
                        onClick = { sortOption = if (sortOption == "PriceDesc") "Default" else "PriceDesc" },
                        modifier = Modifier.testTag("filter_costly_btn")
                    )
                }
            }

            // --- Scrollable Lists based on Selected Tab ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (viewModeOption == "All" || viewModeOption == "Restaurant") {
                            item {
                                ListCategoryTitle(text = "Local Kitchens & Restaurants 🏪")
                            }
                            if (eatsRestaurants.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No kitchens found in this range. 📍", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                items(eatsRestaurants, key = { it.id }) { r ->
                                    RestaurantEatsCard(
                                        restaurant = r,
                                        onBrowseMenu = {
                                            selectedRestaurantForMenu = r
                                        }
                                    )
                                }
                            }
                        }

                        if (viewModeOption == "All" || viewModeOption == "Fruit") {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                ListCategoryTitle(
                                    text = if (viewModeOption == "Fruit") "Organic Fresh Fruits Only 🍎" else "Organic Fresh Foods & Fruits 🍍"
                                )
                            }
                            if (eatsFoods.isEmpty()) {
                                item { LoaderPlaceholder() }
                            } else {
                                items(eatsFoods, key = { it.name }) { item ->
                                    ShoppingItemCard(
                                        name = item.name,
                                        price = item.price,
                                        category = item.category,
                                        desc = item.description,
                                        isFruit = item.category.equals("fruit", ignoreCase = true) || item.name.contains("Pineapple", ignoreCase = true) || item.name.contains("Mango", ignoreCase = true) || item.name.contains("Smoothie", ignoreCase = true) || item.name.contains("Fruit", ignoreCase = true),
                                        onAdd = { viewModel.addToCart(item.name, item.price, item.category, if (item.category.equals("fruit", ignoreCase = true)) "FRUIT" else "FOOD") }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            ListCategoryTitle(text = "Local Retail Outlets & Grocers 🛍️")
                        }
                        if (sortedShops.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(sortedShops, key = { it.name }) { shop ->
                                RetailShopCard(shop = shop, onAddItem = { name, price, cat ->
                                    viewModel.addToCart(name, price, cat, "RETAIL")
                                })
                            }
                        }
                    }

                    2 -> {
                        item {
                            ListCategoryTitle(text = "Dine-Out Table Bookings 🍽️")
                        }
                        if (sortedRestaurants.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(sortedRestaurants, key = { it.name }) { r ->
                                RestaurantBookingCard(
                                    restaurant = r,
                                    onReserve = { time ->
                                        viewModel.reserveRestaurantTable(r, time)
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ListCategoryTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = PrimaryPurple,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun LoaderPlaceholder() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SecondaryYellow.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ShoppingItemCard(
    name: String,
    price: Double,
    category: String,
    desc: String,
    isFruit: Boolean,
    onAdd: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, if (isFruit) Color(0xFF22C55E).copy(alpha = 0.5f) else PrimaryPurple.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getFoodDrawableRes(name)),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, if (isFruit) Color(0xFF22C55E).copy(alpha = 0.3f) else PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (category.isNotBlank()) {
                        YangaBadge(
                            text = category,
                            containerColor = if (isFruit) Color(0xFFDCFCE7) else SecondaryYellow,
                            contentColor = if (isFruit) Color(0xFF15803D) else PrimaryPurple
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Eats",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = if (price > 0) "₦${String.format("%,.0f", price)}" else "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (name.isNotBlank()) name else "Food Selection",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack
                )
                val finalDesc = if (desc.isNotBlank()) desc else "Refreshing and healthy selection sourced fresh daily."
                Text(
                    text = finalDesc,
                    fontSize = 10.sp,
                    color = CharcoalBlack.copy(alpha = 0.60f),
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isFruit) Color(0xFF22C55E) else PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(30.dp)
                        .border(1.dp, if (isFruit) Color(0xFF15803D) else PrimaryPurple, RoundedCornerShape(8.dp))
                        .testTag("add_item_${name.replace(" ", "_")}"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Add +",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun getRetailShopDrawableRes(shopName: String): Int {
    return when {
        shopName.contains("Fashion", ignoreCase = true) -> com.example.R.drawable.img_retail_fashion_1782106609270
        shopName.contains("Alaba", ignoreCase = true) || shopName.contains("Gadget", ignoreCase = true) || shopName.contains("Tech", ignoreCase = true) -> com.example.R.drawable.img_retail_tech_1782106619597
        else -> com.example.R.drawable.img_retail_tech_1782106619597
    }
}

fun getRestaurantDrawableRes(restaurantName: String): Int {
    return when {
        restaurantName.contains("Pepper", ignoreCase = true) -> com.example.R.drawable.img_dining_pepper_1782106632965
        restaurantName.contains("Wok", ignoreCase = true) || restaurantName.contains("Panda", ignoreCase = true) -> com.example.R.drawable.img_dining_wok_1782106645149
        else -> com.example.R.drawable.img_food_suya_burger
    }
}

@Composable
fun YangaSortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryPurple else SecondaryYellow.copy(alpha = 0.2f))
            .border(2.dp, if (selected) CharcoalBlack else PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) Color.White else PrimaryPurple
        )
    }
}

@Composable
fun RetailShopCard(
    shop: com.example.domain.model.RetailShop,
    onAddItem: (String, Double, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
    ) {
        Column {
            // Header Image showing Retail Shop graphics
            Image(
                painter = painterResource(id = getRetailShopDrawableRes(shop.name)),
                contentDescription = shop.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = PrimaryPurple)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = shop.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${shop.specialty}",
                                    fontSize = 11.sp,
                                    color = CharcoalBlack.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                                        .padding(vertical = 1.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "${shop.distanceKm}km away",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = PrimaryPurple
                    )
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = PrimaryPurple.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    shop.items.forEach { retailItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = getRetailShopDrawableRes(shop.name)),
                                contentDescription = retailItem.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = retailItem.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBlack
                                )
                                Text(
                                    text = "Category: ${retailItem.category}",
                                    fontSize = 10.sp,
                                    color = CharcoalBlack.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "₦${String.format("%,.0f", retailItem.price)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryPurple
                                )
                            }

                            Button(
                                onClick = { onAddItem(retailItem.name, retailItem.price, retailItem.category) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .border(1.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                                    .testTag("add_item_${retailItem.name.replace(" ", "_")}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text("+ Buy", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantEatsCard(
    restaurant: com.example.domain.model.Restaurant,
    onBrowseMenu: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
            .clickable { onBrowseMenu() }
            .testTag("restaurant_eats_card_${restaurant.name.replace(" ", "_")}")
    ) {
        Column {
            Image(
                painter = painterResource(id = getRestaurantDrawableRes(restaurant.name)),
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack,
                        modifier = Modifier.weight(1f)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SecondaryYellow.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⭐ ${restaurant.rating}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Specialty: ${restaurant.cuisine}",
                    fontSize = 11.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Distance",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${restaurant.address} (${restaurant.distanceKm} km away)",
                        fontSize = 10.sp,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onBrowseMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("browse_menu_button_${restaurant.name.replace(" ", "_")}"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Browse Dishes & Menu 🍲",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantBookingCard(
    restaurant: com.example.domain.model.Restaurant,
    onReserve: (String) -> Unit,
    viewModel: MainViewModel
) {
    val times = listOf("12:00 PM", "3:30 PM", "7:00 PM")
    var selectedTime by remember { mutableStateOf("7:00 PM") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
    ) {
        Column {
            // Header Image showing Restaurant dining picture
            Image(
                painter = painterResource(id = getRestaurantDrawableRes(restaurant.name)),
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = restaurant.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⭐ ${restaurant.rating} • ${restaurant.cuisine}",
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                    .padding(vertical = 1.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${restaurant.distanceKm}km away",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Table: ₦${String.format("%,.0f", restaurant.tablePrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time slot selector
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (time in times) {
                            val active = time == selectedTime
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) PrimaryPurple else Color(0xFFF1F1F1))
                                    .clickable { selectedTime = time }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = time,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else CharcoalBlack
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = { onReserve(selectedTime) },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .border(1.5.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                            .testTag("reserve_btn_${restaurant.name.replace(" ", "_")}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text("Book Table", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Delicious Menu 🔼" else "View Restaurant Menu (${restaurant.meals.size} Dishes) 🍲 🔽",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = PrimaryPurple.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (restaurant.meals.isEmpty()) {
                        Text(
                            text = "No dishes loaded for this kitchen right now.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        restaurant.meals.forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = getFoodDrawableRes(meal.name)),
                                    contentDescription = meal.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = meal.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack
                                    )
                                    Text(
                                        text = meal.description,
                                        fontSize = 10.sp,
                                        color = CharcoalBlack.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "₦${String.format("%,.0f", meal.price)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryPurple
                                    )
                                }

                                Button(
                                    onClick = { viewModel.addToCart(meal.name, meal.price, meal.category, "FOOD") },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("add_item_${meal.name.replace(" ", "_")}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("+ Order", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilitySpaceInquiryCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.facilitySearchQuery.collectAsState()
    val selectedFloor by viewModel.facilitySelectedFloor.collectAsState()
    val sections by viewModel.lookedUpSections.collectAsState()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryPurple.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Facility Icon",
                        tint = PrimaryPurple
                    )
                }
                Column {
                    Text(
                        text = "Yanga Mall Space Explorer 🏢",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Interactive 2D Facility Layout & Availability",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prompter Input Field String
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateFacilitySearchQuery(it) },
                label = { Text("Prompt for Floor (1-4) or Location Code (e.g. F2-B)") },
                placeholder = { Text("Search rent price or occupancy...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("facility_inquiry_input"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Search indicator",
                        tint = PrimaryPurple
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateFacilitySearchQuery("") },
                            modifier = Modifier.testTag("facility_inquiry_clear")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear input",
                                tint = CharcoalBlack.copy(alpha = 0.4f)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.4f),
                    focusedLabelColor = PrimaryPurple,
                    cursorColor = PrimaryPurple
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Floor Chips Selector
            Text(
                text = "Quick Map Level Selection:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalBlack.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // All floors
                val isAllSelected = selectedFloor == null && searchQuery.isEmpty()
                InteractiveFloorChip(
                    text = "All",
                    isSelected = isAllSelected,
                    onClick = { viewModel.selectFacilityFloor(null) }
                )

                (1..4).forEach { floorNum ->
                    InteractiveFloorChip(
                        text = "Floor $floorNum",
                        isSelected = selectedFloor == floorNum,
                        onClick = { viewModel.selectFacilityFloor(floorNum) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results from 2D Layout Grid
            Text(
                text = "Filtered Compartments (${sections.size} found):",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryPurple
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (sections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching sections found for your inquiry.",
                        fontSize = 12.sp,
                        color = Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Render list of sections
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sections.forEach { section ->
                        FacilitySectionRow(section = section)
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveFloorChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) PrimaryPurple else Color(0xFFF3F4F6),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else CharcoalBlack.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FacilitySectionRow(
    section: com.example.domain.model.FacilitySection
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge Location Code
                    Box(
                        modifier = Modifier
                            .background(PrimaryPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = section.locationCode,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryPurple
                        )
                    }
                    Text(
                        text = section.fetchCoordinateString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = section.servicesSubtype,
                    fontSize = 10.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Formatting currency
                Text(
                    text = "₦%,.0f/mo".format(section.rentAmt),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF047857) // Dark Emerald Green
                )
                // Availability Status Badge Pill
                Box(
                    modifier = Modifier
                        .background(
                            if (section.isAvailable) Color(0xFFD1FAE5) else Color(0xFFFFEDD5),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (section.isAvailable) "Available" else "Occupied",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (section.isAvailable) Color(0xFF065F46) else Color(0xFF9A3412)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun YangaMultiColumnProductHelpToolSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    YangaVisuallyDistinctSection(
        title = "Yanga Dynamic Directory Help 💡🗺️",
        subtitle = "Experience our mouseover-driven event explorer. Hover (or hover-click) categories, sub-categories, or items to instantly fetch real-time specifications:",
        headerBadgeText = "ACTIVE LISTENERS",
        headerBadgeColor = Color(0xFFFEF08A), // pale yellow
        backgroundColor = Color(0xFFFAF5FF), // pale purple
        borderColor = PrimaryPurple,
        borderWidth = 2.0,
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        YangaMultiColumnProductHelpTool(viewModel = viewModel)
    }
}

// Data Models for dynamic multi-column help center
private data class HelpCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val subcategories: List<HelpSubcategory>
)

private data class HelpSubcategory(
    val id: String,
    val name: String,
    val emoji: String,
    val products: List<HelpProduct>
)

private data class HelpProduct(
    val id: String,
    val name: String,
    val price: Double,
    val details: String,
    val unit: String = "each"
)

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.yangaMouseoverAndClick(
    onTrigger: () -> Unit
): Modifier = this.clickable { onTrigger() }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun YangaMultiColumnProductHelpTool(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val helpData = remember {
        listOf(
            HelpCategory(
                id = "cat_food",
                name = "Food Eats",
                emoji = "🥘",
                subcategories = listOf(
                    HelpSubcategory(
                        id = "sub_local",
                        name = "Local Delicacies",
                        emoji = "🍲",
                        products = listOf(
                            HelpProduct("prod_jollof", "Jollof Rice Extra", 3500.0, "Authentic smoky wood-fired party Jollof Rice served with choice grilled chicken piece, sweet fried dodo plantains, and fresh slaw."),
                            HelpProduct("prod_suya", "Suya Platter Mix", 5000.0, "Finely shredded charcoal-grilled premium beef skewers tossed in traditional Hausa yaji spice, onions, and shredded cabbage."),
                            HelpProduct("prod_soup", "Pepper Soup Bowl", 4000.0, "Hot & fiery light broth of slow-simmered tender goat meat cuts, flavored with calabash nutmeg, scent leaves, and local chili.")
                        )
                    ),
                    HelpSubcategory(
                        id = "sub_fast",
                        name = "Street Snacks",
                        emoji = "🥞",
                        products = listOf(
                            HelpProduct("prod_puffs", "Puff Puff Basket", 2500.0, "Golden-brown fluffy deep-fried local yeast sweet dough balls, dusted finely with powdered sugar or chocolate glaze sauce."),
                            HelpProduct("prod_burger", "Yanga Beef Burger", 4500.0, "Thick hand-pressed double beef patty with melted cheese, spicy scotch bonnet mayo, lettuce, and tomatoes inside warm brioche bun.")
                        )
                    )
                )
            ),
            HelpCategory(
                id = "cat_fruits",
                name = "Fresh Fruits",
                emoji = "🍍",
                subcategories = listOf(
                    HelpSubcategory(
                        id = "sub_citrus",
                        name = "Citrus Boosts",
                        emoji = "🍋",
                        products = listOf(
                            HelpProduct("prod_oranges", "Sweet Tangerines", 2200.0, "Lagos local seedless sweet tangerines, highly refreshing and naturally packed with raw Vitamin C nutrients."),
                            HelpProduct("prod_lemons", "Yellow Lemons", 1800.0, "Juicy handpicked bright yellow lemons perfect for detox drinks, marinades, or zest preparations.")
                        )
                    ),
                    HelpSubcategory(
                        id = "sub_tropical",
                        name = "Tropical Fresh",
                        emoji = "🍉",
                        products = listOf(
                            HelpProduct("prod_pines", "Premium Pineapple", 2000.0, "Whole gold sweet sliced pineapple crown, packed in vacuum containers to preserve sweet natural juice essence."),
                            HelpProduct("prod_avo", "Buttery Avocado", 1500.0, "Rich pear-shaped buttery smooth avocados. Sourced daily from lush organic orchards in southwestern Nigeria.")
                        )
                    )
                )
            ),
            HelpCategory(
                id = "cat_hospital",
                name = "Health Care",
                emoji = "🏥",
                subcategories = listOf(
                    HelpSubcategory(
                        id = "sub_clinics",
                        name = "Elite Clinics",
                        emoji = "🩺",
                        products = listOf(
                            HelpProduct("prod_lagoon", "Lagoon Emergency", 12000.0, "Instant priority trauma consultations, state-of-the-art specialist doctors, 24/7 cardiac ICU care networks.", "consult"),
                            HelpProduct("prod_evercare", "Evercare Pediatrics", 15000.0, "Specialist child neonatology clinics, pediatric consultation wards, premium multi-specialist private nursing.", "visit")
                        )
                    ),
                    HelpSubcategory(
                        id = "sub_pharms",
                        name = "Pharmacies",
                        emoji = "💊",
                        products = listOf(
                            HelpProduct("prod_medplus", "Medplus Certified", 3500.0, "Top-tier medication dispensing, basic blood sugar/pressure screening, prescription support, active pharmacy.", "pack"),
                            HelpProduct("prod_healthplus", "HealthPlus Organic", 4000.0, "Premium organic wellness supplements, safe herbal extract consultations, certified immunity support boosts.", "pack")
                        )
                    )
                )
            ),
            HelpCategory(
                id = "cat_events",
                name = "Event Services",
                emoji = "🎪",
                subcategories = listOf(
                    HelpSubcategory(
                        id = "sub_cater",
                        name = "Catering Packages",
                        emoji = "🍽️",
                        products = listOf(
                            HelpProduct("prod_carly_std", "Standard Guest Rate", 35.0, "Carly's authentic catering rate computed per guest for standard banquet events. Includes 1 entree, 2 sides, 1 dessert.", "guest"),
                            HelpProduct("prod_carly_lrg", "Large Event Discount", 32.0, "Discounted rate calculated automatically for larger dinner gatherings (cutoff 50 guests or more). Includes complete buffet service.", "guest")
                        )
                    ),
                    HelpSubcategory(
                        id = "sub_rent",
                        name = "Party Rentals",
                        emoji = "🛋️",
                        products = listOf(
                            HelpProduct("prod_canopy", "Waterproof Canopy", 45000.0, "Heavy-duty outdoor event tent with strong steel frame grids and luxurious white fabric curtains. Safe against wind.", "day"),
                            HelpProduct("prod_lights", "Ballroom Chandelier", 15000.0, "Premium sparkling multi-tiered chandelier fixture layout to grace grand wedding ballroom setups.", "day")
                        )
                    )
                )
            )
        )
    }

    // Reactive states for columns
    var selectedCategory by remember { mutableStateOf(helpData[0]) }
    var selectedSubcategory by remember { mutableStateOf(helpData[0].subcategories[0]) }
    var selectedProduct by remember { mutableStateOf(helpData[0].subcategories[0].products[0]) }

    fun selectCat(cat: HelpCategory) {
        selectedCategory = cat
        if (cat.subcategories.isNotEmpty()) {
            selectedSubcategory = cat.subcategories[0]
            if (cat.subcategories[0].products.isNotEmpty()) {
                selectedProduct = cat.subcategories[0].products[0]
            }
        }
    }

    fun selectSub(sub: HelpSubcategory) {
        selectedSubcategory = sub
        if (sub.products.isNotEmpty()) {
            selectedProduct = sub.products[0]
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Hover items or slide horizontally to browse category columns below 🗺️",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalBlack.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(2.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Start
        ) {
            
            // COLUMN 1: CATEGORY (Width 160dp)
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFAF5FF))
                    .border(width = 1.dp, color = PrimaryPurple.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryPurple)
                        .padding(8.dp)
                ) {
                    Text("1. Categories", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    helpData.forEach { cat ->
                        val isSelected = selectedCategory.id == cat.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SecondaryYellow else Color.White)
                                .border(1.dp, if (isSelected) PrimaryPurple else Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .yangaMouseoverAndClick { selectCat(cat) }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("help_cat_${cat.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryPurple else CharcoalBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // COLUMN 2: SUB-CATEGORY (Width 170dp)
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .border(width = 1.dp, color = PrimaryPurple.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryPurple.copy(alpha = 0.8f))
                        .padding(8.dp)
                ) {
                    Text("2. Sub-categories", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedCategory.subcategories.forEach { sub ->
                        val isSelected = selectedSubcategory.id == sub.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SecondaryYellow.copy(alpha = 0.4f) else Color.Transparent)
                                .border(1.dp, if (isSelected) PrimaryPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                .yangaMouseoverAndClick { selectSub(sub) }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("help_sub_${sub.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sub.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sub.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryPurple else CharcoalBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // COLUMN 3: PRODUCT (Width 180dp)
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFFFDF5))
                    .border(width = 1.dp, color = PrimaryPurple.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF97316))
                        .padding(8.dp)
                ) {
                    Text("3. Products", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedSubcategory.products.forEach { prod ->
                        val isSelected = selectedProduct.id == prod.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFEF3C7) else Color.White)
                                .border(1.dp, if (isSelected) Color(0xFFD97706) else Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .yangaMouseoverAndClick { selectedProduct = prod }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("help_prod_${prod.id}")
                        ) {
                            Column {
                                Text(
                                    text = prod.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF92400E) else CharcoalBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (prod.unit == "guest") "₦${prod.price.toInt()}/guest" else "₦%,.0f".format(prod.price),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFFB45309) else Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
            }

            // COLUMN 4: INFO DISPLAY (Width 250dp)
            Column(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10B981), RoundedCornerShape(6.dp))
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("4. Specifications Info", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = selectedProduct.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF059669), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RATE / PRICE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF047857)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedProduct.unit == "guest") "₦${selectedProduct.price.toInt()} per guest" else "₦%,.0f / ${selectedProduct.unit}".format(selectedProduct.price),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857)
                        )
                    }

                    if (selectedCategory.id == "cat_food" || selectedCategory.id == "cat_fruits") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = painterResource(id = getFoodDrawableRes(selectedProduct.name)),
                            contentDescription = selectedProduct.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Technical Specification:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = selectedProduct.details,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalBlack.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            when (selectedCategory.id) {
                                "cat_food" -> viewModel.addToCart(selectedProduct.name, selectedProduct.price, "Food", "FOOD")
                                "cat_fruits" -> viewModel.addToCart(selectedProduct.name, selectedProduct.price, "Fruit", "FRUIT")
                                else -> {
                                    viewModel.bookDinnerCateringService(
                                        title = "Help Desk: " + selectedProduct.name,
                                        eventNumber = "HLP1",
                                        guests = 1,
                                        totalPrice = selectedProduct.price,
                                        contactPhone = "999999999",
                                        entree = "Standard Selection",
                                        sides = "Standard Selection",
                                        dessert = "Standard Selection",
                                        extraInfo = "Inquiry from Explorer Help Desk: " + selectedProduct.id
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("help_action_btn")
                    ) {
                        Text(
                            text = when(selectedCategory.id) {
                                "cat_food", "cat_fruits" -> "Add to Cart 🛒"
                                "cat_hospital" -> "Locate Clinic 🏥"
                                else -> "Request Quote 🎪"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun getFoodDrawableRes(name: String): Int {
    return when {
        name.contains("Jollof", ignoreCase = true) -> com.example.R.drawable.img_food_jollof
        name.contains("Yam Fries", ignoreCase = true) || name.contains("Yam", ignoreCase = true) && name.contains("fries", ignoreCase = true) -> com.example.R.drawable.img_food_yam_fries
        name.contains("Pounded Yam", ignoreCase = true) || name.contains("Egusi", ignoreCase = true) -> com.example.R.drawable.img_food_pounded_yam
        name.contains("Suya", ignoreCase = true) || name.contains("Burger", ignoreCase = true) -> com.example.R.drawable.img_food_suya_burger
        name.contains("Fruit", ignoreCase = true) || name.contains("Mango", ignoreCase = true) || name.contains("Smoothie", ignoreCase = true) || name.contains("Pawpaw", ignoreCase = true) || name.contains("Pineapple", ignoreCase = true) || name.contains("Avocado", ignoreCase = true) || name.contains("Tangerine", ignoreCase = true) || name.contains("Lemon", ignoreCase = true) -> com.example.R.drawable.img_food_fruits_platter
        else -> com.example.R.drawable.img_food_fruits_platter
    }
}
