package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SavedBookingEntity
import com.example.ui.MainViewModel
import com.example.ui.components.YangaBadge
import com.example.ui.components.YangaFunButton
import com.example.ui.components.YangaPlayfulCard
import com.example.ui.theme.*
import androidx.compose.animation.core.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.delay


@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.walletBalance.collectAsState()
    val bookings by viewModel.savedBookings.collectAsState()

    val globalUserName by viewModel.userName.collectAsState()
    val globalUserLocation by viewModel.userLocation.collectAsState()

    // Interactive UI States
    var selectedAddress by remember { mutableStateOf("") }
    LaunchedEffect(globalUserLocation) {
        selectedAddress = if (globalUserLocation.isNotEmpty()) globalUserLocation else "Lekki Phase 1, Lagos, Nigeria"
    }
    var showAddressDialog by remember { mutableStateOf(false) }
    var addressInput by remember { mutableStateOf("") }
    
    var showHowItWorks by remember { mutableStateOf(false) }
    var selectedPromoForPage by remember { mutableStateOf<String?>(null) }
    
    // Yanga Ride Interactive state
    var selectedVehicle by remember { mutableStateOf("Bike") } // Bike, Keke, Cab
    var bookingStatusMessage by remember { mutableStateOf<String?>(null) }
    var rsvpSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Hot Promos & Coupon Deals
    val promosAndCoupons = listOf(
        "🎟️ Promo: 25% DISCOUNT on 'Lagos Vibes Concert' event limit slots!",
        "🏷️ Coupon 'YANGAOFF' for ₦1,500 off at Choice Supermarket!",
        "🎁 Deal: FREE local drinks coming up this Friday Fiesta!",
        "🍕 Promo: Buy 1 Get 1 FREE on all sizes at Pizza Corner!",
        "🍒 Coupon: Get 20% discount on organic fresh fruits today!",
        "🎫 Flash Sale: 40% OFF all VIP Event table reservations!"
    )

    val promoListState = rememberLazyListState()
    LaunchedEffect(Unit) {
        var index = 0
        while (true) {
            delay(3500)
            index = (index + 1) % promosAndCoupons.size
            try {
                promoListState.animateScrollToItem(index)
            } catch (e: Exception) {
                // Ignore pre-layout scroll requests securely
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PlayfulBg),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- 1. LIVE LAGOON MARKET DEALS & PROMOS ---
        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF97316), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HOT PROMOS 🔥",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow(
                            state = promoListState,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(promosAndCoupons) { promo ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                                        .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                                        .clickable { selectedPromoForPage = promo }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("promo_item_${promo.take(20).replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = promo,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                    Divider(color = PrimaryPurple.copy(alpha = 0.15f), thickness = 1.5.dp)
                }
            }
        }

        // --- 2. HERO BANNER ("Everything You Need, Delivered Together") ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = if (globalUserName.isNotEmpty()) globalUserName else "Guest"
                    Text(
                        text = "Hello, $displayName! 👋",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF97316)
                    )
                }
                // Main Header Title with high-contrast playful typography
                Text(
                    text = "Everything You Need,\nDelivered Together",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple,
                    lineHeight = 34.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "From daily foodstuffs & hot meals to medical centers and premium events, Yanga has Lagos fully covered.",
                    fontSize = 13.sp,
                    color = CharcoalBlack.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Address Input Bar
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
                        .clickable {
                            addressInput = selectedAddress
                            showAddressDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location Pin",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedAddress,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBlack,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigate("market") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                            .testTag("hero_order_now_btn")
                    ) {
                        Text(
                            text = "Order Now 🚀",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { showHowItWorks = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "How it works icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "How It Works",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 2.5 LET'S SHARE BULK BUY ---
        item {
            DecorativeFloatingFoodRow(onNavigate = onNavigate)
        }

        // --- 4. YANGAMARKET DIRECTORY SECTION (Grid Match) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YangaMarket 🛒",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Your everyday needs, delivered fast",
                            fontSize = 12.sp,
                            color = CharcoalBlack.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "See all",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple,
                        modifier = Modifier
                            .clickable { onNavigate("market") }
                            .padding(4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Beautifully organized 2x3 grid imitating Chowdeck's category selectors
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryGridCard(
                            emoji = "🍔",
                            title = "Restaurants",
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setDashboardMarketFilter("None")
                                    onNavigate("market")
                                }
                        )
                        CategoryGridCard(
                            emoji = "🛒",
                            title = "Supermarkets",
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setDashboardMarketFilter("Supermarket")
                                    onNavigate("market")
                                }
                        )
                        CategoryGridCard(
                            emoji = "💊",
                            title = "Pharmacies",
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("hospitals") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryGridCard(
                            emoji = "🍞",
                            title = "Bakeries",
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setDashboardMarketFilter("Bakery")
                                    onNavigate("market")
                                }
                        )
                        CategoryGridCard(
                            emoji = "🏥",
                            title = "Care",
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("hospitals") }
                        )
                        CategoryGridCard(
                            emoji = "🎟️",
                            title = "Events",
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("events") }
                        )
                    }
                }
            }
        }

        // --- 5. MARKET INSIGHT AND VIBES ---
        item {
            val quotesLib by viewModel.quotesLibrary.collectAsState()
            val availableCategories = listOf("All", "Fashion", "Electronics", "Groceries", "Business", "Motivation")
            
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = "Market Insight and Vibes 💡",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                Text(
                    text = "Dynamic merchant professional thoughts served directly by superapp community",
                    fontSize = 12.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Filter chip row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items(availableCategories, key = { it }) { cat ->
                        val isSelected = (quotesLib?.category ?: "All").equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.fetchQuotesByCategory(cat) },
                            label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White,
                                containerColor = SecondaryYellow.copy(alpha = 0.25f),
                                labelColor = PrimaryPurple
                            ),
                            modifier = Modifier.testTag("quote_filter_chip_$cat")
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Quotes List
                quotesLib?.let { library ->
                    if (library.quotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(WarmCardWhite, RoundedCornerShape(12.dp))
                                .border(1.5.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No quotes found for this category.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(library.quotes, key = { it.id }) { quote ->
                                YangaPlayfulCard(
                                    backgroundColor = Color.White,
                                    borderColor = PrimaryPurple,
                                    borderWidth = 2.0,
                                    modifier = Modifier
                                        .width(280.dp)
                                        .height(130.dp)
                                        .testTag("quote_card_${quote.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "“${quote.text}”",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CharcoalBlack,
                                            maxLines = 3
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "— ${quote.author}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PrimaryPurple
                                            )
                                            YangaBadge(
                                                text = quote.category,
                                                containerColor = SecondaryYellow,
                                                contentColor = PrimaryPurple
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GraphQL API connection established: Loaded ${library.totalCount} vibes successfully",
                        fontSize = 10.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // --- 6. FAST, RELIABLE & ALWAYS ON THE MOVE (TIMELINE & INTERACTIVE BOOKING) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Fast, Reliable & Always on the Move ⚡",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBlack
                )
                Text(
                    text = "We deliver with modern speed and premium secure care",
                    fontSize = 12.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timeline component imitating transport steps from the image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bike selector
                            TimelineStep(
                                emoji = "🛵",
                                name = "Bike Rider",
                                selected = selectedVehicle == "Bike",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedVehicle = "Bike"
                                        bookingStatusMessage = null
                                    }
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            // Keke selector
                            TimelineStep(
                                emoji = "🛺",
                                name = "Yanga Keke",
                                selected = selectedVehicle == "Keke",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedVehicle = "Keke"
                                        bookingStatusMessage = null
                                    }
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            // Cab selector
                            TimelineStep(
                                emoji = "🚗",
                                name = "Yanga Ride",
                                selected = selectedVehicle == "Cab",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedVehicle = "Cab"
                                        bookingStatusMessage = null
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = PrimaryPurple.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Ride booking details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Yanga logistics option: $selectedVehicle",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryPurple
                                )
                                Text(
                                    text = when(selectedVehicle) {
                                        "Bike" -> "Best for nimble grocery deliveries (₦500)"
                                        "Keke" -> "Best for heavy foodstuffs & party barrels (₦1,200)"
                                        else -> "Premium air-conditioned superapp transit (₦2,500)"
                                    },
                                    fontSize = 11.sp,
                                    color = CharcoalBlack.copy(alpha = 0.60f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onNavigate("yanga_rider")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("dashboard_book_ride_btn")
                        ) {
                            Text(
                                text = "Book a $selectedVehicle Now →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        bookingStatusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (msg.contains("Success")) Color(0xFFEFF6FF) else Color(0xFFFEF2F2),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.contains("Success")) PrimaryPurple else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 7. ACTIVE TICKETS & DISCOVERY LEDGER ROW ---
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Reservations & Tickets 🎫",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                    if (bookings.isNotEmpty()) {
                        YangaBadge(
                            text = "${bookings.size} Active",
                            containerColor = PrimaryPurple,
                            contentColor = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                if (bookings.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = "No active bookings",
                                    tint = PrimaryPurple.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Book a dine-in restaurant table, RSVP for a musical concert, or schedule an medical spot to track tickets directly on your ledger.",
                                    fontSize = 11.sp,
                                    color = CharcoalBlack.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(bookings, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                onCancel = { viewModel.cancelActiveBooking(booking.id) }
                            )
                        }
                    }
                }
            }
        }

        // --- 8. VALUE STATEMENT GRAPHICS ROW (CHOWDECK VALUE STATEMENTS) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Divider(color = PrimaryPurple.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ValuePropBox(
                        emoji = "🛡️",
                        title = "Safe & Secure",
                        desc = "Your security & database reliability is our priority",
                        background = Color(0xFFF9FAF7),
                        modifier = Modifier.weight(1f)
                    )
                    ValuePropBox(
                        emoji = "⏰",
                        title = "On-time Delivery",
                        desc = "Quick, dependable and tracked live across Lagos",
                        background = Color(0xFFFEFBF3),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ValuePropBox(
                        emoji = "⭐",
                        title = "Top Quality",
                        desc = "Vetted vendors, fresh ingredients & fully active places",
                        background = Color(0xFFFBF9FF),
                        modifier = Modifier.weight(1f)
                    )
                    ValuePropBox(
                        emoji = "🎧",
                        title = "24/7 Support",
                        desc = "Prompt friendly customer team ready to reply instantly",
                        background = Color(0xFFEFFBFE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }




    }


    // --- Interactive Dialogue Modal: Address Picker ---
    if (showAddressDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddressDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "📍 Set Location Address",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Lagos operations run coordinates in Lekki, Victoria Island, Ikeja, Surulere, and Lagos Island.",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    TextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        placeholder = { Text("E.g., Admiralty Way, Lekki Phase 1") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAF7),
                            unfocusedContainerColor = Color(0xFFF9FAF7)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddressDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Black, color = CharcoalBlack)
                        }
                        Button(
                            onClick = {
                                if (addressInput.isNotBlank()) {
                                    selectedAddress = addressInput.trim()
                                }
                                showAddressDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- Interactive Dialogue Modal: How It Works ---
    if (showHowItWorks) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showHowItWorks = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, PrimaryPurple, RoundedCornerShape(18.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🎪 Superapp Lifecycle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Yanga Superapp connects you with top-tier services using secure transactions:",
                        fontSize = 12.sp,
                        color = CharcoalBlack,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🎒", fontSize = 20.sp)
                        Column {
                            Text("Secure Coin Purse", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            Text("Tracks Gold & Silver pieces (1 GP = 100 SP) to purchase anything on-demand instantly.", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🛒", fontSize = 20.sp)
                        Column {
                            Text("Market & LetsShare Categories", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            Text("Order meals, buy retail apparel, or rent party goods from people around Lagos.", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🏥", fontSize = 20.sp)
                        Column {
                            Text("Care Discovery Directory", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            Text("Instantly search verified local hospitals, check services offered, and RSVP for time slots.", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                        }
                    }

                    Button(
                        onClick = { showHowItWorks = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Awesome, Got It!", fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }

    // --- Promo Page Dialog / Overlay ---
    if (selectedPromoForPage != null) {
        val promo = selectedPromoForPage!!
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedPromoForPage = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)), // Pale purple background
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(3.dp, PrimaryPurple, RoundedCornerShape(24.dp))
                    .testTag("promo_details_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎟️ Yanga Hot Deal!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        IconButton(
                            onClick = { selectedPromoForPage = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Promo Details",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Graphic Card representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(SecondaryYellow.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                promo.contains("Concert", ignoreCase = true) -> "🎸🔥"
                                promo.contains("Choice Supermarket", ignoreCase = true) -> "🛒🎒"
                                promo.contains("Friday Fiesta", ignoreCase = true) -> "🍹🎉"
                                promo.contains("Pizza Corner", ignoreCase = true) -> "🍕😋"
                                promo.contains("organic fresh fruits", ignoreCase = true) -> "🍒🍓"
                                else -> "🎫⚡"
                            },
                            fontSize = 56.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = promo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val couponCode = when {
                        promo.contains("Concert", ignoreCase = true) -> "VIBES25"
                        promo.contains("Choice Supermarket", ignoreCase = true) -> "YANGAOFF"
                        promo.contains("Friday Fiesta", ignoreCase = true) -> "FIESTA"
                        promo.contains("Pizza Corner", ignoreCase = true) -> "PIZZA1G1"
                        promo.contains("organic fresh fruits", ignoreCase = true) -> "FRUITY"
                        else -> "YNG40"
                    }

                    // Coupon claim panel
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("COUPON CODE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(couponCode, fontSize = 16.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                            }
                            Button(
                                onClick = {
                                    viewModel.postSuccess("Coupon '$couponCode' claimed successfully! Applied to your next checkout. 💜")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Exclusively on Yanga Market. Limited slots are available for active Yanga citizens. Terms and conditions apply.",
                        fontSize = 9.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val targetRoute = when {
                        promo.contains("Concert", ignoreCase = true) || promo.contains("Event", ignoreCase = true) -> "events"
                        else -> "market"
                    }
                    val buttonLabel = if (targetRoute == "events") "Go to Events Page 🎸" else "Go to Market Page 🛒"

                    Button(
                        onClick = {
                            selectedPromoForPage = null
                            onNavigate(targetRoute)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(buttonLabel, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}

// Data class for Marquee
data class TickerItem(
    val name: String,
    val price: String,
    val isUp: Boolean
)

@Composable
fun CategoryGridCard(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier
) {
    // Elegant neo-brutalist / soft 3D highlight under the square card
    Box(
        modifier = modifier
            .height(86.dp)
            .background(PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (-3).dp, y = (-3).dp) // Shift up-left to create the 3D bottom-right highlight peek
                .border(1.dp, PrimaryPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PlayfulCream, CircleShape)
                        .border(1.dp, SecondaryYellow, CircleShape),  // Highlight on the circle icon as well!
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TimelineStep(
    emoji: String,
    name: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(6.dp)
            .background(
                if (selected) SecondaryYellow.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (selected) SecondaryYellow else Color(0xFFF3F4F6),
                    CircleShape
                )
                .border(2.dp, if (selected) PrimaryPurple else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            color = if (selected) PrimaryPurple else CharcoalBlack.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ValuePropBox(
    emoji: String,
    title: String,
    desc: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = emoji, fontSize = 16.sp)
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
            }
            Text(
                text = desc,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = CharcoalBlack.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ServiceGridItem(
    title: String,
    desc: String,
    icon: ImageVector,
    tag: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
            .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
            .height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.5.dp, PrimaryPurple, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                }
                YangaBadge(text = tag, containerColor = Color.White, contentColor = PrimaryPurple)
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = CharcoalBlack.copy(alpha = 0.70f),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: SavedBookingEntity,
    onCancel: () -> Unit
) {
    val badgeColor = when (booking.bookingType) {
        "HOSPITAL" -> Color(0xFFE0F2FE)
        "EVENT" -> Color(0xFFF3E8FF)
        else -> Color(0xFFFFFEE5)
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(260.dp)
            .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                YangaBadge(
                    text = booking.bookingType,
                    containerColor = badgeColor,
                    contentColor = PrimaryPurple
                )
                IconButton(onClick = onCancel, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel reservation",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = booking.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack,
                maxLines = 1
            )
            Text(
                text = booking.subtitle,
                fontSize = 11.sp,
                color = CharcoalBlack.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = PrimaryPurple.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.dateOrTime,
                    fontSize = 10.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (booking.price > 0.0) "₦${String.format("%,.0f", booking.price)}" else "FREE RSVP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBlack
                )
            }
        }
    }
}

@Composable
fun CSSKeyframeAnimationSimulatorSection(
    modifier: Modifier = Modifier
) {
    var selectedAnimTab by remember { mutableStateOf("resize") }
    var isPlaying by remember { mutableStateOf(true) }
    var manualProgress by remember { mutableStateOf(0.5f) }

    // Create play progress using infinite transition if playing, else manual scrubbing.
    val progress: Float
    if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "css_transition")
        progress = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "progress"
        ).value
    } else {
        progress = manualProgress
    }

    YangaPlayfulCard(
        backgroundColor = Color(0xFFFAF5FF), // pale purple
        borderColor = PrimaryPurple,
        borderWidth = 3.0,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("css_keyframe_simulator_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                YangaBadge(
                    text = "CSS KEYFRAMES STUDIO 🎬💫",
                    containerColor = SecondaryYellow,
                    contentColor = PrimaryPurple
                )
                YangaBadge(
                    text = "SPEC-COMPLIANT",
                    containerColor = Color(0xFFF0FDF4),
                    contentColor = Color(0xFF15803D)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Dynamic Motion Designer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = CharcoalBlack
            )
            Text(
                text = "Specifying changes in height, width, and position from starter keypoints to end destinations.",
                fontSize = 12.sp,
                color = CharcoalBlack.copy(alpha = 0.6f),
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // TAB CHIPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "resize" to "Resize 📐",
                    "move" to "Move 🚀",
                    "composite" to "Composite ☄️"
                ).forEach { (id, label) ->
                    val isSelected = selectedAnimTab == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryPurple else Color.White)
                            .border(1.5.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                            .clickable { selectedAnimTab = id }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else PrimaryPurple,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Render animated custom box state variables
            var boxColor = Color.LightGray
            var boxWidth = 100.dp
            var boxHeight = 100.dp
            var boxOffsetX = 0.dp
            var boxOffsetY = 0.dp

            // ANIMATION CANVAS VIEWER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                // Background grid indicators
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        Divider(color = PrimaryPurple.copy(alpha = 0.04f))
                    }
                }


                when (selectedAnimTab) {
                    "resize" -> {
                        // 0% -> width 100, height 100, color #FEF08A
                        // 100% -> width 200, height 150, color #FF6D00
                        boxWidth = lerpDp(100.dp, 200.dp, progress)
                        boxHeight = lerpDp(100.dp, 150.dp, progress)
                        boxColor = lerpColor(Color(0xFFFEF08A), Color(0xFFFF6D00), progress)
                        boxOffsetX = 0.dp
                        boxOffsetY = 0.dp
                    }
                    "move" -> {
                        // 0% -> left 0, top 0
                        // 100% -> left 150, top 80
                        boxWidth = 120.dp
                        boxHeight = 120.dp
                        boxColor = PrimaryPurple
                        boxOffsetX = lerpDp(0.dp, 150.dp, progress)
                        boxOffsetY = lerpDp(0.dp, 40.dp, progress) // Scaled slightly for compact viewport bounds
                    }
                    else -> {
                        // composite (0% / 50% / 100%)
                        if (progress < 0.5f) {
                            val p = progress * 2f
                            boxWidth = lerpDp(80.dp, 140.dp, p)
                            boxHeight = lerpDp(80.dp, 110.dp, p)
                            boxOffsetX = lerpDp(0.dp, 80.dp, p)
                            boxOffsetY = lerpDp(0.dp, -15.dp, p)
                            boxColor = lerpColor(PrimaryPurple, Color(0xFFFF6D00), p)
                        } else {
                            val p = (progress - 0.5f) * 2f
                            boxWidth = lerpDp(140.dp, 200.dp, p)
                            boxHeight = lerpDp(110.dp, 140.dp, p)
                            boxOffsetX = lerpDp(80.dp, 150.dp, p)
                            boxOffsetY = lerpDp(-15.dp, 25.dp, p)
                            boxColor = lerpColor(Color(0xFFFF6D00), Color(0xFF00C853), p)
                        }
                    }
                }

                // Render Live Object
                Box(
                    modifier = Modifier
                        .offset(x = boxOffsetX, y = boxOffsetY)
                        .size(width = boxWidth, height = boxHeight)
                        .background(boxColor, RoundedCornerShape(8.dp))
                        .border(1.5.dp, CharcoalBlack, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (selectedAnimTab) {
                                "resize" -> "📏 RESIZING"
                                "move" -> "🚀 MOVING"
                                else -> "☄️ COMPOSITE"
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (boxColor == Color(0xFFFEF08A)) PrimaryPurple else Color.White
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (boxColor == Color(0xFFFEF08A)) PrimaryPurple else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CONTROLLER SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Button(
                    onClick = { isPlaying = !isPlaying },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("css_play_pause_btn")
                ) {
                    Text(
                        text = if (isPlaying) "⏸️ Pause Engine" else "▶️ Start Engine",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Scrub label
                Text(
                    text = "Timeline Scrub: ${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TIMELINE SCRUB SLIDER
            Slider(
                value = progress,
                onValueChange = {
                    isPlaying = false
                    manualProgress = it
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryPurple,
                    activeTrackColor = PrimaryPurple,
                    inactiveTrackColor = PrimaryPurple.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("css_timeline_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // METADATA TELEMETRY CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "LIVE RENDERED SPECIFICATIONS (W3C INTERNALS)",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        specItem("Width", "${boxWidth.value.toInt()}dp", "Start: ${if (selectedAnimTab == "resize") 100 else if (selectedAnimTab == "move") 120 else 80}dp", "End: 200dp")
                        specItem("Height", "${boxHeight.value.toInt()}dp", "Start: ${if (selectedAnimTab == "resize") 100 else if (selectedAnimTab == "move") 120 else 80}dp", "End: ${if (selectedAnimTab == "resize") 150 else if (selectedAnimTab == "move") 120 else 140}dp")
                        specItem("Position Offset", "${boxOffsetX.value.toInt()}x${boxOffsetY.value.toInt()}dp", "Start: 0x0dp", "End: ${if (selectedAnimTab == "resize") "0x0" else if (selectedAnimTab == "move") "150x40" else "150x25"}dp")
                    }
                }
            }


            Spacer(modifier = Modifier.height(14.dp))

            // RAW CSS SOURCE BLOCK
            Text(
                text = "Corresponding CSS keyframes code in /assets/yanga_style.css:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalBlack.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = getCSSCodeForTab(selectedAnimTab),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFF8FAFC),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

private fun lerpDp(start: Dp, end: Dp, fraction: Float): Dp {
    return start + (end - start) * fraction
}

private fun lerpColor(startColor: Color, endColor: Color, fraction: Float): Color {
    val r = startColor.red + (endColor.red - startColor.red) * fraction
    val g = startColor.green + (endColor.green - startColor.green) * fraction
    val b = startColor.blue + (endColor.blue - startColor.blue) * fraction
    val a = startColor.alpha + (endColor.alpha - startColor.alpha) * fraction
    return Color(r, g, b, a)
}

@Composable
private fun specItem(label: String, current: String, startLabel: String, endLabel: String) {
    Column {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
        Text(text = current, fontSize = 12.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
        Text(text = startLabel, fontSize = 8.sp, color = CharcoalBlack.copy(alpha = 0.4f))
        Text(text = endLabel, fontSize = 8.sp, color = CharcoalBlack.copy(alpha = 0.4f))
    }
}

private fun getCSSCodeForTab(tab: String): String {
    return when (tab) {
        "resize" -> """
@keyframes resize {
  0% {
    width: 100px;
    height: 100px;
    background-color: #FEF08A; /* Pale Yellow */
  }
  100% {
    width: 200px;
    height: 150px;
    background-color: #FF6D00; /* Appetizing Orange */
  }
}

.yanga-animate-resize {
  animation: resize 3s infinite alternate ease-in-out;
}
        """.trimIndent()
        "move" -> """
@keyframes move {
  0% {
    left: 0px;
    top: 0px;
  }
  100% {
    left: 150px;
    top: 80px;
  }
}

.yanga-animate-move {
  position: relative;
  animation: move 4s infinite alternate cubic-bezier(0.4, 0, 0.2, 1);
}
        """.trimIndent()
        else -> """
@keyframes resizeAndMove {
  0% {
    width: 80px;
    height: 80px;
    left: 0px;
    top: 0px;
    background-color: #7E22CE; /* Primary Purple */
  }
  50% {
    width: 140px;
    height: 110px;
    left: 80px;
    top: -20px;
    background-color: #FF6D00;
  }
  100% {
    width: 200px;
    height: 140px;
    left: 160px;
    top: 40px;
    background-color: #00C853; /* Success Green */
  }
}

.yanga-animate-composite {
  position: relative;
  animation: resizeAndMove 5s infinite alternate ease-in-out;
}
        """.trimIndent()
    }
}

@Composable
fun DecorativeFloatingFoodRow(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // We create multiple independent infinite transitions to simulate different floating speeds & offsets.
    // This replicates CSS 'animation-iteration-count: infinite' playing continuously.
    val infinite1 = rememberInfiniteTransition(label = "float_fast")
    val infinite2 = rememberInfiniteTransition(label = "float_medium")
    val infinite3 = rememberInfiniteTransition(label = "float_slow")

    val floatY1 by infinite1.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y1"
    )

    val floatY2 by infinite2.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y2"
    )

    val floatY3 by infinite3.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y3"
    )

    val rotate1 by infinite1.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "r1"
    )

    val rotate2 by infinite2.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "r2"
    )

    data class BulkItemFloating(
        val emoji: String,
        val label: String,
        val priceText: String,
        val route: String,
        val animY: Float,
        val animRot: Float
    )

    val bulkItems = listOf(
        BulkItemFloating("🧺", "5K Fruits", "₦5,000", "bulkbuy5k", floatY1, rotate1),
        BulkItemFloating("🛍️", "10K Fruits", "₦10,000", "bulkbuy10k", floatY2, rotate2),
        BulkItemFloating("🌾", "15K Grains", "₦15,000", "bulkbuy15k", floatY3, rotate1),
        BulkItemFloating("📦", "30K Pack", "₦30,000", "bulkbuy30k", floatY1, rotate2)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("decorative_floating_shelf")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Let's Share Bulk buy 🎒✨",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                // Fun mini badge
                Box(
                    modifier = Modifier
                        .background(SecondaryYellow, RoundedCornerShape(4.dp))
                        .border(1.dp, PrimaryPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TAP TO SEE INSIDE 🔍",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                }
            }
            Text(
                text = "Group-Splits",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalBlack.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bulkItems) { item ->
                // Custom subtle highlight under the floating item card
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(104.dp)
                        .background(PrimaryPurple.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = (-2).dp, y = (-2).dp) // Shift up-left to expose shadow highlight
                            .graphicsLayer {
                                translationY = item.animY * density
                                rotationZ = item.animRot
                            }
                            .clickable { onNavigate(item.route) }
                            .border(1.dp, PrimaryPurple.copy(alpha = 0.10f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(PlayfulCream, CircleShape)
                                    .border(1.dp, SecondaryYellow, CircleShape), // yellow border highlight on circle accent
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = item.priceText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}





