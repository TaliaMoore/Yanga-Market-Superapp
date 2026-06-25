package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val freelancers by viewModel.freelancers.collectAsState()
    val escrowBookings by viewModel.escrowBookings.collectAsState()

    var selectedFreelancer by remember { mutableStateOf<FreelancerProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Yanga Services Marketplace 💼",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Verified local talent. Secured Escrow Ledger.",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (selectedFreelancer != null) {
                        IconButton(onClick = { selectedFreelancer = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to list",
                                tint = PrimaryPurple
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PlayfulBg
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PlayfulBg)
        ) {
            if (selectedFreelancer != null) {
                // High-Fidelity Detail Overlay Sheet
                FreelancerDetailView(
                    profile = selectedFreelancer!!,
                    onBack = { selectedFreelancer = null },
                    viewModel = viewModel,
                    escrowBookings = escrowBookings
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    FreelancersListTab(
                        freelancers = freelancers,
                        onSelect = { selectedFreelancer = it }
                    )
                }
            }
        }
    }
}

// --- EXPLORE TALENT TAB ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FreelancersListTab(
    freelancers: List<FreelancerProfile>,
    onSelect: (FreelancerProfile) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Explicit list of categories requested by user
    val categories = listOf(
        "All",
        "Software",
        "Business",
        "Finance",
        "Engineering",
        "Software development",
        "Creative arts"
    )

    // Suggestion chips for skills that are commonly needed (data entry, typing, printing)
    val popularSkills = listOf("data entry", "typing", "printing", "excel", "software", "creative", "planning")

    // Filter freelancers based on category and search query
    val filteredFreelancers = freelancers.filter { f ->
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "Software" -> f.category.equals("Software", ignoreCase = true)
            "Software development" -> f.category.equals("Software", ignoreCase = true) || f.skills.any { it.contains("Software", ignoreCase = true) }
            else -> f.category.equals(selectedCategory, ignoreCase = true)
        }

        val matchesQuery = if (searchQuery.isBlank()) {
            true
        } else {
            f.name.contains(searchQuery, ignoreCase = true) ||
            f.title.contains(searchQuery, ignoreCase = true) ||
            f.bio.contains(searchQuery, ignoreCase = true) ||
            f.category.contains(searchQuery, ignoreCase = true) ||
            f.skills.any { it.contains(searchQuery, ignoreCase = true) }
        }

        matchesCategory && matchesQuery
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontally Scrollable Categories
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val selected = selectedCategory == cat
                FilterChip(
                    selected = selected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple,
                        selectedLabelColor = Color.White,
                        containerColor = WarmCardWhite,
                        labelColor = CharcoalBlack
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = PrimaryPurple.copy(alpha = if (selected) 1f else 0.15f),
                        enabled = true,
                        selected = selected
                    ),
                    modifier = Modifier.testTag("filter_chip_$cat")
                )
            }
        }

        // Beautiful Search Bar for skills
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search skills (e.g. data entry, typing, printing)...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PrimaryPurple.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = PrimaryPurple.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("skills_search_bar")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick skill pickers list
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pick skill:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 6.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(popularSkills) { skill ->
                        val isSelected = searchQuery.equals(skill, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    searchQuery = if (isSelected) "" else skill
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = skill,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else PrimaryPurple
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredFreelancers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No freelancers matching these criteria.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for another skill like 'typing' or 'data entry'.",
                        fontSize = 12.sp,
                        color = CharcoalBlack.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("freelancers_list")
            ) {
                items(filteredFreelancers) { item ->
                    FreelancerCard(profile = item, onClick = { onSelect(item) })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FreelancerCard(
    profile: FreelancerProfile,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("freelancer_card_${profile.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display profile photo if available, fallback to emoji
                if (profile.profileImageRes != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = profile.profileImageRes),
                        contentDescription = profile.name,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryPurple, CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SecondaryYellow, PrimaryPurple.copy(alpha = 0.15f))
                                ), CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.avatarEmoji, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                    Text(
                        text = profile.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dynamic Rating Badges (calculating active rating averages)
                Row(
                    modifier = Modifier
                        .background(PaleYellow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", profile.calculateAverageRating()),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.bio,
                fontSize = 12.sp,
                color = CharcoalBlack.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            // Dynamic skill tags directly on the card
            if (profile.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profile.skills.take(4).forEach { skill ->
                        Box(
                            modifier = Modifier
                                .background(PrimaryPurple.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(0.5.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = skill,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }
                    }
                    if (profile.skills.size > 4) {
                        Box(
                            modifier = Modifier
                                .background(PaleYellow, RoundedCornerShape(12.dp))
                                .border(0.5.dp, BrandOrange.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "+${profile.skills.size - 4}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalBlack
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = PrimaryPurple.copy(alpha = 0.06f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(PrimaryPurple.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = profile.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "From ",
                        fontSize = 11.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "₦%,.0f", profile.basePrice),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandGreen
                    )
                    Text(
                        text = "/proj",
                        fontSize = 10.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// --- DETAILED FREELANCER OVERLAY VIEW ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FreelancerDetailView(
    profile: FreelancerProfile,
    onBack: () -> Unit,
    viewModel: MainViewModel,
    escrowBookings: List<EscrowProjectBooking>
) {
    // Collect wallet transactions to verify balance
    val txs by viewModel.walletTransactions.collectAsState(initial = emptyList())
    val currentBalance = 10000.0 + txs.map {
        if (it.type == "FUND") it.amount else -it.amount
    }.sum()

    var activeDetailTab by remember { mutableIntStateOf(0) } // 0 = Services & Portfolio, 1 = Testimonials & Reviews
    var showBookingForm by remember { mutableStateOf(false) }

    // Booking Configurer State
    var selectedPackageIndex by remember { mutableIntStateOf(0) }
    var splitMilestones by remember { mutableStateOf(true) } // true: 2 milestones, false: Single milestone

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(PlayfulBg)
    ) {
        // Brand banner backing header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryPurple, PrimaryPurple.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular back chevron
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }

                Box(
                    modifier = Modifier
                        .background(SecondaryYellow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = profile.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack
                    )
                }
            }
        }

        // Freelancer Profile Summary Card overlaps slightly
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-16).dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.profileImageRes != null) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = profile.profileImageRes),
                            contentDescription = profile.name,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(3.dp, PrimaryPurple, CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(SecondaryYellow, CircleShape)
                                .border(3.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(profile.avatarEmoji, fontSize = 38.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = profile.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack
                        )
                        Text(
                            text = profile.title,
                            fontSize = 13.sp,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // LinkedIn and GitHub connect buttons
                if (profile.linkedinUrl != null || profile.githubUrl != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Connect:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalBlack.copy(alpha = 0.5f)
                        )
                        profile.linkedinUrl?.let { link ->
                            Card(
                                onClick = { /* simulated link tap */ },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0077B5)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "LinkedIn",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LinkedIn Profile",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        profile.githubUrl?.let { link ->
                            Card(
                                onClick = { /* simulated link tap */ },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF181717)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "GitHub",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GitHub",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.bio,
                    fontSize = 13.sp,
                    color = CharcoalBlack.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                if (profile.skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Expertise & Skills",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.skills.forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .background(PrimaryPurple.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = skill,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Yanga Secured",
                            fontSize = 11.sp,
                            color = BrandGreen,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", profile.calculateAverageRating()) + " / 5.0 (${profile.reviews.size} reviews)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalBlack
                        )
                    }
                }
            }
        }

        // Mini Navigation Row for detail sections
        TabRow(
            selectedTabIndex = activeDetailTab,
            containerColor = PlayfulBg,
            contentColor = PrimaryPurple,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeDetailTab == 0,
                onClick = { activeDetailTab = 0 },
                text = { Text("Portfolio & Services", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeDetailTab == 1,
                onClick = { activeDetailTab = 1 },
                text = { Text("Client Reviews", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (activeDetailTab == 0) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Portfolio Gallery Headers
                Text(
                    text = "Featured Portfolio Projects 🎨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Render portfolio images and descriptions if available
                if (profile.portfolioImages.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(profile.portfolioImages.zip(profile.portfolioGallery)) { (imgRes, text) ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                                border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .width(220.dp)
                            ) {
                                Column {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = imgRes),
                                        contentDescription = text,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = text,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CharcoalBlack,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Fallback to text tags if no images
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.portfolioGallery.forEach { work ->
                            Box(
                                modifier = Modifier
                                    .background(WarmCardWhite, RoundedCornerShape(12.dp))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = work,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Service Packages
                Text(
                    text = "Select Service Packages 📋",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                profile.serviceListings.forEachIndexed { idx, listing ->
                    val isSelected = selectedPackageIndex == idx && showBookingForm
                    Card(
                        onClick = {
                            selectedPackageIndex = idx
                            showBookingForm = true
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PlayfulCream else WarmCardWhite
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (isSelected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .testTag("service_listing_$idx")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isSelected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.CardTravel,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else PrimaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = listing,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack
                                )
                                Text(
                                    text = "Locked Escrow Guarantee",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = String.format(java.util.Locale.US, "₦%,.0f", profile.basePrice),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalBlack
                            )
                        }
                    }
                }

                // Booking Configuration Form
                if (showBookingForm) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PlayfulCream),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_config_form")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Secure Escrow Setup 🔒",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                            Text(
                                text = "Your money is held securely in the Escrow ledger vault. Only release it to the freelancer when milestones are completed to your absolute satisfaction.",
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(alpha = 0.7f),
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Select Milestone Delivery System:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBlack
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Split Milestones Options
                                Card(
                                    onClick = { splitMilestones = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (splitMilestones) WarmCardWhite else PlayfulCream
                                    ),
                                    border = BorderStroke(1.5.dp, if (splitMilestones) PrimaryPurple else Color.Gray.copy(0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Split Milestones", fontSize = 11.sp, fontWeight = FontWeight.Black, color = CharcoalBlack)
                                        Text("40% / 60% Split", fontSize = 9.sp, color = CharcoalBlack.copy(0.6f))
                                    }
                                }

                                Card(
                                    onClick = { splitMilestones = false },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!splitMilestones) WarmCardWhite else PlayfulCream
                                    ),
                                    border = BorderStroke(1.5.dp, if (!splitMilestones) PrimaryPurple else Color.Gray.copy(0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Single Payout", fontSize = 11.sp, fontWeight = FontWeight.Black, color = CharcoalBlack)
                                        Text("100% Release", fontSize = 9.sp, color = CharcoalBlack.copy(0.6f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Structured Escrow Milestones Ledger:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Construct Milestones list visually
                            val milestoneItems = if (splitMilestones) {
                                listOf(
                                    "Milestone 1: Prototype, Drafts & Layout Review (40%)" to (profile.basePrice * 0.4),
                                    "Milestone 2: Final Handover, Quality Check & Launch (60%)" to (profile.basePrice * 0.6)
                                )
                            } else {
                                listOf(
                                    "Milestone 1: Complete Project Delivery and Acceptance (100%)" to profile.basePrice
                                )
                            }

                            milestoneItems.forEachIndexed { i, m ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(WarmCardWhite, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(PrimaryPurple, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                (i + 1).toString(),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = m.first,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalBlack,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        text = String.format(java.util.Locale.US, "₦%,.0f", m.second),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BrandGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Wallet sufficiency check
                            val balanceSufficient = currentBalance >= profile.basePrice

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (balanceSufficient) BrandGreen.copy(0.08f) else HeartRed.copy(0.08f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (balanceSufficient) Icons.Default.Wallet else Icons.Default.Warning,
                                    tint = if (balanceSufficient) BrandGreen else HeartRed,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (balanceSufficient) {
                                        "Wallet balance sufficient: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}"
                                    } else {
                                        "Insufficient Balance! Balance: ₦${String.format(java.util.Locale.US, "%,.2f", currentBalance)}. Project: ₦${String.format(java.util.Locale.US, "%,.2f", profile.basePrice)}"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balanceSufficient) CharcoalBlack else HeartRed
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val titles = milestoneItems.map { it.first }
                                    val amounts = milestoneItems.map { it.second }
                                    viewModel.hireFreelancer(
                                        freelancerId = profile.id,
                                        selectedService = profile.serviceListings[selectedPackageIndex],
                                        amount = profile.basePrice,
                                        milestoneTitles = titles,
                                        milestoneAmounts = amounts,
                                        onSuccess = {
                                            showBookingForm = false
                                            onBack() // Back to lists on success
                                        }
                                    )
                                },
                                enabled = balanceSufficient,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("hire_confirm_button")
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fund Escrow & Hire ${profile.name}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Client Testimonials & Review posting system
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What Clients Share 💬",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                profile.reviews.forEach { r ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.05f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(r.reviewerName, fontWeight = FontWeight.Black, fontSize = 12.sp, color = CharcoalBlack)
                                Row {
                                    repeat(r.rating) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = BoldGold, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = r.comment,
                                fontSize = 11.sp,
                                color = CharcoalBlack.copy(0.75f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Post a review
                Text(
                    text = "Leave a Talent Feedback ⭐",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                var reviewText by remember { mutableStateOf(TextFieldValue("")) }
                var draftRating by remember { mutableIntStateOf(5) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = PlayfulCream),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rate service quality: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Row {
                                repeat(5) { starIdx ->
                                    val active = starIdx < draftRating
                                    IconButton(
                                        onClick = { draftRating = starIdx + 1 },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (active) BoldGold else Color.Gray.copy(0.4f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = { Text("E.g., Delivered very clean, verified code components!", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = Color.Gray.copy(0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("review_input_text")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (reviewText.text.isNotBlank()) {
                                    viewModel.submitFreelancerReview(
                                        freelancerId = profile.id,
                                        reviewerName = "Verified Client",
                                        rating = draftRating,
                                        comment = reviewText.text
                                    )
                                    reviewText = TextFieldValue("")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("submit_review_button")
                        ) {
                            Text("Submit Review", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- PROJECT TRACKING AND ESCROW VAULT TAB ---
@Composable
fun EscrowTrackingTab(
    bookings: List<EscrowProjectBooking>,
    viewModel: MainViewModel
) {
    if (bookings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(PrimaryPurple.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Secured Escrow Vault is Empty",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hire premium freelancers from the explore tab! Funds will be securely placed under Escrow here.",
                    fontSize = 12.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("escrow_tracking_list")
        ) {
            items(bookings) { booking ->
                EscrowProjectCard(booking = booking, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EscrowProjectCard(
    booking: EscrowProjectBooking,
    viewModel: MainViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("escrow_project_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.selectedService,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Freelancer: ${booking.freelancerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }

                // Overall Status Badge
                val (statusLabel, badgeColor, textColor) = when (booking.currentStatus) {
                    ServiceBookingStatus.ESCROW_HELD -> Triple("Escrow Held 🔒", PaleYellow, CharcoalBlack)
                    ServiceBookingStatus.IN_PROGRESS -> Triple("Working ⚙️", PlayfulCream, PrimaryPurple)
                    ServiceBookingStatus.MILESTONE_APPROVED -> Triple("Milestone Done 📈", WarmCardWhite, BrandGreen)
                    ServiceBookingStatus.FINISHED_RELEASED -> Triple("Released ✅", BrandGreen.copy(alpha = 0.08f), BrandGreen)
                    ServiceBookingStatus.CANCELLED_REFUNDED -> Triple("Refunded ↩️", Color.LightGray.copy(0.2f), Color.DarkGray)
                }

                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(8.dp))
                        .border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Deposit
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = BrandGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Locked Escrow Deposit: ",
                    fontSize = 11.sp,
                    color = CharcoalBlack.copy(0.6f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(java.util.Locale.US, "₦%,.2f", booking.totalAmountPaidToEscrow),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = PrimaryPurple.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Milestones Progress Tracking:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Render milestones
            booking.milestones.forEachIndexed { idx, milestone ->
                val statusText: String
                val iconVector: ImageVector
                val iconColor: Color

                when (milestone.status) {
                    MilestoneStatus.PENDING -> {
                        statusText = "Not Started"
                        iconVector = Icons.Default.HourglassEmpty
                        iconColor = Color.Gray
                    }
                    MilestoneStatus.SUBMITTED_FOR_REVIEW -> {
                        statusText = "Submitted. Action required!"
                        iconVector = Icons.Default.Announcement
                        iconColor = BrandOrange
                    }
                    MilestoneStatus.APPROVED_AND_PAID -> {
                        statusText = "Approved & Payout Released"
                        iconVector = Icons.Default.CheckCircle
                        iconColor = BrandGreen
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = PlayfulBg),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.04f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = milestone.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "₦%,.0f", milestone.costAmount),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )

                            // Actions for interactive lifecycle simulation!
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (milestone.status == MilestoneStatus.PENDING) {
                                    // Simulation button for Freelancer submitting work
                                    TextButton(
                                        onClick = {
                                            viewModel.submitMilestoneForReview(booking.id, milestone.id)
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Simulate Submission ⚙️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                    }
                                }

                                if (milestone.status == MilestoneStatus.SUBMITTED_FOR_REVIEW) {
                                    // Client action to approve and disburse funds
                                    Button(
                                        onClick = {
                                            viewModel.approveMilestone(booking.id, milestone.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Approve & Release 💸", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
