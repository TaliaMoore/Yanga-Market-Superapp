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

    var selectedTab by remember { mutableStateOf(0) } // 0: Food & Fruits, 1: Retail, 2: Dine-In

    // Cart Sheet State
    var showCartDialog by remember { mutableStateOf(false) }

    val cartCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.price * it.quantity }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Header ---
            YangaHeader(
                title = "Yanga Directory 🎪",
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
                    text = { Text("Eats & Fruits 🍉", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) },
                    selectedContentColor = PrimaryPurple,
                    unselectedContentColor = CharcoalBlack.copy(alpha = 0.5f)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Retail Shops 👕", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) },
                    selectedContentColor = PrimaryPurple,
                    unselectedContentColor = CharcoalBlack.copy(alpha = 0.5f)
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Restaurants 🍝", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) },
                    selectedContentColor = PrimaryPurple,
                    unselectedContentColor = CharcoalBlack.copy(alpha = 0.5f)
                )
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
                        // --- Category: Foods ---
                        item {
                            ListCategoryTitle(text = "African Food Offerings 🥘")
                        }
                        if (foods.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(foods) { food ->
                                ShoppingItemCard(
                                    name = food.name,
                                    price = food.price,
                                    category = food.category,
                                    desc = food.description,
                                    isFruit = false,
                                    onAdd = { viewModel.addToCart(food.name, food.price, food.category, "FOOD") }
                                )
                            }
                        }

                        // --- Category: Fruits ---
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            ListCategoryTitle(text = "Organic Fresh Fruits 🍍")
                        }
                        if (fruits.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(fruits) { fruit ->
                                ShoppingItemCard(
                                    name = fruit.name,
                                    price = fruit.price,
                                    category = fruit.category,
                                    desc = fruit.description,
                                    isFruit = true,
                                    onAdd = { viewModel.addToCart(fruit.name, fruit.price, fruit.category, "FRUIT") }
                                )
                            }
                        }
                    }

                    1 -> {
                        item {
                            ListCategoryTitle(text = "Local Retail Outlets & Grocers 🛍️")
                        }
                        if (shops.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(shops) { shop ->
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
                        if (restaurants.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(restaurants) { r ->
                                RestaurantBookingCard(restaurant = r, onReserve = { time ->
                                    viewModel.reserveRestaurantTable(r, time)
                                })
                            }
                        }
                    }
                }
            }
        }

        // --- Bottom Cart Notification Sticker bar ---
        if (cartCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .fillMaxWidth()
                    .testTag("cart_preview_bar")
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SecondaryYellow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                        .clickable { showCartDialog = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Checkout Basket Open!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CharcoalBlack
                                )
                                Text(
                                    text = "Tap to review items in your shopping list",
                                    fontSize = 10.sp,
                                    color = CharcoalBlack.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₦${String.format("%,.2f", cartSubtotal)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Arrow right", tint = PrimaryPurple)
                        }
                    }
                }
            }
        }

        // --- Custom Interactive Shopping Cart Dialogue Drawer ---
        if (showCartDialog) {
            AlertDialog(
                onDismissRequest = { showCartDialog = false },
                confirmButton = {
                    YangaFunButton(
                        text = "Pay ₦${String.format("%,.0f", cartSubtotal)} with Wallet",
                        onClick = {
                            viewModel.checkoutCart()
                            showCartDialog = false
                        },
                        containerColor = PrimaryPurple,
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth().testTag("cart_checkout_pay_btn")
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCartDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add more items +", fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Shopping Basket 🛒", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        IconButton(onClick = { showCartDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close cart")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        Text(
                            text = "Yanga pay secure transaction verification logic is loaded. Check your balance before ordering.",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Divider(color = PrimaryPurple.copy(alpha = 0.12f))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cartItems) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalBlack,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "₦${String.format("%,.2f", item.price)} each",
                                            fontSize = 10.sp,
                                            color = CharcoalBlack.copy(alpha = 0.5f)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.modifyCartQuantity(item, -1) }, modifier = Modifier.size(28.dp)) {
                                            Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Minus", tint = PrimaryPurple)
                                        }
                                        Text(
                                            text = item.quantity.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(onClick = { viewModel.modifyCartQuantity(item, 1) }, modifier = Modifier.size(28.dp)) {
                                            Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Plus", tint = PrimaryPurple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .border(3.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                    .testTag("shopping_cart_dialog")
            )
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
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                YangaBadge(
                    text = category,
                    containerColor = if (isFruit) Color(0xFFDCFCE7) else SecondaryYellow,
                    contentColor = if (isFruit) Color(0xFF15803D) else PrimaryPurple
                )
                Text(
                    text = "₦${String.format("%,.0f", price)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBlack
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = CharcoalBlack.copy(alpha = 0.60f),
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = if (isFruit) Color(0xFF22C55E) else PrimaryPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(34.dp)
                    .border(1.dp, if (isFruit) Color(0xFF15803D) else PrimaryPurple, RoundedCornerShape(10.dp))
                    .testTag("add_item_${name.replace(" ", "_")}")
            ) {
                Text(
                    text = "Add to Cart +",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
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
            .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
    ) {
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalBlack
                        )
                        Text(
                            text = "${shop.specialty} • ${shop.distanceKm}km away",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
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
                Divider(color = PrimaryPurple.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(6.dp))
                shop.items.forEach { retailItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₦${String.format("%,.0f", retailItem.price)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalBlack
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { onAddItem(retailItem.name, retailItem.price, retailItem.category) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .border(1.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                                    .testTag("add_item_${retailItem.name.replace(" ", "_")}")
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
fun RestaurantBookingCard(
    restaurant: com.example.domain.model.Restaurant,
    onReserve: (String) -> Unit
) {
    val times = listOf("12:00 PM", "3:30 PM", "7:00 PM")
    var selectedTime by remember { mutableStateOf("7:00 PM") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = restaurant.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack
                    )
                    Text(
                        text = "Cuisine: ${restaurant.cuisine} • ⭐ ${restaurant.rating}",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Table: ₦${String.format("%,.0f", restaurant.tablePrice)}",
                    fontSize = 13.sp,
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
                        .testTag("reserve_btn_${restaurant.name.replace(" ", "_")}")
                ) {
                    Text("Book Table", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
