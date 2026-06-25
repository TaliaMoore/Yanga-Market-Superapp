package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val silverCoins by viewModel.silverCoins.collectAsState()
    val goldCoins by viewModel.goldCoins.collectAsState()
    val tickets by viewModel.drawTickets.collectAsState()
    val totalTicketsInPool by viewModel.drawTotalTicketsAll.collectAsState()
    val totalParticipants by viewModel.drawTotalParticipants.collectAsState()

    var ticketPurchaseCount by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Yanga Golden Draw Desk 🎰🌟",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("draw_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back to Wallet",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple)
            )
        },
        containerColor = PlayfulBg,
        modifier = modifier.testTag("draw_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HERO BANNER & MAIN PRIZE DISPLAY ---
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, PrimaryPurple, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SecondaryYellow, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .border(1.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = "🏆 MEGA JACKPOT PRIZE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Suzuki Dispatch Motorcycle 🏍️💨",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Equipped with cold-chain box + Yanga Super-Dispatcher kit. Boost your freelance income instantly!",
                            fontSize = 12.sp,
                            color = CharcoalBlack.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(PlayfulCream, CircleShape)
                                .border(2.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏍️", fontSize = 54.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Draw live countdown", tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                            Text(
                                text = "DRAW CLOSES: Tomorrow at 6:00 PM (GMT+1)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }

            // --- REAL-TIME DRAW METRICS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Your Tickets Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎫", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$tickets",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple,
                                modifier = Modifier.testTag("user_draw_tickets_count")
                            )
                            Text(
                                text = "Your Tickets",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Total Pool Tickets Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🗳️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalTicketsInPool",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack,
                                modifier = Modifier.testTag("pool_tickets_count")
                            )
                            Text(
                                text = "Total Tickets In Pool",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Total Participants Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "👥", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalParticipants",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack,
                                modifier = Modifier.testTag("pool_participants_count")
                            )
                            Text(
                                text = "Active Citizens",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // --- BUY ENTRY TICKETS FORM ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)), // Warm yellow highlight
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.5.dp, PrimaryPurple, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎟️ Enter the Golden Pool",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Deposit Gold Pieces to purchase entry tickets. 1 GP equals 1 Ticket.",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("YOUR GOLD BALANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "🪙", fontSize = 16.sp)
                                    Text(text = "$goldCoins GP", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
                                }
                            }

                            // - / + Ticket Selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (ticketPurchaseCount > 1) ticketPurchaseCount-- },
                                    modifier = Modifier.size(32.dp).testTag("decrease_ticket_btn")
                                ) {
                                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                }

                                Text(
                                    text = "$ticketPurchaseCount",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    modifier = Modifier.width(20.dp).testTag("ticket_purchase_value"),
                                    textAlign = TextAlign.Center
                                )

                                IconButton(
                                    onClick = { ticketPurchaseCount++ },
                                    modifier = Modifier.size(32.dp).testTag("increase_ticket_btn")
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.enterDraw(ticketPurchaseCount)
                                ticketPurchaseCount = 1 // Reset count
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_draw_entry_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Purchase $ticketPurchaseCount Ticket(s) ($ticketPurchaseCount GP)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // --- CONSOLATION PRIZES LIST ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🎁 Runner-Up Prizes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack
                        )

                        listOf(
                            "🛵 Consolation prize" to "Yanga Rider Free Dispatch Credits (₦15,000 value)",
                            "🍕 Consolation prize" to "Choice Supermarket Gourmet Snack Crate & Drinks Box",
                            "💵 Consolation prize" to "₦10,000 In-app Wallet Cashback directly deposited"
                        ).forEach { (badge, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF3E8FF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(badge, fontSize = 8.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                }
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = CharcoalBlack.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // --- HOW TO EARN MORE COINS RULES INFO ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Rules icon", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                            Text(
                                text = "How to Earn More Yanga Coins?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        listOf(
                            "🛍️ Order Items" to "Every item purchased across Yanga Market, Fruits, or Restaurants gives +1 Silver piece.",
                            "🎒 Freelance reviews" to "As a freelance supplier, receiving a 5-star review instantly rewards you with +5 Silver pieces.",
                            "🔥 Let's Share Vibes" to "Post your bulk-buy requests or citizen status! Hits 100+ vibe check likes? Earn +1 Silver piece.",
                            "🪙 Seamless gold upgrade" to "Once you reach 100 Silver pieces, convert them instantly into 1 Gold Piece in your wallet!"
                        ).forEach { (rule, details) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(rule, fontSize = 10.sp, fontWeight = FontWeight.Black, color = CharcoalBlack)
                                Text(details, fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.6f), lineHeight = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
