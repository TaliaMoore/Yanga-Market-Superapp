package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.CardGiftcard
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
import com.example.ui.YangaNotification
import com.example.ui.components.PurplePeacockLogo
import com.example.ui.components.YangaFunButton
import com.example.ui.components.YangaPlayfulCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PurplePeacockLogo(size = 32.dp)
                        Text(
                            text = "Notification Center",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple,
                                fontSize = 20.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("notifications_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = PrimaryPurple
                        )
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.markAllNotificationsAsRead() },
                            modifier = Modifier.testTag("mark_all_read_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = PrimaryPurple
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // Unread count and Quick Mock Triggers Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryPurple.copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, PrimaryPurple)
                ) {
                    Text(
                        text = " $unreadCount Unread ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Interactive Demo Action Trigger to simulate a "new bonus" showing up
                TextButton(
                    onClick = {
                        val bonusAmount = (1000..5000).random()
                        viewModel.addNotification(
                            title = "Instant Peacock Bonus! 🦚🎉",
                            message = "Amazing! A new community bonus of ₦${String.format("%,.2f", bonusAmount.toDouble())} was just randomly generated and credited. Claim yours on the Ledger!",
                            type = "BONUS",
                            icon = "🦚"
                        )
                        viewModel.addSilverCoins(25, "Instant Peacock Bonus")
                    },
                    modifier = Modifier.testTag("trigger_bonus_demo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Gift Icon",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Trigger Demo Bonus 🎁",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF97316)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (notifications.isEmpty()) {
                // Empty state centered with beautiful purple peacock logo placeholder
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(PlayfulCream, CircleShape)
                            .border(2.dp, PrimaryPurple.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PurplePeacockLogo(size = 110.dp)
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Your Peacock Plumes are Clear! 🦚💜",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Any new wallet transactions, draw entries, or special community bonuses will fly right here in real-time.",
                        fontSize = 12.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("notifications_list")
                ) {
                    items(notifications, key = { it.id }) { item ->
                        NotificationCard(
                            notification = item,
                            onDelete = { viewModel.deleteNotification(item.id) },
                            onMarkRead = {
                                // Since clicking should mark it as read
                                // Let's add marking read behavior in MainViewModel later
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotificationCard(
    notification: YangaNotification,
    onDelete: () -> Unit,
    onMarkRead: () -> Unit
) {
    // Styling attributes based on category
    val (cardColor, borderColor, badgeColor) = when (notification.type) {
        "BONUS" -> Triple(Color(0xFFFAF5FF), Color(0xFFD8B4FE), Color(0xFF8B5CF6)) // Lavender/Purple
        "TRANSACTION" -> Triple(Color(0xFFEFF6FF), Color(0xFF93C5FD), Color(0xFF3B82F6)) // Blue
        "DRAW" -> Triple(Color(0xFFFFF7ED), Color(0xFFFDBA74), Color(0xFFF97316)) // Orange/Yellow
        else -> Triple(Color(0xFFF0FDF4), Color(0xFF86EFAC), Color(0xFF10B981)) // Green/System
    }

    YangaPlayfulCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkRead() },
        borderColor = if (!notification.isRead) PrimaryPurple else borderColor,
        borderWidth = if (!notification.isRead) 2.5 else 1.2,
        backgroundColor = cardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Styled Category Emoji/Icon Sphere
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, CircleShape)
                    .border(1.5.dp, badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.icon,
                    fontSize = 20.sp
                )
            }

            // Notification message body details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = if (!notification.isRead) FontWeight.Black else FontWeight.Bold,
                        color = CharcoalBlack,
                        modifier = Modifier.weight(1f)
                    )

                    // Unread badge indicator dot
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryPurple, CircleShape)
                        )
                    }
                }

                Text(
                    text = notification.message,
                    fontSize = 11.5.sp,
                    color = CharcoalBlack.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.timestamp,
                        fontSize = 10.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = " " + notification.type + " ",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Delete action button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
                    .testTag("delete_notif_${notification.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete notification",
                    tint = CharcoalBlack.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
