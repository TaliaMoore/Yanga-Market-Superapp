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
import com.example.ui.EventApplication
import com.example.ui.FreelancerApplication
import com.example.ui.BusinessApplication
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

    // SuperApp Hub states
    var activePanel by remember { mutableStateOf<String?>(null) }

    // Admin & Moderator states
    var newModEmail by remember { mutableStateOf("") }
    var adminNotifTitle by remember { mutableStateOf("") }
    var adminNotifMessage by remember { mutableStateOf("") }
    var insightAuthor by remember { mutableStateOf("") }
    var insightText by remember { mutableStateOf("") }
    var insightCategory by remember { mutableStateOf("Motivation") }
    var isInsightCategoryExpanded by remember { mutableStateOf(false) }
    
    // Freelancer Application
    var freeName by remember { mutableStateOf("") }
    var freeTitle by remember { mutableStateOf("") }
    var freeLinkedin by remember { mutableStateOf("") }
    var freeGithub by remember { mutableStateOf("") }
    var freeBackPhoto by remember { mutableStateOf("") }
    var freeNormalPhoto by remember { mutableStateOf("") }
    var freeSkills by remember { mutableStateOf("") }
    var freeBio by remember { mutableStateOf("") }
    var freeBasePrice by remember { mutableStateOf("5000") }
    var freeCategory by remember { mutableStateOf("Software") }
    var isFreeCategoryExpanded by remember { mutableStateOf(false) }

    // Business Application
    var bizName by remember { mutableStateOf("") }
    var bizCategory by remember { mutableStateOf("Hospital") }
    var isBizCategoryExpanded by remember { mutableStateOf(false) }
    var bizCac by remember { mutableStateOf("") }
    var bizLocation by remember { mutableStateOf("") }
    var bizImage by remember { mutableStateOf("") }
    var bizServices by remember { mutableStateOf("") }

    // Event Application
    var evtTitle by remember { mutableStateOf("") }
    var evtDescription by remember { mutableStateOf("") }
    var evtDate by remember { mutableStateOf("") }
    var evtTime by remember { mutableStateOf("") }
    var evtVenue by remember { mutableStateOf("") }
    var evtHost by remember { mutableStateOf("") }
    var evtCoupon by remember { mutableStateOf("") }
    var evtIsFree by remember { mutableStateOf(true) }
    var evtImageDesc by remember { mutableStateOf("") }

    // Skill & Portfolio Addition
    var additionalSkills by remember { mutableStateOf("") }
    var portfolioItemTitle by remember { mutableStateOf("") }

    // Business Item Form
    var newItemName by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("2500") }

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

        // --- SECTION 1.5: Yanga Superapp Hub (Registrations & Admin Portal) 🚀 ---
        item {
            val myFreelancerStatus by viewModel.myFreelancerAppStatus.collectAsState()
            val myBusinessStatus by viewModel.myBusinessAppStatus.collectAsState()
            val myFreelancerId by viewModel.myFreelancerProfileId.collectAsState()
            val myBusinessId by viewModel.myBusinessProfileId.collectAsState()
            val myBusinessCategory by viewModel.myBusinessCategory.collectAsState()

            val pendingFreelancers by viewModel.pendingFreelancers.collectAsState()
            val pendingBusinesses by viewModel.pendingBusinesses.collectAsState()
            val pendingEvents by viewModel.pendingEvents.collectAsState()

            val currentUserEmail by viewModel.userPhoneOrEmail.collectAsState()
            val moderators by viewModel.moderators.collectAsState()

            val isAdmin = currentUserEmail.lowercase().trim() == "admin101@admin.com"
            val isModerator = moderators.contains(currentUserEmail.lowercase().trim())

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activePanel == null) {
                    YangaPlayfulCard(
                        backgroundColor = Color(0xFFFAF5FF), // Playful soft purple
                        borderColor = PrimaryPurple,
                        borderWidth = 1.5,
                        modifier = Modifier.fillMaxWidth().testTag("talent_business_hub_card")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFF3E8FF), CircleShape)
                                        .border(1.dp, PrimaryPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Yanga Superapp Hub 🚀",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Register as a service provider, list your retail outlet or restaurant, publish events, or moderate submissions in real time!",
                                fontSize = 12.sp,
                                color = CharcoalBlack.copy(alpha = 0.75f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Option 1: Freelancer Option
                            when (myFreelancerStatus) {
                                "" -> {
                                    YangaFunButton(
                                        text = "Become a Freelancer 👦💼",
                                        onClick = { activePanel = "register_freelancer" },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        testTagStr = "become_freelancer_btn"
                                    )
                                }
                                "Pending" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).border(1.dp, Color(0xFFEAB308), RoundedCornerShape(8.dp))
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Freelancer Profile: Pending Admin Review ⏱️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA16207))
                                        }
                                    }
                                }
                                "Approved" -> {
                                    YangaFunButton(
                                        text = "Enter Freelancer Profile 👦⭐",
                                        onClick = { activePanel = "edit_freelancer" },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        testTagStr = "enter_freelancer_profile_btn"
                                    )
                                }
                            }

                            // Option 2: Business Option
                            when (myBusinessStatus) {
                                "" -> {
                                    YangaFunButton(
                                        text = "Register a Business 🏢💼",
                                        onClick = { activePanel = "register_business" },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        testTagStr = "register_business_btn"
                                    )
                                }
                                "Pending" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).border(1.dp, Color(0xFFEAB308), RoundedCornerShape(8.dp))
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.HourglassEmpty, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Business Profile: Pending Admin Review ⏱️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA16207))
                                        }
                                    }
                                }
                                "Approved" -> {
                                    YangaFunButton(
                                        text = "Enter Business Profile 🏢⭐",
                                        onClick = { activePanel = "edit_business" },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        testTagStr = "enter_business_profile_btn"
                                    )
                                }
                            }

                            // Option 3: Event Option
                            YangaFunButton(
                                text = "Register an Event 🎟️🔥",
                                onClick = { activePanel = "register_event" },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                testTagStr = "register_event_btn"
                            )

                            if (isAdmin) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                                // Option 4: Admin command center
                                YangaFunButton(
                                    text = "Admin Control Panel 👑🔒",
                                    onClick = { activePanel = "admin_portal" },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTagStr = "admin_control_panel_btn"
                                )
                            } else if (isModerator) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                                // Option 5: Moderator command center
                                YangaFunButton(
                                    text = "Moderator Control Panel 🛡️🔒",
                                    onClick = { activePanel = "moderator_portal" },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTagStr = "moderator_control_panel_btn"
                                )
                            }
                        }
                    }
                } else {
                    YangaPlayfulCard(
                        backgroundColor = Color.White,
                        borderColor = PrimaryPurple,
                        borderWidth = 1.5,
                        modifier = Modifier.fillMaxWidth().testTag("active_panel_card")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header with Back Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when (activePanel) {
                                        "register_freelancer" -> "Freelancer Application 👦"
                                        "register_business" -> "Business Application 🏢"
                                        "register_event" -> "Register an Event 🎟️"
                                        "admin_portal" -> "Admin Verification Portal 👑"
                                        "moderator_portal" -> "Moderator Control Panel 🛡️"
                                        "edit_freelancer" -> "Manage Freelancer Profile 👦"
                                        "edit_business" -> "Manage Business Profile 🏢"
                                        else -> ""
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryPurple
                                )
                                IconButton(
                                    onClick = { activePanel = null },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(SecondaryYellow, CircleShape)
                                        .border(1.dp, PrimaryPurple, CircleShape)
                                        .testTag("close_panel_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Panel", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            when (activePanel) {
                                "register_freelancer" -> {
                                    // Freelancer registration form
                                    OutlinedTextField(
                                        value = freeName,
                                        onValueChange = { freeName = it },
                                        label = { Text("Your Professional Name") },
                                        placeholder = { Text("e.g. Eniola Agbeyindo") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_name_input")
                                    )
                                    OutlinedTextField(
                                        value = freeTitle,
                                        onValueChange = { freeTitle = it },
                                        label = { Text("Professional Title / Profession") },
                                        placeholder = { Text("e.g. Lead Software Architect") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_title_input")
                                    )
                                    OutlinedTextField(
                                        value = freeLinkedin,
                                        onValueChange = { freeLinkedin = it },
                                        label = { Text("LinkedIn URL") },
                                        placeholder = { Text("https://linkedin.com/in/username") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_linkedin_input")
                                    )
                                    OutlinedTextField(
                                        value = freeGithub,
                                        onValueChange = { freeGithub = it },
                                        label = { Text("GitHub URL") },
                                        placeholder = { Text("https://github.com/username") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_github_input")
                                    )
                                    OutlinedTextField(
                                        value = freeBackPhoto,
                                        onValueChange = { freeBackPhoto = it },
                                        label = { Text("Portfolio Banner / Back Photo Description") },
                                        placeholder = { Text("e.g. Lagos Skyline, Neon Office Setup") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_back_photo_input")
                                    )
                                    OutlinedTextField(
                                        value = freeNormalPhoto,
                                        onValueChange = { freeNormalPhoto = it },
                                        label = { Text("Profile Photo / Normal Photo Description") },
                                        placeholder = { Text("e.g. Playful Avatar with glasses") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_normal_photo_input")
                                    )
                                    OutlinedTextField(
                                        value = freeSkills,
                                        onValueChange = { freeSkills = it },
                                        label = { Text("Your Core Skills (comma separated)") },
                                        placeholder = { Text("e.g. Software, Kotlin, Compose, UI/UX") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_skills_input")
                                    )
                                    OutlinedTextField(
                                        value = freeBio,
                                        onValueChange = { freeBio = it },
                                        label = { Text("Professional Bio") },
                                        placeholder = { Text("Tell us what you do best...") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_bio_input")
                                    )
                                    OutlinedTextField(
                                        value = freeBasePrice,
                                        onValueChange = { freeBasePrice = it },
                                        label = { Text("Your Base Price (₦ / hr)") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("free_price_input")
                                    )

                                    Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        OutlinedTextField(
                                            value = freeCategory,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                IconButton(onClick = { isFreeCategoryExpanded = !isFreeCategoryExpanded }, modifier = Modifier.testTag("free_category_dropdown_icon")) {
                                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().clickable { isFreeCategoryExpanded = !isFreeCategoryExpanded }.testTag("free_category_input")
                                        )
                                        DropdownMenu(expanded = isFreeCategoryExpanded, onDismissRequest = { isFreeCategoryExpanded = false }, modifier = Modifier.fillMaxWidth()) {
                                            listOf("Software", "Creative arts", "Business", "Other").forEach { cat ->
                                                DropdownMenuItem(text = { Text(cat) }, onClick = { freeCategory = cat; isFreeCategoryExpanded = false })
                                            }
                                        }
                                    }

                                    YangaFunButton(
                                        text = "Submit Application 🚀",
                                        onClick = {
                                            if (freeName.isNotBlank() && freeTitle.isNotBlank() && freeSkills.isNotBlank() && freeBio.isNotBlank()) {
                                                viewModel.submitFreelancerApplication(
                                                    name = freeName,
                                                    title = freeTitle,
                                                    linkedinUrl = freeLinkedin,
                                                    githubUrl = freeGithub,
                                                    backPhotoUrl = freeBackPhoto,
                                                    normalPhotoUrl = freeNormalPhoto,
                                                    skills = freeSkills,
                                                    bio = freeBio,
                                                    basePrice = freeBasePrice.toDoubleOrNull() ?: 5000.0,
                                                    category = freeCategory
                                                )
                                                activePanel = null
                                            } else {
                                                viewModel.postError("Please fill out Name, Title, Skills and Bio!")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTagStr = "submit_freelancer_app_btn"
                                    )
                                }
                                "register_business" -> {
                                    // Business registration form
                                    OutlinedTextField(
                                        value = bizName,
                                        onValueChange = { bizName = it },
                                        label = { Text("Business / Store / Clinic Name") },
                                        placeholder = { Text("e.g. Yanga Fresh Bakery") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("biz_name_input")
                                    )
                                    
                                    Text("Business Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                        OutlinedTextField(
                                            value = bizCategory,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                IconButton(onClick = { isBizCategoryExpanded = !isBizCategoryExpanded }, modifier = Modifier.testTag("biz_category_dropdown_icon")) {
                                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().clickable { isBizCategoryExpanded = !isBizCategoryExpanded }.testTag("biz_category_input")
                                        )
                                        DropdownMenu(expanded = isBizCategoryExpanded, onDismissRequest = { isBizCategoryExpanded = false }, modifier = Modifier.fillMaxWidth()) {
                                            listOf("Hospital", "Care Center", "Pharmacy", "Restaurant", "Bakery", "Confectionery", "Retail").forEach { cat ->
                                                DropdownMenuItem(text = { Text(cat) }, onClick = { bizCategory = cat; isBizCategoryExpanded = false })
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = bizCac,
                                        onValueChange = { bizCac = it },
                                        label = { Text("CAC Registration Number") },
                                        placeholder = { Text("e.g. RC-948210") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("biz_cac_input")
                                    )
                                    OutlinedTextField(
                                        value = bizLocation,
                                        onValueChange = { bizLocation = it },
                                        label = { Text("Physical Business Location") },
                                        placeholder = { Text("e.g. 45 Admiralty Way, Lekki Phase 1") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("biz_location_input")
                                    )
                                    OutlinedTextField(
                                        value = bizImage,
                                        onValueChange = { bizImage = it },
                                        label = { Text("Business Storefront Image description") },
                                        placeholder = { Text("e.g. Modern glass storefront, bright signage") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("biz_image_input")
                                    )
                                    OutlinedTextField(
                                        value = bizServices,
                                        onValueChange = { bizServices = it },
                                        label = { Text("Initial Services / Courses / Items (comma separated)") },
                                        placeholder = { Text("e.g. Croissant, Beef Sausage, Jollof Rice") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("biz_services_input")
                                    )

                                    YangaFunButton(
                                        text = "Submit Business Application 🏢",
                                        onClick = {
                                            if (bizName.isNotBlank() && bizCac.isNotBlank() && bizLocation.isNotBlank() && bizServices.isNotBlank()) {
                                                viewModel.submitBusinessApplication(
                                                    name = bizName,
                                                    category = bizCategory,
                                                    cacNumber = bizCac,
                                                    location = bizLocation,
                                                    imageDescription = bizImage,
                                                    services = bizServices
                                                )
                                                activePanel = null
                                            } else {
                                                viewModel.postError("Please fill out Name, CAC number, Location and Services!")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTagStr = "submit_business_app_btn"
                                    )
                                }
                                "register_event" -> {
                                    // Event registration form
                                    OutlinedTextField(
                                        value = evtTitle,
                                        onValueChange = { evtTitle = it },
                                        label = { Text("Event Title") },
                                        placeholder = { Text("e.g. Lekki Friday Beach Rave") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_title_input")
                                    )
                                    OutlinedTextField(
                                        value = evtDescription,
                                        onValueChange = { evtDescription = it },
                                        label = { Text("Event Description / About") },
                                        placeholder = { Text("Write about the vibes, lineup, and rules...") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_description_input")
                                    )
                                    OutlinedTextField(
                                        value = evtHost,
                                        onValueChange = { evtHost = it },
                                        label = { Text("Event Host") },
                                        placeholder = { Text("e.g. Yanga Entertainment") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_host_input")
                                    )
                                    OutlinedTextField(
                                        value = evtDate,
                                        onValueChange = { evtDate = it },
                                        label = { Text("Date") },
                                        placeholder = { Text("e.g. July 25, 2026") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_date_input")
                                    )
                                    OutlinedTextField(
                                        value = evtTime,
                                        onValueChange = { evtTime = it },
                                        label = { Text("Time") },
                                        placeholder = { Text("e.g. 18:00") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_time_input")
                                    )
                                    OutlinedTextField(
                                        value = evtVenue,
                                        onValueChange = { evtVenue = it },
                                        label = { Text("Venue / Location") },
                                        placeholder = { Text("e.g. Elegushi Beach, Lekki") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_venue_input")
                                    )
                                    OutlinedTextField(
                                        value = evtCoupon,
                                        onValueChange = { evtCoupon = it },
                                        label = { Text("Promo/Coupon Code") },
                                        placeholder = { Text("e.g. BEACHVIBES") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("evt_coupon_input")
                                    )
                                    OutlinedTextField(
                                        value = evtImageDesc,
                                        onValueChange = { evtImageDesc = it },
                                        label = { Text("Event Poster / Image description") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("evt_image_input")
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = evtIsFree, onCheckedChange = { evtIsFree = it }, modifier = Modifier.testTag("evt_free_checkbox"))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("This is a Free Event (Free RSVP passes)", fontSize = 12.sp, color = CharcoalBlack)
                                    }

                                    YangaFunButton(
                                        text = "Publish & Submit Event 🎟️",
                                        onClick = {
                                            if (evtTitle.isNotBlank() && evtDescription.isNotBlank() && evtVenue.isNotBlank() && evtHost.isNotBlank()) {
                                                viewModel.submitEventApplication(
                                                    title = evtTitle,
                                                    description = evtDescription,
                                                    isFree = evtIsFree,
                                                    couponCode = evtCoupon,
                                                    host = evtHost,
                                                    date = evtDate.ifBlank { "July 30, 2026" },
                                                    time = evtTime.ifBlank { "19:00" },
                                                    venue = evtVenue,
                                                    imageDescription = evtImageDesc
                                                )
                                                activePanel = null
                                            } else {
                                                viewModel.postError("Please fill out Title, Description, Host and Venue!")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTagStr = "submit_event_app_btn"
                                    )
                                }
                                "admin_portal" -> {
                                    // Admin panel to approve/reject, manage moderators, do notifications, add insights
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // TAB 1: VERIFICATIONS
                                        Text("👑 Admin Verification Desk", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Pending freelancers
                                        Text("Pending Talent Submissions (${pendingFreelancers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                        if (pendingFreelancers.isEmpty()) {
                                            Text("No pending freelancers. All clear! 🟢", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                        } else {
                                            pendingFreelancers.forEach { free ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                         Text(free.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                                         Text(free.title + " (${free.category})", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryPurple)
                                                         Text("Skills: " + free.skills, fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                                                         Text("Bio: " + free.bio, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.8f))
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                         ) {
                                                             Button(
                                                                 onClick = { viewModel.approveFreelancer(free.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("approve_free_${free.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Approve ✅", fontSize = 11.sp)
                                                             }
                                                             Button(
                                                                 onClick = { viewModel.rejectFreelancer(free.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("reject_free_${free.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Reject ❌", fontSize = 11.sp)
                                                             }
                                                         }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                                        // Pending businesses
                                        Text("Pending Business Submissions (${pendingBusinesses.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                        if (pendingBusinesses.isEmpty()) {
                                            Text("No pending businesses. All clear! 🟢", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                        } else {
                                            pendingBusinesses.forEach { biz ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                         Text(biz.name + " [${biz.category}]", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                                         Text("CAC Number: " + biz.cacNumber, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryPurple)
                                                         Text("Location: " + biz.location, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.8f))
                                                         Text("Services/Products: " + biz.services, fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                         ) {
                                                             Button(
                                                                 onClick = { viewModel.approveBusiness(biz.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("approve_biz_${biz.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Approve ✅", fontSize = 11.sp)
                                                             }
                                                             Button(
                                                                 onClick = { viewModel.rejectBusiness(biz.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("reject_biz_${biz.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Reject ❌", fontSize = 11.sp)
                                                             }
                                                         }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                                        // Pending events
                                        Text("Pending Event Submissions (${pendingEvents.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                        if (pendingEvents.isEmpty()) {
                                            Text("No pending events. All clear! 🟢", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                        } else {
                                            pendingEvents.forEach { ev ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                         Text(ev.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                                         Text("Host: " + ev.host + " | Venue: " + ev.venue, fontSize = 11.sp, color = PrimaryPurple)
                                                         Text("Coupon: " + ev.couponCode + " | Is Free: " + ev.isFree, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                                                         Text("Details: " + ev.description, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.8f))
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                         ) {
                                                             Button(
                                                                 onClick = { viewModel.approveEvent(ev.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("approve_evt_${ev.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Approve ✅", fontSize = 11.sp)
                                                             }
                                                             Button(
                                                                 onClick = { viewModel.rejectEvent(ev.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("reject_evt_${ev.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Reject ❌", fontSize = 11.sp)
                                                             }
                                                         }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)

                                        // TAB 2: MODERATOR MANAGEMENT
                                        Text("🛡️ Manage Moderators List", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                             value = newModEmail,
                                             onValueChange = { newModEmail = it },
                                             label = { Text("Add Moderator Email") },
                                             placeholder = { Text("e.g. mod1@yanga.live") },
                                             modifier = Modifier.fillMaxWidth().testTag("mod_email_input"),
                                             singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        YangaFunButton(
                                             text = "Add Moderator Email ➕",
                                             onClick = {
                                                 if (newModEmail.isNotBlank()) {
                                                     viewModel.addModerator(newModEmail)
                                                     newModEmail = ""
                                                 }
                                             },
                                             modifier = Modifier.fillMaxWidth(),
                                             testTagStr = "add_moderator_btn"
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Existing Moderators (${moderators.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        if (moderators.isEmpty()) {
                                             Text("No moderators configured yet.", fontSize = 11.sp, color = Color.Gray)
                                        } else {
                                             moderators.forEach { modEmail ->
                                                 Row(
                                                     modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                     verticalAlignment = Alignment.CenterVertically,
                                                     horizontalArrangement = Arrangement.SpaceBetween
                                                 ) {
                                                     Text("• $modEmail", fontSize = 12.sp, color = CharcoalBlack)
                                                     IconButton(
                                                         onClick = { viewModel.removeModerator(modEmail) },
                                                         modifier = Modifier.size(28.dp)
                                                     ) {
                                                         Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                     }
                                                 }
                                             }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)

                                        // TAB 3: BROADCAST NOTIFICATIONS
                                        Text("📢 Broadcast Custom Notifications", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                             value = adminNotifTitle,
                                             onValueChange = { adminNotifTitle = it },
                                             label = { Text("Notification Title") },
                                             modifier = Modifier.fillMaxWidth(),
                                             singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                             value = adminNotifMessage,
                                             onValueChange = { adminNotifMessage = it },
                                             label = { Text("Notification Message") },
                                             modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        YangaFunButton(
                                             text = "Send Broadcast 🚀",
                                             onClick = {
                                                 if (adminNotifTitle.isNotBlank() && adminNotifMessage.isNotBlank()) {
                                                     viewModel.addNotification(adminNotifTitle, adminNotifMessage, "BROADCAST", "📢")
                                                     adminNotifTitle = ""
                                                     adminNotifMessage = ""
                                                 }
                                             },
                                             modifier = Modifier.fillMaxWidth(),
                                             testTagStr = "send_broadcast_btn"
                                         )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)

                                        // TAB 4: MARKET INSIGHTS
                                        Text("💡 Add Market Insight / community Vibe", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                             value = insightAuthor,
                                             onValueChange = { insightAuthor = it },
                                             label = { Text("Author Handle/Name") },
                                             modifier = Modifier.fillMaxWidth(),
                                             singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                             value = insightText,
                                             onValueChange = { insightText = it },
                                             label = { Text("Insight Text") },
                                             modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                         
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                             OutlinedButton(
                                                 onClick = { isInsightCategoryExpanded = true },
                                                 modifier = Modifier.fillMaxWidth()
                                             ) {
                                                 Text("Category: $insightCategory ▾", color = PrimaryPurple)
                                             }
                                             DropdownMenu(
                                                 expanded = isInsightCategoryExpanded,
                                                 onDismissRequest = { isInsightCategoryExpanded = false }
                                             ) {
                                                 listOf("Fashion", "Electronics", "Groceries", "Business", "Motivation").forEach { cat ->
                                                     DropdownMenuItem(
                                                         text = { Text(cat) },
                                                         onClick = {
                                                             insightCategory = cat
                                                             isInsightCategoryExpanded = false
                                                         }
                                                     )
                                                 }
                                             }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        YangaFunButton(
                                             text = "Add Insight / Vibe 💡",
                                             onClick = {
                                                 if (insightAuthor.isNotBlank() && insightText.isNotBlank()) {
                                                     viewModel.addMarketInsight(insightAuthor, insightText, insightCategory)
                                                     insightAuthor = ""
                                                     insightText = ""
                                                 }
                                             },
                                             modifier = Modifier.fillMaxWidth(),
                                             testTagStr = "add_insight_btn"
                                        )
                                    }
                                }
                                "moderator_portal" -> {
                                    // Moderator panel: ONLY verifications! NO moderators, NO broadcast, NO insights
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("🛡️ Moderator Verification Desk", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Pending freelancers
                                        Text("Pending Talent Submissions (${pendingFreelancers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                        if (pendingFreelancers.isEmpty()) {
                                            Text("No pending freelancers. All clear! 🟢", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                        } else {
                                            pendingFreelancers.forEach { free ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                         Text(free.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                                         Text(free.title + " (${free.category})", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryPurple)
                                                         Text("Skills: " + free.skills, fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                                                         Text("Bio: " + free.bio, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.8f))
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                         ) {
                                                             Button(
                                                                 onClick = { viewModel.approveFreelancer(free.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("mod_approve_free_${free.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Approve ✅", fontSize = 11.sp)
                                                             }
                                                             Button(
                                                                 onClick = { viewModel.rejectFreelancer(free.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("mod_reject_free_${free.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Reject ❌", fontSize = 11.sp)
                                                             }
                                                         }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                                        // Pending businesses
                                        Text("Pending Business Submissions (${pendingBusinesses.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                        if (pendingBusinesses.isEmpty()) {
                                            Text("No pending businesses. All clear! 🟢", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                        } else {
                                            pendingBusinesses.forEach { biz ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                         Text(biz.name + " [${biz.category}]", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                                         Text("CAC Number: " + biz.cacNumber, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryPurple)
                                                         Text("Location: " + biz.location, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.8f))
                                                         Text("Services/Products: " + biz.services, fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                         ) {
                                                             Button(
                                                                 onClick = { viewModel.approveBusiness(biz.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("mod_approve_biz_${biz.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Approve ✅", fontSize = 11.sp)
                                                             }
                                                             Button(
                                                                 onClick = { viewModel.rejectBusiness(biz.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("mod_reject_biz_${biz.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Reject ❌", fontSize = 11.sp)
                                                             }
                                                         }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                                        // Pending events
                                        Text("Pending Event Submissions (${pendingEvents.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                        if (pendingEvents.isEmpty()) {
                                            Text("No pending events. All clear! 🟢", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                        } else {
                                            pendingEvents.forEach { ev ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                         Text(ev.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                                         Text("Host: " + ev.host + " | Venue: " + ev.venue, fontSize = 11.sp, color = PrimaryPurple)
                                                         Text("Coupon: " + ev.couponCode + " | Is Free: " + ev.isFree, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f))
                                                         Text("Details: " + ev.description, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.8f))
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Row(
                                                             modifier = Modifier.fillMaxWidth(),
                                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                         ) {
                                                             Button(
                                                                 onClick = { viewModel.approveEvent(ev.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("mod_approve_evt_${ev.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Approve ✅", fontSize = 11.sp)
                                                             }
                                                             Button(
                                                                 onClick = { viewModel.rejectEvent(ev.id) },
                                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                                 modifier = Modifier.weight(1f).height(36.dp).testTag("mod_reject_evt_${ev.id}"),
                                                                 shape = RoundedCornerShape(6.dp)
                                                             ) {
                                                                 Text("Reject ❌", fontSize = 11.sp)
                                                             }
                                                         }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                "edit_freelancer" -> {
                                    // Edit/View Approved Freelancer Profile
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("Your Freelancer profile is live! Add more detail below to stand out in the directory.", fontSize = 12.sp, color = CharcoalBlack.copy(alpha = 0.75f))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = additionalSkills,
                                            onValueChange = { additionalSkills = it },
                                            label = { Text("Add More Skills (comma separated)") },
                                            placeholder = { Text("e.g. Node.js, GraphQL, Docker") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("additional_skills_input")
                                        )
                                        OutlinedTextField(
                                            value = portfolioItemTitle,
                                            onValueChange = { portfolioItemTitle = it },
                                            label = { Text("Add New Portfolio Item / Project Name") },
                                            placeholder = { Text("e.g. Yanga Wallet Core Redesign") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("portfolio_item_input")
                                        )

                                        YangaFunButton(
                                            text = "Update Portfolio & Skills 🚀",
                                            onClick = {
                                                if (additionalSkills.isNotBlank() || portfolioItemTitle.isNotBlank()) {
                                                    viewModel.updateFreelancerSkillsAndPortfolio(additionalSkills, portfolioItemTitle)
                                                    additionalSkills = ""
                                                    portfolioItemTitle = ""
                                                    activePanel = null
                                                } else {
                                                    viewModel.postError("Please specify some skills or a portfolio item title!")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            testTagStr = "update_freelancer_profile_btn"
                                        )
                                    }
                                }
                                "edit_business" -> {
                                    // Edit/View Approved Business Profile
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        val isHospitalType = myBusinessCategory == "Hospital" || myBusinessCategory == "Care Center" || myBusinessCategory == "Pharmacy"
                                        val isFoodType = myBusinessCategory == "Restaurant" || myBusinessCategory == "Bakery" || myBusinessCategory == "Confectionery"
                                        val isRetailType = myBusinessCategory == "Retail"

                                        Text("Manage and expand your '$myBusinessCategory' offerings on Yanga Superapp.", fontSize = 12.sp, color = CharcoalBlack.copy(alpha = 0.75f))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = newItemName,
                                            onValueChange = { newItemName = it },
                                            label = { 
                                                Text(
                                                    when {
                                                        isHospitalType -> "Add New Healthcare specialty / Test / Service"
                                                        isFoodType -> "Add New Menu Course / Baked Good Name"
                                                        else -> "Add New Shop Item / Product Name"
                                                    }
                                                )
                                            },
                                            placeholder = { 
                                                Text(
                                                    when {
                                                        isHospitalType -> "e.g. Free Blood Pressure Check"
                                                        isFoodType -> "e.g. Spicy Seafood Platter"
                                                        else -> "e.g. Yanga Special Hoodie"
                                                    }
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("new_item_name_input")
                                        )

                                        if (!isHospitalType) {
                                            // Price field for food and retail
                                            OutlinedTextField(
                                                value = newItemPrice,
                                                onValueChange = { newItemPrice = it },
                                                label = { Text("Price (₦)") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("new_item_price_input")
                                            )
                                        }

                                        OutlinedTextField(
                                            value = newItemDescription,
                                            onValueChange = { newItemDescription = it },
                                            label = { Text("Short Offering Description") },
                                            placeholder = { Text("Describe what makes this item or test special...") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("new_item_desc_input")
                                        )

                                        YangaFunButton(
                                            text = when {
                                                isHospitalType -> "Add Medical Service 🩺"
                                                isFoodType -> "Add Menu Course 🍔"
                                                else -> "Add Retail Product 🛒"
                                            },
                                            onClick = {
                                                if (newItemName.isNotBlank() && newItemDescription.isNotBlank()) {
                                                    viewModel.addBusinessItem(
                                                        itemName = newItemName,
                                                        itemDescription = newItemDescription,
                                                        itemPrice = newItemPrice.toDoubleOrNull() ?: 2500.0
                                                    )
                                                    newItemName = ""
                                                    newItemDescription = ""
                                                    activePanel = null
                                                } else {
                                                    viewModel.postError("Please fill out name and description!")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            testTagStr = "add_business_item_btn"
                                        )
                                    }
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
