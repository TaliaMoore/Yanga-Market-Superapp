package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.YangaComplaint
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val complaints by viewModel.complaints.collectAsState()

    // Screen States
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showCustomerCareChat by remember { mutableStateOf(false) }

    // Complaint Form States
    var selectedCategory by remember { mutableStateOf("Food Delivery Delay") }
    val categories = listOf(
        "Food Delivery Delay",
        "Wallet Transaction Error",
        "Event Ticket Issue",
        "Hospital Service Bug",
        "Vibes Feed Spam",
        "Other Support"
    )
    var complaintTitle by remember { mutableStateOf("") }
    var complaintDetails by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Chat states
    var chatInput by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            "Yanga Assistant" to "E ku joko! Welcome to Yanga Live Care! How can we make your day awesome today? 🌟",
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header with back navigation ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SecondaryYellow, CircleShape)
                        .border(1.5.dp, PrimaryPurple, CircleShape)
                        .testTag("settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Yanga Settings ⚙️",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack
                    )
                    Text(
                        text = "Customize your superapp experience & get prompt help",
                        fontSize = 12.sp,
                        color = CharcoalBlack.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- SECTION 1: Help, Customer Care & Contact ---
        item {
            YangaPlayfulCard(
                backgroundColor = Color(0xFFFDFBF7),
                borderColor = PrimaryPurple,
                borderWidth = 1.5,
                modifier = Modifier.fillMaxWidth().testTag("customer_care_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFDCFCE7), CircleShape)
                                .border(1.dp, Color(0xFF16A34A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HeadsetMic,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Yanga Support Desk & Live Chat",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Got stuck? No worries! Tap below to start an interactive chat session with our round-the-clock Yanga Care specialists.",
                        fontSize = 12.sp,
                        color = CharcoalBlack.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    YangaFunButton(
                        text = if (showCustomerCareChat) "Hide Live Support Chat" else "Open Live Support Chat 💬",
                        onClick = { showCustomerCareChat = !showCustomerCareChat },
                        modifier = Modifier.fillMaxWidth(),
                        testTagStr = "toggle_support_chat_btn"
                    )

                    AnimatedVisibility(visible = showCustomerCareChat) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Live Session with Yanga Care Agent 🟢",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Chat scrollable messages area
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                                    .verticalScrollStatePadding(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chatMessages.forEach { (sender, text) ->
                                    val isAgent = sender == "Yanga Assistant"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isAgent) Alignment.Start else Alignment.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 8.dp,
                                                        topEnd = 8.dp,
                                                        bottomStart = if (isAgent) 0.dp else 8.dp,
                                                        bottomEnd = if (isAgent) 8.dp else 0.dp
                                                    )
                                                )
                                                .background(if (isAgent) PrimaryPurple.copy(alpha = 0.1f) else SecondaryYellow.copy(alpha = 0.3f))
                                                .padding(8.dp)
                                                .widthIn(max = 220.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = sender,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = PrimaryPurple
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = text,
                                                    fontSize = 11.sp,
                                                    color = CharcoalBlack
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    placeholder = { Text("Type helper request...", fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("chat_input_field")
                                )
                                Button(
                                    onClick = {
                                        if (chatInput.isNotBlank()) {
                                            val query = chatInput
                                            chatMessages.add("You" to query)
                                            chatInput = ""
                                            // Simulated smart response
                                            val reply = when {
                                                query.contains("wallet", ignoreCase = true) || query.contains("pay", ignoreCase = true) -> {
                                                    "Our secure Ledger system settles wallet transactions in seconds! If your funds didn't reflect immediately, please submit a Wallet Transaction complaint below so we can scan the GraphQL logs! 💳"
                                                }
                                                query.contains("food", ignoreCase = true) || query.contains("fruit", ignoreCase = true) -> {
                                                    "Food and fruits from Yanga Market are sourced directly and delivered hot! For late riders, check the 'Yanga Rider' page to track their live coordinates! 🍔🏍️"
                                                }
                                                query.contains("hospital", ignoreCase = true) || query.contains("doctor", ignoreCase = true) -> {
                                                    "Health is wealth! The Hospital Service Discovery links you with live consults. Feel free to explore details in the Hospitals tab! 🏥"
                                                }
                                                else -> {
                                                    "No worries, your request is noted! Our customer care heroes have received your chat request. Feel free to launch a formal complaint below for maximum visibility! 🚀"
                                                }
                                            }
                                            chatMessages.add("Yanga Assistant" to reply)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("send_chat_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: Launch a Complaint ---
        item {
            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = Color(0xFFF97316), // Fun orange border
                borderWidth = 1.5,
                modifier = Modifier.fillMaxWidth().testTag("launch_complaint_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFF7ED), CircleShape)
                                .border(1.dp, Color(0xFFF97316), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Launch a Complaint 📢",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC2410C)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dissatisfied with a vendor, rider, or system issue? Register a formal dispute below. We track complaints immediately via transparent ticket histories.",
                        fontSize = 12.sp,
                        color = CharcoalBlack.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Complaint Category Dropdown Selector
                    Text(
                        text = "Complaint Category",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                                .testTag("complaint_category_input")
                        )
                        DropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, fontSize = 12.sp) },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Complaint Title
                    OutlinedTextField(
                        value = complaintTitle,
                        onValueChange = { complaintTitle = it },
                        label = { Text("Complaint Heading / Title") },
                        placeholder = { Text("e.g. Double payment during checkout") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complaint_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Complaint Details
                    OutlinedTextField(
                        value = complaintDetails,
                        onValueChange = { complaintDetails = it },
                        label = { Text("Detailed description of the issue") },
                        placeholder = { Text("Write transaction times, amounts, or merchant details here...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complaint_details_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    YangaFunButton(
                        text = "Submit Complaint Ticket 📢",
                        onClick = {
                            if (complaintTitle.isNotBlank() && complaintDetails.isNotBlank()) {
                                viewModel.launchComplaint(selectedCategory, complaintTitle, complaintDetails)
                                complaintTitle = ""
                                complaintDetails = ""
                            } else {
                                viewModel.postError("Please fill out both the complaint title and details.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTagStr = "submit_complaint_btn"
                    )
                }
            }
        }

        // --- SECTION 3: Your Complaint History ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Your Submitted Tickets (${complaints.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (complaints.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tickets registered yet. All systems clear! 🟢",
                            fontSize = 12.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        complaints.forEach { ticket ->
                            YangaPlayfulCard(
                                backgroundColor = Color.White,
                                borderColor = when (ticket.status) {
                                    "Resolved" -> Color(0xFF16A34A)
                                    "Under Investigation" -> Color(0xFFEAB308)
                                    else -> PrimaryPurple
                                },
                                borderWidth = 1.0,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ticket.id,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CharcoalBlack
                                        )
                                        // Status tag
                                        val bgStatus = when (ticket.status) {
                                            "Resolved" -> Color(0xFFDCFCE7)
                                            "Under Investigation" -> Color(0xFFFEF9C3)
                                            else -> Color(0xFFF3E8FF)
                                        }
                                        val textStatusColor = when (ticket.status) {
                                            "Resolved" -> Color(0xFF15803D)
                                            "Under Investigation" -> Color(0xFFA16207)
                                            else -> PrimaryPurple
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(bgStatus)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ticket.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textStatusColor
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Category: ${ticket.category}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ticket.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ticket.details,
                                        fontSize = 11.sp,
                                        color = CharcoalBlack.copy(alpha = 0.75f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Filed on: ${ticket.timestamp}",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 4: Legal Frameworks (T&C, Privacy Policy) ---
        item {
            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = PrimaryPurple.copy(alpha = 0.5f),
                borderWidth = 1.0,
                modifier = Modifier.fillMaxWidth().testTag("legal_framework_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Yanga Superapp Legal Hub ⚖️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Read our simplified, highly compliant Terms & Conditions and Privacy Policy designed to protect Yanga Superapp citizens.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showTermsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("terms_of_service_btn")
                        ) {
                            Text("Terms & Conditions", fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showPrivacyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("privacy_policy_btn")
                        ) {
                            Text("Privacy Policy", fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTION 5: Core Account Actions (Log Out & Delete Account) ---
        item {
            YangaPlayfulCard(
                backgroundColor = Color(0xFFFEF2F2), // Light red canvas
                borderColor = Color(0xFFEF4444),
                borderWidth = 1.0,
                modifier = Modifier.fillMaxWidth().testTag("account_danger_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Danger & Session Zone ⚠️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Log out button
                        Button(
                            onClick = { showLogoutConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_logout_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Out Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Delete account button
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_delete_account_btn")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 1: Terms & Conditions ---
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions 📜", fontWeight = FontWeight.Bold, color = PrimaryPurple) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScrollStatePadding()
                        .heightIn(max = 280.dp)
                ) {
                    Text(
                        text = "Yanga Superapp Citizen Rules",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CharcoalBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Respect the Vibes: The 'Let's Share Vibes' board is built for unity, fun, and mutual support. Harassment or spam leads to a lifetime ban.\n\n" +
                               "2. Split Fairly: Our bulk purchase split packages are run collaboratively. Ensure immediate resolution via in-app wallet secure ledger entries.\n\n" +
                               "3. Safe Deliveries: Customer care guarantees swift hot food arrivals. However, severe traffic or rain delays are escalated safely via Yanga Rider tracking.\n\n" +
                               "4. Wallet Security: Always maintain sufficient balance in your wallet. Do not share your superapp authentication details with third parties.",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("I Agree & Understand", color = PrimaryPurple)
                }
            }
        )
    }

    // --- DIALOG 2: Privacy Policy ---
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy 🔒", fontWeight = FontWeight.Bold, color = PrimaryPurple) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScrollStatePadding()
                        .heightIn(max = 280.dp)
                ) {
                    Text(
                        text = "Your Data is Secure with Yanga",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CharcoalBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Personal Information: We securely store your username, phone/email, and location to power food deliveries, hospital discovery, and vibes community streams.\n\n" +
                               "2. Financial Logs: All wallet top-up transactions, splits, and purchases are safely indexed in our secure local Room database with client-side verification.\n\n" +
                               "3. Real-time Location: App utilizes GPS coordinates ONLY to estimate delivery rider timelines and close-proximity clinic recommendations.\n\n" +
                               "4. Account Deletion: You possess the absolute 'Right to be Forgotten'. Triggering account deletion completely purges your local databases, files, and preferences.",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("I Consent", color = PrimaryPurple)
                }
            }
        )
    }

    // --- DIALOG 3: Logout Confirmation ---
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Sign Out Session?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to end your active Yanga session? Your wallet balance is secure and will be waiting for your return.", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logOutUser()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Yes, Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // --- DIALOG 4: Delete Account Confirmation ---
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("⚠️ PERMANENT ACCOUNT DELETION", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
            text = {
                Text(
                    text = "This action is completely irreversible. You will lose access to your Yanga profile, custom handle, registered vibes posts, and any remaining wallet balance. Are you absolutely certain?",
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Yes, DELETE FOREVER")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

// Helper Extension modifier to support simple scroll behavior for chat/dialog views
@Composable
private fun Modifier.verticalScrollStatePadding(): Modifier {
    return this.then(
        Modifier.verticalScroll(rememberScrollState())
    )
}
