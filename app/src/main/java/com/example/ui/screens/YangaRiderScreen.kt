package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.YangaPlayfulCard
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PlayfulCream
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow

@Composable
fun YangaRiderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFBF7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Custom Header with Navigation Back ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, PrimaryPurple, CircleShape)
                        .testTag("rider_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to main board",
                        tint = PrimaryPurple
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Yanga Rider network ⚡",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "On-Demand Peer-to-Peer Logistics System",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // --- Custom Hero Graphic Design Banner ---
        item {
            YangaPlayfulCard(
                backgroundColor = SecondaryYellow,
                borderColor = PrimaryPurple,
                borderWidth = 2.5,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, PrimaryPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏍️💨", fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ALWAYS ON THE MOVE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lagos’ swiftest dispatcher fleet is just one tap away. Direct, custom pricing, and zero complex middle-men delays.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // --- How it Works Section (Pure P2P explaining ride app essence) ---
        item {
            Text(
                text = "How the Yanga Dispatch Ecosystem Works",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple
            )
        }

        item {
            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = PrimaryPurple.copy(alpha = 0.3f),
                borderWidth = 1.5,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Step 1: Request & Match
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PlayfulCream, CircleShape)
                                .border(1.5.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "1",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instantly request a route dispatch",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalBlack
                            )
                            Text(
                                text = "Enter your current destination on the interactive board. Nearby active, vetted drivers automatically receive your request through our specialized dispatch channels.",
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Step 2: Live Fair Pricing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PlayfulCream, CircleShape)
                                .border(1.5.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "2",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Real-time transparent fare estimation",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalBlack
                            )
                            Text(
                                text = "There are no hidden charges. Choose from different categories depending on budget and speed. You authorize the payment securely beforehand, ensuring high-integrity rides.",
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Step 3: Fast Fleet Match
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PlayfulCream, CircleShape)
                                .border(1.5.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "3",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Locate drivers with mapping & tracking",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalBlack
                            )
                            Text(
                                text = "Once a nearby rider claims your dispatch, they navigate to you immediately. Watch their real-time progression across streets for a seamless, punctual takeoff.",
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // --- CTA Download Play store Quote Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PlayfulCream),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Quote Icon",
                        tint = PrimaryPurple.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "“The Yanga Rider network is powered entirely by our decentralized driver utility system. Driving or requesting has never been simpler. To register as an active partner, earn money running delivery errands, or request trips, please download the official Yanga Rider application today!”",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = CharcoalBlack,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Play Store Imitation Button
                    Row(
                        modifier = Modifier
                            .background(CharcoalBlack, RoundedCornerShape(8.dp))
                            .border(1.5.dp, SecondaryYellow, RoundedCornerShape(8.dp))
                            .clickable {
                                // Simulate play store hyperlink navigation or trigger dialog instruction
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayForWork,
                            contentDescription = "Play Store Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "GET IT ON",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Google Play Store",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- Interactive Driver benefits lists ---
        item {
            Text(
                text = "Why Join as a Yanga Partner?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).border(1.dp, PrimaryPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "💰 100% Retained", fontSize = 11.sp, fontWeight = FontWeight.Black, color = CharcoalBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set your minimum bids with clients directly and keep modern commissions.",
                            fontSize = 9.sp,
                            color = CharcoalBlack.copy(alpha = 0.6f),
                            lineHeight = 13.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).border(1.dp, PrimaryPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "⏳ Flex Schedules", fontSize = 11.sp, fontWeight = FontWeight.Black, color = CharcoalBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Simply flip your partner toggle online to process local foodstuff or fruit deliveries.",
                            fontSize = 9.sp,
                            color = CharcoalBlack.copy(alpha = 0.6f),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // --- Custom footer signature ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Securely powered by Yanga Superapp Ecosystem 🔒",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryPurple,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
