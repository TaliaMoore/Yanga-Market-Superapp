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

        // --- Create Post Panel ---
        item {
            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = PrimaryPurple,
                borderWidth = 2.0,
                modifier = Modifier.fillMaxWidth().testTag("vibe_composer")
            ) {
                Text(
                    text = "Broadcast Your Vibe ✨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                
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
            items(posts) { post ->
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
