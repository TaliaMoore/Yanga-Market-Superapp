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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.walletBalance.collectAsState()
    val bookings by viewModel.savedBookings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Playful Welcome Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Yanga Market! 🎪",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Eku Abo! Welcome to the Superapp",
                        fontSize = 14.sp,
                        color = CharcoalBlack.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondaryYellow)
                        .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                        .clickable { onNavigate("vibes") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Vibes shortcut",
                        tint = PrimaryPurple
                    )
                }
            }
        }

        // --- Interactive Wallet Info Widget ---
        item {
            YangaPlayfulCard(
                backgroundColor = SecondaryYellow,
                borderColor = PrimaryPurple,
                borderWidth = 3.0,
                modifier = Modifier.fillMaxWidth().testTag("home_wallet_widget")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet Icon",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "YANGA PAY SECURE WALLET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryPurple,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₦${String.format("%,.2f", balance)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Includes ₦10,000.00 welcome bonus!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalBlack.copy(alpha = 0.6f)
                        )
                    }
                    Button(
                        onClick = { onNavigate("wallet") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                            .testTag("fund_wallet_shortcut_btn")
                    ) {
                        Text(
                            text = "Fund +",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- Superapp Services Directory Grid ---
        item {
            Text(
                text = "Discover Our Ecosystem 🌟",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ServiceGridItem(
                        title = "Food & Fruits",
                        desc = "Order Chow & Fresh Pickings",
                        icon = Icons.Default.Restaurant,
                        tag = "Yummy",
                        color = Color(0xFFFFEDD5), // Soft Orange
                        modifier = Modifier.weight(1f).clickable { onNavigate("market") }
                    )
                    ServiceGridItem(
                        title = "Events Management",
                        desc = "Grab Concert & Suya Tickets",
                        icon = Icons.Default.ConfirmationNumber,
                        tag = "Happening",
                        color = Color(0xFFF3E8FF), // Soft Purple
                        modifier = Modifier.weight(1f).clickable { onNavigate("events") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ServiceGridItem(
                        title = "Retail Shops",
                        desc = "Custom Ankara & Powerbanks",
                        icon = Icons.Default.ShoppingBag,
                        tag = "Fashion",
                        color = Color(0xFFDFFFD6), // Soft Green
                        modifier = Modifier.weight(1f).clickable { onNavigate("market") }
                    )
                    ServiceGridItem(
                        title = "Hospitals & Medical",
                        desc = "Emergency & Tests Discovery",
                        icon = Icons.Default.LocalHospital,
                        tag = "24/7 Care",
                        color = Color(0xFFE0F2FE), // Soft Blue
                        modifier = Modifier.weight(1f).clickable { onNavigate("hospitals") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ServiceGridItem(
                        title = "Let's Share Vibes",
                        desc = "Interactive Community Feed",
                        icon = Icons.Default.Campaign,
                        tag = "Social",
                        color = Color(0xFFFFE4E6), // Soft Rose
                        modifier = Modifier.weight(1f).clickable { onNavigate("vibes") }
                    )
                    ServiceGridItem(
                        title = "Secure Wallet",
                        desc = "Review Audited Transactions",
                        icon = Icons.Default.Shield,
                        tag = "Fintech",
                        color = Color(0xFFFEF9C3), // Soft Yellow
                        modifier = Modifier.weight(1f).clickable { onNavigate("wallet") }
                    )
                }
            }
        }

        // --- Active Booking Ledger Row ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My active Bookings & Tickets 🎫",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
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
            Spacer(modifier = Modifier.height(8.dp))

            if (bookings.isEmpty()) {
                Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "No active bookings",
                                tint = PrimaryPurple.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No active table bookings, healthcare slots, or event tickets. Purchase one to see it pop up here!",
                                fontSize = 12.sp,
                                color = CharcoalBlack.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bookings) { booking ->
                        BookingCard(booking = booking, onCancel = { viewModel.cancelActiveBooking(booking.id) })
                    }
                }
            }
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
