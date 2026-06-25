package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.HeartRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VibesScreen(
    viewModel: MainViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.vibePosts.collectAsState()
    val authorInput by viewModel.vibeAuthorInput.collectAsState()
    val contentInput by viewModel.vibeContentInput.collectAsState()
    val globalUserName by viewModel.userName.collectAsState()
    val selectedPhoto by viewModel.vibeAttachedPhotoInput.collectAsState()
    val groups by viewModel.discussionGroups.collectAsState()

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var selectedTagCircle by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.selectAttachedPhoto(uri.toString())
        }
    }

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
                subtitle = "Reddit-style community groups, instant answers, and live discussions with fellow super-citizens",
                icon = Icons.Default.Campaign
            )
        }

        // --- Community Rolling Marquee Banner ---
        item {
            YangaCommunityMarqueeBanner()
        }

        // --- DISCUSSION GROUPS SECTION (REDDIT/QUORA STYLE) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discussion Circles & Channels 📡",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Text(
                        text = "Join & Post",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(groups, key = { it.id }) { group ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (group.isJoined) Color(0xFFF9F7FE) else Color.White
                            ),
                            modifier = Modifier
                                .width(220.dp)
                                .border(
                                    2.dp, 
                                    if (group.isJoined) PrimaryPurple else PrimaryPurple.copy(alpha = 0.15f), 
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${group.name}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryPurple,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(SecondaryYellow, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = group.category.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CharcoalBlack
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = group.description,
                                    fontSize = 10.sp,
                                    color = CharcoalBlack.copy(alpha = 0.7f),
                                    lineHeight = 13.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.height(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "👥 ${group.memberCount} members",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack.copy(alpha = 0.5f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (group.isJoined) PrimaryPurple.copy(alpha = 0.1f) else SecondaryYellow)
                                            .clickable { viewModel.toggleJoinGroup(group.id) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (group.isJoined) "Joined ✓" else "Join 👥",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (group.isJoined) PrimaryPurple else CharcoalBlack
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Card to prompt group creation
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .width(160.dp)
                                .border(2.dp, SecondaryYellow, RoundedCornerShape(16.dp))
                                .clickable { showCreateGroupDialog = true }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📣", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Start a Group",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryPurple,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Launch a Reddit-style custom discussion channel",
                                    fontSize = 9.sp,
                                    color = CharcoalBlack.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
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
                
                // Author handle recognition
                if (globalUserName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryPurple.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "✍️ Posting as: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalBlack
                        )
                        Text(
                            text = "@$globalUserName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "✓ Synced",
                            fontSize = 10.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // Tell them to set handle or type a temporary one
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondaryYellow.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "💡 Pro-Tip: Go to your Profile page (labeled Profile tab below) and save your username handle so it fills in automatically next time!",
                            fontSize = 10.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = authorInput,
                        onValueChange = { viewModel.updateVibeInputs(author = it, content = null) },
                        label = { Text("Your Temporary Handle") },
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
                }

                // Choose a channel to tag (optional)
                val joinedGroups = groups.filter { it.isJoined }
                if (joinedGroups.isNotEmpty()) {
                    Text(
                        text = "Post inside a Discussion Circle (Optional):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTagCircle == null) PrimaryPurple else PrimaryPurple.copy(alpha = 0.05f)
                                    )
                                    .clickable { selectedTagCircle = null }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Global Feed 🌍",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTagCircle == null) Color.White else PrimaryPurple
                                )
                            }
                        }
                        items(joinedGroups) { g ->
                            val isSelected = selectedTagCircle == g.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.05f)
                                    )
                                    .clickable { selectedTagCircle = g.name }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "#${g.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else PrimaryPurple
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Content input body
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { viewModel.updateVibeInputs(author = null, content = it) },
                    label = { Text("What's happening at Yanga Market?") },
                    placeholder = { Text("Write your query, debate, suggestion or updates here...") },
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
                Spacer(modifier = Modifier.height(8.dp))

                // PHOTO ATTACHMENT BOX
                Text(
                    text = "Attach a beautiful Community Snapshot 📸",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val context = LocalContext.current
                val photos = listOf(
                    "img_event_festival_1782134258914" to "🎪 Festival",
                    "img_event_tech_1782134273337" to "💻 Tech Suya",
                    "img_food_jollof" to "🍛 Jollof",
                    "img_food_suya_burger" to "🍔 Burger"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Option to attach custom file from device
                    item {
                        val isCustomFileSelected = selectedPhoto != null && !photos.any { it.first == selectedPhoto }
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(90.dp)
                                .height(65.dp)
                                .border(
                                    if (isCustomFileSelected) 3.dp else 1.dp,
                                    if (isCustomFileSelected) PrimaryPurple else Color.LightGray,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCustomFileSelected && selectedPhoto != null) {
                                    AsyncImage(
                                        model = selectedPhoto,
                                        contentDescription = "Custom attached file",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Attached ✓",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(PrimaryPurple, CircleShape)
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = "Attach custom file",
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Device File",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(photos) { (resName, label) ->
                        val isSelected = selectedPhoto == resName
                        val drawableId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(90.dp)
                                .height(65.dp)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) PrimaryPurple else Color.LightGray,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        viewModel.selectAttachedPhoto(null)
                                    } else {
                                        viewModel.selectAttachedPhoto(resName)
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (drawableId != 0) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(PrimaryPurple, CircleShape)
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedPhoto != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { viewModel.selectAttachedPhoto(null) }
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = HeartRed, modifier = Modifier.size(14.dp))
                        Text("Remove Image Attachment", fontSize = 10.sp, color = HeartRed, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit button
                YangaFunButton(
                    text = "Share Vibe Now 📣",
                    onClick = {
                        val finalContent = if (selectedTagCircle != null) {
                            "$contentInput #${selectedTagCircle}"
                        } else contentInput

                        if (globalUserName.isBlank() && authorInput.trim().isBlank()) {
                            viewModel.postError("Please type your name or save your username in the Profile tab!")
                        } else {
                            viewModel.updateVibeInputs(author = null, content = finalContent)
                            viewModel.submitVibe()
                            selectedTagCircle = null
                        }
                    },
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
                val repliesCount = viewModel.parseComments(post.commentsJson).size
                VibePostRow(
                    post = post,
                    repliesCount = repliesCount,
                    onReaction = { viewModel.voteVibePost(post.id) },
                    onClick = { onNavigateToDetails(post.id) },
                    onBoost = { viewModel.boostVibeLikes(post.id) }
                )
            }
        }
    }

    // --- POPUP DIALOG TO CREATE NEW CHANNELS ---
    if (showCreateGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }
        var newGroupDesc by remember { mutableStateOf("") }
        var newGroupCat by remember { mutableStateOf("General") }

        Dialog(onDismissRequest = { showCreateGroupDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, PrimaryPurple, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Create Discussion Circle 📣",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )

                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Circle Name (e.g. tech-suya-night)") },
                        placeholder = { Text("use-hyphens-only") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newGroupDesc,
                        onValueChange = { newGroupDesc = it },
                        label = { Text("Circle Description") },
                        placeholder = { Text("What are we sharing in here?") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newGroupCat,
                        onValueChange = { newGroupCat = it },
                        label = { Text("Category / Tag") },
                        placeholder = { Text("e.g. Food, Tech, Care, Gossip") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showCreateGroupDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryPurple),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (newGroupName.isNotBlank() && newGroupDesc.isNotBlank()) {
                                    viewModel.createDiscussionGroup(newGroupName, newGroupDesc, newGroupCat)
                                    showCreateGroupDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                            modifier = Modifier
                                .weight(1.5f)
                                .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Launch Circle 🚀", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VibePostRow(
    post: com.example.data.database.VibePostEntity,
    repliesCount: Int,
    onReaction: () -> Unit,
    onClick: () -> Unit,
    onBoost: (() -> Unit)? = null
) {
    val df = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeLabel = df.format(Date(post.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
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

            // Render attached photo if any
            if (!post.attachedPhoto.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val isUri = post.attachedPhoto.startsWith("content://") || 
                             post.attachedPhoto.startsWith("file://") || 
                             post.attachedPhoto.contains("/")

                if (isUri) {
                    AsyncImage(
                        model = post.attachedPhoto,
                        contentDescription = "Attached photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val context = LocalContext.current
                    val drawableId = context.resources.getIdentifier(
                        post.attachedPhoto, "drawable", context.packageName
                    )
                    if (drawableId != 0) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Attached photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Replies ($repliesCount)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                }

                if (onBoost != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBoost() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("boost_likes_btn_${post.id}")
                    ) {
                        Text(
                            text = "🚀 Boost Vibes (+100 Checkouts)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                    }
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
