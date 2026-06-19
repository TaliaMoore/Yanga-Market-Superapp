package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.HeartRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import com.example.data.network.WebSocketState
import com.example.data.network.WebSocketFrame
import com.example.data.network.SafeVibeMessage
import androidx.compose.foundation.lazy.LazyRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VibesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.vibePosts.collectAsState()
    val authorInput by viewModel.vibeAuthorInput.collectAsState()
    val contentInput by viewModel.vibeContentInput.collectAsState()
    val wsState by viewModel.webSocketStatus.collectAsState()
    val wsLogs by viewModel.webSocketLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            YangaHeader(
                title = "Let’s Share Vibes! 📣💜",
                subtitle = "Read, upvote and broadcast thoughts with fellow Yanga Market super-citizens",
                icon = Icons.Default.Campaign
            )
        }

        // --- Community Rolling Marquee Banner ---
        item {
            YangaCommunityMarqueeBanner()
        }

        // --- WebSocket Interactive Connection Settings & Console Tracer ---
        item {
            val activeThreads by viewModel.activeThreads.collectAsState()
            val registeredThreadUsers by viewModel.registeredThreadUsers.collectAsState()

            YangaWebSocketConsole(
                wsState = wsState,
                wsLogs = wsLogs,
                activeThreads = activeThreads,
                registeredThreadUsers = registeredThreadUsers,
                onConnect = { viewModel.connectWebSocket() },
                onDisconnect = { viewModel.disconnectWebSocket() },
                onCreateThread = { title, desc ->
                    viewModel.triggerCreatedThread(authorInput.ifBlank { "UnsignedCitizen" }, title, desc)
                },
                onAddUser = { threadId, userId, status ->
                    viewModel.triggerAddedUserToThread(threadId, userId, authorInput.ifBlank { "UnsignedCitizen" }, status)
                }
            )
        }

        // --- Create Post Panel ---
        item {
            YangaVisuallyDistinctSection(
                title = "Broadcast Your Vibe ✨",
                subtitle = "Type below to share instantly",
                headerBadgeText = "COMMUNITY CHAT",
                headerBadgeColor = SecondaryYellow,
                backgroundColor = Color.White,
                borderColor = PrimaryPurple,
                borderWidth = 2.0,
                modifier = Modifier.fillMaxWidth().testTag("vibe_composer")
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                
                // Author input handle
                OutlinedTextField(
                    value = authorInput,
                    onValueChange = { viewModel.updateVibeInputs(author = it, content = null) },
                    label = { Text("Your Handle / Pseudo") },
                    placeholder = { Text("e.g. tunde_yanga") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.fillMaxWidth().testTag("vibe_author_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Content input body
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { viewModel.updateVibeInputs(author = null, content = it) },
                    label = { Text("What is happening at Yanga Market today?") },
                    placeholder = { Text("Tell us! Jollof ratings, event rsvps...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .testTag("vibe_body_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Submit button
                YangaFunButton(
                    text = "Share Vibe Now 📣",
                    onClick = { viewModel.submitVibe() },
                    modifier = Modifier.fillMaxWidth(),
                    testTagStr = "vibe_submit_btn"
                )
            }
        }

        // --- Feed Header ---
        item {
            Text(
                text = "Community Board Streams 📡",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
        }

        // --- Feed List Loops ---
        if (posts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = PrimaryPurple.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Silence on the wire. Be the first to share a vibe above!",
                                fontSize = 12.sp,
                                color = CharcoalBlack.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                VibePostRow(post = post, onReaction = { viewModel.voteVibePost(post.id) })
            }
        }
    }
}

@Composable
fun VibePostRow(
    post: com.example.data.database.VibePostEntity,
    onReaction: () -> Unit
) {
    val df = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeLabel = df.format(Date(post.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Profile & Author meta raw
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circular Pseudo-Avatar representation
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryYellow)
                            .border(1.5.dp, PrimaryPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.take(2).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "@${post.author}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalBlack
                        )
                        Text(
                            text = "Super-Citizen • Shared at $timeLabel",
                            fontSize = 10.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Security status badge
                YangaBadge(text = "Verified Vibe", containerColor = Color(0xFFF0FDF4), contentColor = Color(0xFF15803D))
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Post payload
            Text(
                text = post.content,
                fontSize = 13.sp,
                color = CharcoalBlack,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = PrimaryPurple.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action section (Reaction Like buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Vibe Check triggers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onReaction() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("vibe_check_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isVibeChecked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Vibe checked state",
                        tint = if (post.isVibeChecked) HeartRed else PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vibe Check (${post.vibeCount})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (post.isVibeChecked) HeartRed else CharcoalBlack
                    )
                }

                // Sub comments count placeholder
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = CharcoalBlack.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reply",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun YangaCommunityMarqueeBanner(
    modifier: Modifier = Modifier
) {
    val announcements = remember {
        listOf(
            "🔥 LAGOS JOLLOF FESTIVAL ticket sales are now live in Yanga Events! Use coupon Code: YANGAJOLLOF for 15% off!",
            "🍊 Fresh Harvest! Tangerines, Strawberries & Yellow Melons just restocked in Yanga Fruits section now!",
            "💜 Meet up with friends at Yanga's Let's Share Vibes community board! Let's keep the energy positive!",
            "💳 SECURE IN-APP WALLET: Top up your Yanga Wallet directly via secure bank transfer or cards for super-fast deliveries!"
        )
    }
    val textToShow = remember(announcements) { announcements.joinToString("   •   ") }

    // Infinite transition to continuously update offset coordinate across the container
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val marqueeFraction by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = -2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "x"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF08A)), // Pale Yellow background
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
            .testTag("yanga_community_marquee"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Static Purple tag on the left
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(PrimaryPurple, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HOT UPDATES 📡",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // The animated scrolling ticker
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val textOffset = marqueeFraction * containerWidthPx

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = textOffset
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = textToShow,
                        color = PrimaryPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun YangaWebSocketConsole(
    wsState: WebSocketState,
    wsLogs: List<String>,
    activeThreads: List<SafeVibeMessage.CreatedThread>,
    registeredThreadUsers: List<SafeVibeMessage.AddedUserToThread>,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onCreateThread: (String, String) -> Unit,
    onAddUser: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLogsExpanded by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
            .testTag("websocket_console_card"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with title & Expand/Collapse toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputComponent,
                        contentDescription = "WebSocket settings",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Vibes Live WebSocket Stream",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                }

                // Expand Collapse Arrow
                IconButton(
                    onClick = { isLogsExpanded = !isLogsExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isLogsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isLogsExpanded) "Collapse" else "Expand",
                        tint = PrimaryPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Two-way live network channels connecting your screen with other Nigerian citizens via event listeners.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = CharcoalBlack.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Two rows: Connection Badges & Control actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection Badge Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Status:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                    
                    val (badgeText, badgeBg, badgeTextClr) = when (wsState) {
                        WebSocketState.CONNECTED -> Triple("CONNECTED 🟢", Color(0xFFDCFCE7), Color(0xFF15803D))
                        WebSocketState.CONNECTING -> Triple("CONNECTING 🟡", Color(0xFFFEF9C3), Color(0xFFA16207))
                        WebSocketState.DISCONNECTED -> Triple("DISCONNECTED 🔴", Color(0xFFFEE2E2), Color(0xFFB91C1C))
                    }

                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .border(1.dp, badgeTextClr.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeTextClr
                        )
                    }
                }

                // Control Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (wsState == WebSocketState.CONNECTED) {
                        Button(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Disconnect",
                                tint = Color(0xFF374151),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Disconnect", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                        }
                    } else {
                        Button(
                            onClick = onConnect,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Connect",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect Stream", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            if (isLogsExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // Log Title section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Pipe Packet Frame Logs (Interactive)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "wss://stream.yanga.live",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // The terminal console shell
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .border(1.5.dp, CharcoalBlack, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (wsLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No TCP packets logged yet. Broadcast a message or wait for inbounds!",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(wsLogs) { logLine ->
                                val logColor = when {
                                    logLine.contains("[CLIENT SEND]") -> Color(0xFFFEF08A)
                                    logLine.contains("[SERVER RECV]") -> Color(0xFF38BDF8)
                                    logLine.contains("[SERVER BROADCAST]") || logLine.contains("[CLIENT_RECV_SYNC]") -> Color(0xFF34D399)
                                    logLine.contains("[CLIENT ERROR]") || logLine.contains("ERROR") -> Color(0xFFF87171)
                                    else -> Color.White
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = logLine,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = logColor,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Interactive SafeEmitter Panel
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = "SafeEmitter",
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Typesafe SafeEmitter Control Board",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                var testThreadTitle by remember { mutableStateOf("Fuji Music Arena") }
                var testUserId by remember { mutableStateOf("yoruba_boy") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Create Thread Panel
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emit CreatedThread", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = testThreadTitle,
                            onValueChange = { testThreadTitle = it },
                            placeholder = { Text("Title", fontSize = 9.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                if (testThreadTitle.isNotBlank()) {
                                    onCreateThread(testThreadTitle, "A super energetic conversation regarding " + testThreadTitle)
                                }
                            },
                            enabled = wsState == WebSocketState.CONNECTED,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("emit createdThread 📡", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Add User to Thread Panel
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emit addedUserToThread", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = testUserId,
                            onValueChange = { testUserId = it },
                            placeholder = { Text("Username", fontSize = 9.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                if (testUserId.isNotBlank()) {
                                    val threadId = activeThreads.firstOrNull()?.threadId ?: "thread-yanga"
                                    onAddUser(threadId, testUserId, "Yanga Ambassador")
                                }
                            },
                            enabled = wsState == WebSocketState.CONNECTED,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("emit addedUserToThread ⚡", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Show active threads and users from typesafe listener store
                if (activeThreads.isNotEmpty() || registeredThreadUsers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Typesafe Listener State Store (Safely intercepts packets):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(activeThreads) { th ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("CreatedThread Event", fontSize = 7.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Black)
                                    Text("ID: ${th.threadId}", fontSize = 7.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                                    Text("Title: ${th.title}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                    Text("By: @${th.creator}", fontSize = 7.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                                }
                            }
                        }

                        items(registeredThreadUsers) { usr ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF3E8FF), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF7E22CE).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("AddedUserToThread Event", fontSize = 7.sp, color = Color(0xFF6B21A8), fontWeight = FontWeight.Black)
                                    Text("User: @${usr.userId}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                    Text("Thread ID: ${usr.threadId}", fontSize = 7.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                                    Text("Invited By: @${usr.addedBy}", fontSize = 7.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Broadcasting and simulation run entirely on active local EventEmitters.",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = CharcoalBlack.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

