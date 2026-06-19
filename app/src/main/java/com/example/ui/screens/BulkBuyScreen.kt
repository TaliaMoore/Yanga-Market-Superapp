package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.YangaHeader
import com.example.ui.components.YangaPlayfulCard
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PlayfulCream
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import java.util.Locale

data class BulkBuyBox(
    val id: String,
    val name: String,
    val price: Double,
    val emoji: String,
    val summary: String,
    val description: String,
    val items: List<String>
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BulkBuyScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    initialBoxId: String = "box_5k",
    modifier: Modifier = Modifier
) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    var selectedBoxId by remember { mutableStateOf(initialBoxId) }
    
    val boxes = listOf(
        BulkBuyBox(
            id = "box_5k",
            name = "5K Fruit Basket",
            price = 5000.0,
            emoji = "🧺",
            summary = "Fresh essential fruit basket",
            description = "A compact, hand-selected basket of rich local fruits perfect for single residents or couples to split.",
            items = listOf(
                "2 Pineapples 🍍",
                "1 Pawpaw 🥭",
                "1 Watermelon 🍉",
                "5 Sweet Oranges 🍊",
                "5 Tangerines 🍊",
                "Green Cucumbers 🥒",
                "And much more inside!"
            )
        ),
        BulkBuyBox(
            id = "box_10k",
            name = "10K Fruit Basket",
            price = 10000.0,
            emoji = "🛍️",
            summary = "Premium family fruit bounty",
            description = "A heavier, deluxe package packed with exotic and premium vitamins to satisfy a full household's grocery run.",
            items = listOf(
                "2 Pineapples 🍍",
                "2 Pawpaws 🥭",
                "1 Watermelon 🍉",
                "2 Sweet Oranges 🍊",
                "6 Tangerines 🍊",
                "2 Cucumbers 🥒",
                "2 Red Apples 🍎",
                "Deluxe Strawberry Grapes 🍇",
                "Carrots 🥕",
                "Fresh Pears 🍐",
                "Golden Bananas 🍌",
                "And much more inside!"
            )
        ),
        BulkBuyBox(
            id = "box_15k",
            name = "15K Foodstuff Box",
            price = 15000.0,
            emoji = "🌾",
            summary = "Staple foodstuff pantry kit",
            description = "Classic daily grain and flour staples compiled to beat inflation in Lagos markets.",
            items = listOf(
                "Premium White Rice grain 🌾",
                "Honey Beans (Oloyin) 🫘",
                "Ijebu Garri (Fine-washed) 🌾",
                "Pure Palm / Vegetable Oil canister 🍾",
                "And much more inside!"
            )
        ),
        BulkBuyBox(
            id = "box_30k",
            name = "30K Food Pack",
            price = 30000.0,
            emoji = "📦",
            summary = "Ultimate mega household bounty",
            description = "The ultimate bulk package containing high-volume food staples, premium sweet fruits, proteins, and fresh soup ingredients.",
            items = listOf(
                "Bulk Premium Rice, Beans & Garri duo 🌾",
                "Full Sweet Melon & Pineapple array 🍉🍍",
                "Fresh Tomato, Pepper & Onion basket 🍅🧅",
                "Live Premium Protein pack (Chicken/Beef) 🍗",
                "Assorted spices & cooking essentials 🧂",
                "And much more inside!"
            )
        )
    )

    val selectedBox = boxes.find { it.id == selectedBoxId } ?: boxes.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Navigation header back button ---
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
                        .testTag("bulk_buy_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = PrimaryPurple
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Let's Share Bulk buy 🛒🎒",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Lagos Community Co-sharing Hub",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // --- Current available secure balance banner ---
        item {
            YangaPlayfulCard(
                backgroundColor = PlayfulCream,
                borderColor = PrimaryPurple,
                borderWidth = 2.0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR SECURE BALANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "₦${String.format("%,.2f", walletBalance)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(SecondaryYellow, RoundedCornerShape(8.dp))
                            .border(1.5.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SECURED WALLET 🔒",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryPurple
                        )
                    }
                }
            }
        }

        // --- Grid selector of the 4 packages ---
        item {
            Text(
                text = "Select Bulk Bundle:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // First Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    boxes.take(2).forEach { box ->
                        val isSelected = box.id == selectedBoxId
                        BulkSelectionCard(
                            box = box,
                            isSelected = isSelected,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBoxId = box.id }
                        )
                    }
                }
                // Second Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    boxes.drop(2).forEach { box ->
                        val isSelected = box.id == selectedBoxId
                        BulkSelectionCard(
                            box = box,
                            isSelected = isSelected,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBoxId = box.id }
                        )
                    }
                }
            }
        }

        // --- Box Interior Detailed list ---
        item {
            Text(
                text = "What is Inside ${selectedBox.name}?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple
            )
        }

        item {
            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = PrimaryPurple,
                borderWidth = 2.0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Headline and brief description
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PlayfulCream, CircleShape)
                                .border(1.5.dp, SecondaryYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedBox.emoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedBox.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack
                            )
                            Text(
                                text = "₦${String.format("%,.0f", selectedBox.price)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF97316)
                            )
                        }
                    }

                    Text(
                        text = selectedBox.description,
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )

                    Divider(color = PrimaryPurple.copy(alpha = 0.1f), thickness = 1.dp)

                    // Items check list
                    selectedBox.items.forEach { itemText ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Included Item",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = itemText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBlack
                            )
                        }
                    }
                }
            }
        }

        // --- Action Buttons (Order / Group-Split Share Vibes) ---
        item {
            Button(
                onClick = {
                    viewModel.purchaseBulkBuyBox(
                        boxName = selectedBox.name,
                        price = selectedBox.price,
                        itemsSummary = selectedBox.items.joinToString(", ").replace(" 🍍", "").replace(" 🥭", "").replace(" 🍉", "").replace(" 🍊", "").replace(" 🥒", "").replace(" 🍎", "").replace(" 🍇", "").replace(" 🥕", "").replace(" 🍐", "").replace(" 🍌", "").replace(" inside!", ""),
                        onSuccess = {
                            onNavigateBack()
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(2.5.dp, SecondaryYellow, RoundedCornerShape(14.dp))
                    .testTag("bulk_buy_order_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBasket,
                    contentDescription = "Order Icon",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Order Now (₦${String.format("%,.0f", selectedBox.price)}) + Broadcast Vibe",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Community Splitting Info",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lagos Splitting Policy: Split cost 50/50 with friends on Vibes!",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
        }
    }
}

@Composable
fun BulkSelectionCard(
    box: BulkBuyBox,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val outlineColor = if (isSelected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.12f)
    val cardBackground = if (isSelected) SecondaryYellow.copy(alpha = 0.25f) else Color.White
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    Box(
        modifier = modifier
            .height(96.dp)
            .background(PrimaryPurple.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .offset(x = if (isSelected) 0.dp else (-2).dp, y = if (isSelected) 0.dp else (-2).dp)
                .border(borderWidth, outlineColor, RoundedCornerShape(16.dp))
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
                        .size(32.dp)
                        .background(PlayfulCream, CircleShape)
                        .border(1.dp, PrimaryPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = box.emoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = box.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = "₦${String.format("%,.0f", box.price)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
