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
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Help Guide 💡", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
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
                            items(foods, key = { it.name }) { food ->
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
                            Spacer(modifier = Modifier.height(16.dp))
                            YangaVisuallyDistinctSection(
                                title = "GraphQL Declarative Field Query ⚡",
                                subtitle = "Toggle query fields to selectively reduce response payload size under GraphQL specifications:",
                                headerBadgeText = "REAL-TIME",
                                headerBadgeColor = Color(0xFFEFF6FF),
                                backgroundColor = Color(0xFFF9FAFB),
                                borderColor = PrimaryPurple.copy(alpha = 0.5f),
                                borderWidth = 2.0,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(2.dp))

                                // Active Selectors Flow Layout (Chowdeck Neobrutalist design specs)
                                val selectedFields by viewModel.selectedFruitFields.collectAsState()
                                val currentQuery by viewModel.currentFruitsQuery.collectAsState()

                                val fieldsList = listOf("name", "price", "category", "description")

                                YangaFlowButtonsLayout(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    fieldsList.forEach { field ->
                                        val isRequired = field == "name" || field == "price"
                                        val isChecked = selectedFields.contains(field)

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable(enabled = !isRequired) {
                                                    val newList = if (isChecked) {
                                                        selectedFields.filter { it != field }
                                                    } else {
                                                        selectedFields + field
                                                    }
                                                    viewModel.queryFruitsDeclaratively(newList)
                                                }
                                                .padding(4.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = if (isRequired) null else { checked ->
                                                    val newList = if (!checked) {
                                                        selectedFields.filter { it != field }
                                                    } else {
                                                        selectedFields + field
                                                    }
                                                    viewModel.queryFruitsDeclaratively(newList)
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = PrimaryPurple,
                                                    checkmarkColor = Color.White
                                                ),
                                                modifier = Modifier.size(24.dp).testTag("field_checkbox_$field")
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = field.replaceFirstChar { it.uppercase() },
                                                fontSize = 12.sp,
                                                fontWeight = if (isRequired) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isRequired) CharcoalBlack else CharcoalBlack.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Calculated simulated payload impact
                                    val schemaSizeFactor = when (selectedFields.size) {
                                        2 -> "0.45 KB (Optimized 📉 -60%)"
                                        3 -> "0.78 KB (Standard 📊 -30%)"
                                        else -> "1.12 KB (Full Payload 🛑 100%)"
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Estimated Payload Weight:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalBlack
                                        )
                                        Text(
                                            text = schemaSizeFactor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (selectedFields.size == 2) Color(0xFF16A34A) else Color(0xFFD97706)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // GraphQL Query Preview Block
                                    Text(
                                        text = "Declarative GraphQL payload queried:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = currentQuery,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF4ADE80),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            Spacer(modifier = Modifier.height(16.dp))
                            ListCategoryTitle(text = "Organic Fresh Fruits 🍍")
                        }
                        if (fruits.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(fruits, key = { it.name }) { fruit ->
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
                        item {
                            FacilitySpaceInquiryCard(viewModel = viewModel)
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (shops.isEmpty()) {
                            item { LoaderPlaceholder() }
                        } else {
                            items(shops, key = { it.name }) { shop ->
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
                            items(restaurants, key = { it.name }) { r ->
                                RestaurantBookingCard(restaurant = r, onReserve = { time ->
                                    viewModel.reserveRestaurantTable(r, time)
                                })
                            }
                        }
                    }

                    3 -> {
                        item {
                            YangaMultiColumnProductHelpToolSection(viewModel = viewModel)
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
                            items(cartItems, key = { it.id }) { item ->
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
                            text = "No category queried (Optimized)",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = if (price > 0) "₦${String.format("%,.0f", price)}" else "No price queried",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBlack
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (name.isNotBlank()) name else "No name queried",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
            val finalDesc = if (desc.isNotBlank()) desc else "Description omitted to save network payload (GraphQL specifications)."
            Text(
                text = finalDesc,
                fontSize = 11.sp,
                color = if (desc.isNotBlank()) CharcoalBlack.copy(alpha = 0.60f) else Color(0xFF15803D).copy(alpha = 0.8f),
                fontWeight = if (desc.isNotBlank()) FontWeight.Medium else FontWeight.SemiBold,
                style = if (desc.isNotBlank()) MaterialTheme.typography.bodyMedium else androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
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
