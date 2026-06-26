package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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

    var selectedEventForDetails by remember { mutableStateOf<com.example.domain.model.Event?>(null) }

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
                subtitle = "Discover Yanga vibes, concerts, culinary food exposures & tech networking meets",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Happening Soon in Lagos 🔥",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack
                )
                Text(
                    text = "Tap cards for details ℹ️",
                    fontSize = 11.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
            }
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
            items(events, key = { it.title }) { event ->
                EventTicketCard(
                    event = event,
                    onPurchase = { viewModel.purchaseEventTicket(event) },
                    onClick = { selectedEventForDetails = event }
                )
            }
        }
    }

    // --- Details Overlay Dialog ---
    selectedEventForDetails?.let { event ->
        EventDetailsDialog(
            event = event,
            onDismiss = { selectedEventForDetails = null },
            onPurchase = { viewModel.purchaseEventTicket(event) }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EventTicketCard(
    event: com.example.domain.model.Event,
    onPurchase: () -> Unit,
    onClick: () -> Unit
) {
    val isFree = event.price <= 0.0
    val context = LocalContext.current

    YangaVisuallyDistinctSection(
        title = event.title,
        subtitle = "Organized by ${event.host}",
        headerBadgeText = if (isFree) "FREE" else "₦${String.format("%,.0f", event.price)}",
        headerBadgeColor = if (isFree) Color(0xFFDCFCE7) else SecondaryYellow,
        backgroundColor = Color.White,
        borderColor = PrimaryPurple,
        borderWidth = 2.0,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Picture banner of the event if resource exists
        val imageResId = remember(event.imageResName) {
            if (event.imageResName.isNotEmpty()) {
                context.resources.getIdentifier(event.imageResName, "drawable", context.packageName)
            } else {
                0
            }
        }

        if (imageResId != 0) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Beautiful FlowLayout wrapping all visual tag labels
        YangaFlowButtonsLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Venue Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF9C3)) // Pale Yellow brand block
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.venue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                }
            }

            // Date Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3E8FF)) // Purple brand block
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} at ${event.time}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                }
            }

            // Attendance Capacity Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDCFCE7)) // Green brand block
                    .border(1.dp, Color(0xFF15803D).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "👥 ${event.rsvpCount} RSVP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF15803D)
                )
            }
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
                .border(2.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                .testTag("reserve_event_${event.title.replace(" ", "_")}")
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = "Secure Ticket",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFree) "Register" else "Buy Ticket",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun EventDetailsDialog(
    event: com.example.domain.model.Event,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = remember(event.imageResName) {
        if (event.imageResName.isNotEmpty()) {
            context.resources.getIdentifier(event.imageResName, "drawable", context.packageName)
        } else {
            0
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onPurchase()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(if (event.price <= 0.0) "Register" else "Buy Ticket", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryPurple)
            }
        },
        title = {
            Text(
                text = event.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = event.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Organized by Host
                Text(
                    text = "Host: ${event.host}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack.copy(alpha = 0.7f)
                )

                // Date & Time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${event.date} at ${event.time}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CharcoalBlack
                    )
                }

                // Venue
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = event.venue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CharcoalBlack
                    )
                }

                // Custom dynamic details
                Text(
                    text = event.details.ifEmpty { "Experience premier vibes with Yanga Market superapp." },
                    fontSize = 12.sp,
                    color = CharcoalBlack,
                    lineHeight = 18.sp
                )

                // Attributes checklist: Maybe there'll be food & Competition
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.hasFood) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "😋 Food Provided",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        }
                    }

                    if (event.hasCompetition) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🏆 Fun Competitions",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
