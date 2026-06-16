package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow

@Composable
fun EventsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val balance by viewModel.walletBalance.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            YangaHeader(
                title = "Yanga Events! 🎟️✨",
                subtitle = "Discover premier concerts, culinary food exposures & coding networking meets",
                icon = Icons.Default.ConfirmationNumber
            )
        }

        // --- Wallet Info balance notice card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SecondaryYellow.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PrimaryPurple, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Wallet balance", tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Yanga Pay Secure Verification",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Ticket fees are automatically debited from your secure wallet. Balance: ₦${String.format("%,.2f", balance)}",
                            fontSize = 10.sp,
                            color = CharcoalBlack.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- Events List Header ---
        item {
            Text(
                text = "Happening Soon in Lagos 🔥",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
        }

        // --- List items ---
        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
        } else {
            items(events) { event ->
                EventTicketCard(event = event, onPurchase = { viewModel.purchaseEventTicket(event) })
            }
        }
    }
}

@Composable
fun EventTicketCard(
    event: com.example.domain.model.Event,
    onPurchase: () -> Unit
) {
    val isFree = event.price <= 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Price Tag & Host Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                YangaBadge(
                    text = "Hosted by ${event.host}",
                    containerColor = SecondaryYellow,
                    contentColor = CharcoalBlack
                )
                Text(
                    text = if (isFree) "FREE RSVP" else "₦${String.format("%,.0f", event.price)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isFree) Color(0xFF16A34A) else PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Event Title
            Text(
                text = event.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date and time row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Event date",
                    tint = PrimaryPurple.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${event.date} at ${event.time}",
                    fontSize = 12.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Location Venue row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Event venue",
                    tint = PrimaryPurple.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = event.venue,
                    fontSize = 12.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = PrimaryPurple.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = onPurchase,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                    .testTag("reserve_event_${event.title.replace(" ", "_")}")
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = "Secure Ticket",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFree) "Register Free Seat" else "Buy Secure Entry Ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}
