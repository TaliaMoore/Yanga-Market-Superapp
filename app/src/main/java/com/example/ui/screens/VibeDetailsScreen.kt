package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeDetailsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.vibePosts.collectAsState()
    val selectedPostId by viewModel.selectedVibePostId.collectAsState()
    val globalUserName by viewModel.userName.collectAsState()

    val currentPost = posts.find { it.id == selectedPostId }

    var replyContent by remember { mutableStateOf("") }
    var tempAuthor by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussion Thread", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (currentPost == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Discussion post not found or has been removed.", color = CharcoalBlack.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))
                    YangaFunButton(text = "Go Back", onClick = onNavigateBack)
                }
            }
        } else {
            val comments = viewModel.parseComments(currentPost.commentsJson)
            val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Main Post Card ---
                item {
                    YangaPlayfulCard(
                        backgroundColor = Color.White,
                        borderColor = PrimaryPurple,
                        borderWidth = 2.5,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryYellow)
                                            .border(1.5.dp, PrimaryPurple, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentPost.author.take(2).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PrimaryPurple
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "@${currentPost.author}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CharcoalBlack
                                        )
                                        Text(
                                            text = "Shared on ${df.format(Date(currentPost.timestamp))}",
                                            fontSize = 10.sp,
                                            color = CharcoalBlack.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                YangaBadge(text = "Verified Thread", containerColor = Color(0xFFF0FDF4), contentColor = Color(0xFF15803D))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentPost.content,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = CharcoalBlack,
                                lineHeight = 21.sp
                            )

                            // Render attached photo if any
                            if (!currentPost.attachedPhoto.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val isUri = currentPost.attachedPhoto.startsWith("content://") || 
                                             currentPost.attachedPhoto.startsWith("file://") || 
                                             currentPost.attachedPhoto.contains("/")

                                if (isUri) {
                                    AsyncImage(
                                        model = currentPost.attachedPhoto,
                                        contentDescription = "Attached photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val drawableId = context.resources.getIdentifier(
                                        currentPost.attachedPhoto, "drawable", context.packageName
                                    )
                                    if (drawableId != 0) {
                                        Image(
                                            painter = painterResource(id = drawableId),
                                            contentDescription = "Attached photo",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- Comment Input Composer ---
                item {
                    YangaVisuallyDistinctSection(
                        title = "Reply to this Thread 💬",
                        subtitle = "Contribute your vibe to the discussion",
                        backgroundColor = Color(0xFFFDFCF7), // Warm yellow/white tint
                        borderColor = PrimaryPurple,
                        borderWidth = 2.0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        if (globalUserName.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PrimaryPurple.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text("✍️ Posting as: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                Text("@$globalUserName", fontSize = 12.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("(Synced with Profile)", fontSize = 10.sp, color = Color.Gray)
                            }
                        } else {
                            // User has no handle, prompt them with notice and let them type or prompt to set in Profile
                            Column {
                                Text(
                                    text = "💡 Pro Tip: Set your handle in the Profile tab to reply automatically!",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = tempAuthor,
                                    onValueChange = { tempAuthor = it.trim().replace(" ", "") },
                                    label = { Text("Your Temp Handle") },
                                    placeholder = { Text("e.g. anonymous_yanga") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = replyContent,
                            onValueChange = { replyContent = it },
                            label = { Text("What are your thoughts on this?") },
                            placeholder = { Text("Type your contribution...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        YangaFunButton(
                            text = "Submit Contribution 🚀",
                            onClick = {
                                val finalAuthor = if (globalUserName.isNotBlank()) globalUserName else tempAuthor
                                if (finalAuthor.isBlank()) {
                                    viewModel.postError("Please provide a handle name or set one in the Profile page!")
                                } else if (replyContent.isBlank()) {
                                    viewModel.postError("Please write a contribution message!")
                                } else {
                                    viewModel.addCommentToPost(currentPost.id, finalAuthor, replyContent)
                                    replyContent = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // --- Comments Header ---
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Replies & Contributions (${comments.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalBlack
                        )
                        Box(
                            modifier = Modifier
                                .background(PrimaryPurple, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "QUORA MODE",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- Comments List ---
                if (comments.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        ) {
                            Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No contributions yet. Start the debate by replying above! 🗣️",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(comments, key = { it.id }) { comment ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryYellow)
                                            .border(1.dp, PrimaryPurple, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = comment.author.take(2).uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PrimaryPurple
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "@${comment.author}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CharcoalBlack
                                        )
                                        Text(
                                            text = df.format(Date(comment.timestamp)),
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = comment.content,
                                    fontSize = 12.sp,
                                    color = CharcoalBlack,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
