package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data model representing a high-fidelity onboarding slide
data class OnboardingSlide(
    val title: String,
    val description: String,
    val emoji: String,
    val color: Color,
    val badge: String
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Onboarding flow steps:
    // 0 - 4: Dynamic Info Slides (5 total) Explaining Yanga Superapp sections
    // 5: Account Authorization Gateway (Google, Email, Phone)
    // 6: Profile Customization & Dynamic Google Map Pinpointing Page
    var currentStep by remember { mutableStateOf(if (viewModel.hasOnboarded()) 5 else 0) }
    
    // Input parameters
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var chosenMethod by remember { mutableStateOf<String?>(null) } // "Google", "Email", "Phone"
    var isAuthenticating by remember { mutableStateOf(false) }
    var showGooglePageChooser by remember { mutableStateOf(false) }
    
    // Profile Fields & Map Data
    var fullNameInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(6.4281) } // Default Lagos
    var longitude by remember { mutableStateOf(3.4219) } // Default Lagos
    var isMapSatellite by remember { mutableStateOf(false) }
    var mapZoomLevel by remember { mutableStateOf(2) } // Scale level
    var isLocationVerified by remember { mutableStateOf(true) } // Custom toggle whether pinpoint is correct

    // Onboarding Dynamic Slides List
    val slides = listOf(
        OnboardingSlide(
            title = "Yanga Superapp Ecosystem 🌍",
            description = "Welcome to Yanga Market! The ultimate African peer-to-peer Superapp hub. Enjoy lightning-fast food delivery, retail convenience, live entertainment, healthcare, and a secure in-app ledger wallet.",
            emoji = "🍇🛍️🍔",
            color = PrimaryPurple,
            badge = "SUPERAPP"
        ),
        OnboardingSlide(
            title = "Food & Organic Fruits Marketplace 🍲🍊",
            description = "Order appetizing hot delicacies and fresh farm-direct fruits. Inspired by top-tier food logistics like Chowdeck, with dynamic delivery routing built to serve you with absolute speed.",
            emoji = "🍛🍍🚀",
            color = BrandOrange,
            badge = "YUMMY FOOD"
        ),
        OnboardingSlide(
            title = "Freelance Talents & Safe Escrow 💼🤝",
            description = "Unlock the vibrant local gig economy! Browse, message, and hire verified local professionals. Funds remain cryptographically locked in secure escrow until milestones are fully completed.",
            emoji = "💻🛠️✨",
            color = PrimaryPurple,
            badge = "FREELANCE"
        ),
        OnboardingSlide(
            title = "Hospital Discovery & Live Shows 🏥🎟️",
            description = "Explore close verified hospital clinics, schedule telehealth virtual sessions, and book VIP tickets to the most trending local musical shows and social events across the city.",
            emoji = "🩺🏨🎷",
            color = BrandGreen,
            badge = "CARE & EVENTS"
        ),
        OnboardingSlide(
            title = "Let's Share Vibes Community 💬🔥",
            description = "Connect effortlessly with buddies! Discuss hotspots, rate restaurants, upload status, and safely share peer-to-peer ledger coin transfers through the modern in-app wallet gateway.",
            emoji = "📢🎉💖",
            color = BoldGold,
            badge = "LET'S SHARE VIBES"
        )
    )

    // Interactive slide slideshow dynamic slide tick handler
    LaunchedEffect(currentStep) {
        if (currentStep in 0..4) {
            // Auto slide every 7 seconds, except if the user interacted
            delay(7000)
            if (currentStep in 0..3) {
                currentStep++
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PlayfulBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                showGooglePageChooser -> {
                    GoogleAccountChooserScreen(
                        onAccountSelected = { selectedEmail, selectedName ->
                            coroutineScope.launch {
                                chosenMethod = "Google"
                                isAuthenticating = true
                                showGooglePageChooser = false
                                delay(1500)
                                viewModel.setLoginDetails(selectedEmail, "Google")
                                viewModel.authorizeWithGoogle(selectedEmail, selectedName)
                                isAuthenticating = false
                                currentStep = 6 // Go to Map Profiler
                            }
                        },
                        onBack = {
                            showGooglePageChooser = false
                        }
                    )
                }

                // STEP 0 to 4: Dynamic Onboarding Slideshow
                currentStep in 0..4 -> {
                    val slide = slides[currentStep]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header with Skip option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(PrimaryPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Y", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Yanga Onboarding",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CharcoalBlack
                                )
                            }
                            
                            Text(
                                text = "Skip Slides",
                                fontSize = 12.sp,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setOnboarded(true)
                                        currentStep = 5
                                    }
                                    .testTag("skip_slides_button")
                            )
                        }

                        // Sliding Animated Presentation
                        AnimatedContent(
                            targetState = slide,
                            transitionSpec = {
                                slideInHorizontally { width -> width } + fadeIn() with
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            },
                            modifier = Modifier.weight(1f)
                        ) { currentSlide ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Dynamic Floating badge
                                Box(
                                    modifier = Modifier
                                        .background(currentSlide.color.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                        .border(1.5.dp, currentSlide.color, RoundedCornerShape(24.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = currentSlide.badge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = currentSlide.color
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Large Animated Emoji Display representing mobile slide features
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .background(currentSlide.color.copy(alpha = 0.08f), CircleShape)
                                        .border(2.dp, currentSlide.color.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Bouncing/Rotating Animation effect
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val rotation by infiniteTransition.animateFloat(
                                        initialValue = -5f,
                                        targetValue = 5f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1200, easing = EaseInOutSine),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    val scaleAnim by infiniteTransition.animateFloat(
                                        initialValue = 0.95f,
                                        targetValue = 1.05f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = EaseInOutSine),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )

                                    Text(
                                        text = currentSlide.emoji,
                                        fontSize = 72.sp,
                                        modifier = Modifier
                                            .rotate(rotation)
                                            .scale(scaleAnim)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // Title of slide
                                Text(
                                    text = currentSlide.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Beautiful description
                                Text(
                                    text = currentSlide.description,
                                    fontSize = 13.sp,
                                    color = CharcoalBlack.copy(alpha = 0.70f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        // Bottom Actions: Indicator dots and Navigations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Indicator Dots
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                slides.forEachIndexed { idx, _ ->
                                    val isSelected = idx == currentStep
                                    val dotWidth by animateDpAsState(if (isSelected) 24.dp else 8.dp)
                                    val dotColor = if (isSelected) PrimaryPurple else CharcoalBlack.copy(alpha = 0.15f)
                                    
                                    Box(
                                        modifier = Modifier
                                            .height(8.dp)
                                            .width(dotWidth)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(dotColor)
                                    )
                                }
                            }

                            // Navigation Button
                            Button(
                                onClick = {
                                    if (currentStep < 4) {
                                        currentStep++
                                    } else {
                                        viewModel.setOnboarded(true)
                                        currentStep = 5 // Go to login selection
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("onboarding_next_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (currentStep == 4) "Get Started 🚀" else "Next Step",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Forward Icon",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // STEP 5: Auth gateways - Log In with Google, Email, or Telephone
                currentStep == 5 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Title Header
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Welcome to Yanga Market 🌍",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose a secure method to authorize your account and load the digital ledger passport.",
                                fontSize = 12.sp,
                                color = CharcoalBlack.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        // Visual Brand Icon
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(SecondaryYellow, CircleShape)
                                .border(3.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Security login icon",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        if (isAuthenticating) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                                border = BorderStroke(2.dp, PrimaryPurple.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(color = PrimaryPurple)
                                    Text(
                                        text = when (chosenMethod) {
                                            "Google" -> "Performing Google Identity Sign-In Handshake..."
                                            "Email" -> "Verifying Credentials on Secure Ledger..."
                                            "Phone" -> "Awaiting SMS Verification Code Hook..."
                                            else -> "Authorizing credentials..."
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Please keep applet open – generating local authentication state variables.",
                                        fontSize = 11.sp,
                                        color = CharcoalBlack.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // CHOICES LIST
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. CONTINUE WITH GOOGLE
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp))
                                        .clickable {
                                            showGooglePageChooser = true
                                        }
                                        .testTag("google_auth_cta"),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Google colored style bullet
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(Color(0xFFEA4335), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Continue with Google",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CharcoalBlack
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Divider(modifier = Modifier.weight(1f), color = PrimaryPurple.copy(alpha = 0.2f))
                                    Text(
                                        text = " or alternate options ",
                                        fontSize = 11.sp,
                                        color = CharcoalBlack.copy(alpha = 0.40f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    Divider(modifier = Modifier.weight(1f), color = PrimaryPurple.copy(alpha = 0.2f))
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                // Select form type switcher
                                var activeFormType by remember { mutableStateOf<String?>(null) } // "Email" or "Phone"

                                if (activeFormType == null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Email icon button
                                        Button(
                                            onClick = { activeFormType = "Email" },
                                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulCream),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Email, "Email Onboarding", tint = PrimaryPurple)
                                                Text("Use Email", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }

                                        // Phone icon button
                                        Button(
                                            onClick = { activeFormType = "Phone" },
                                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulCream),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, "Phone Onboarding", tint = PrimaryPurple)
                                                Text("Use Phone", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else if (activeFormType == "Email") {
                                    // EMAIL SIGN IN EXPANSION
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                                        border = BorderStroke(2.dp, PrimaryPurple),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Email Authorization Gateway", fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 13.sp)
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close Form",
                                                    tint = HeartRed,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable { activeFormType = null }
                                                )
                                            }

                                            OutlinedTextField(
                                                value = emailInput,
                                                onValueChange = { emailInput = it },
                                                label = { Text("Enter Email Address") },
                                                placeholder = { Text("e.g. eniola@yanga.live") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                                modifier = Modifier.fillMaxWidth().testTag("email_input_field"),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = passwordInput,
                                                onValueChange = { passwordInput = it },
                                                label = { Text("Enter Account Password") },
                                                visualTransformation = PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            Button(
                                                onClick = {
                                                    if (emailInput.contains("@") && passwordInput.length >= 4) {
                                                        coroutineScope.launch {
                                                            chosenMethod = "Email"
                                                            isAuthenticating = true
                                                            delay(1800)
                                                            viewModel.setLoginDetails(emailInput, "Email")
                                                            isAuthenticating = false
                                                            currentStep = 6
                                                        }
                                                    } else {
                                                        // Fallback alert
                                                    }
                                                },
                                                enabled = emailInput.isNotBlank() && passwordInput.isNotBlank(),
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("email_submit_button")
                                            ) {
                                                Text("Login & Initialize profile 🔑", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else if (activeFormType == "Phone") {
                                    // PHONE SIGN IN EXPANSION
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                                        border = BorderStroke(2.dp, PrimaryPurple),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("SMS Phone Verification", fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 13.sp)
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close Form",
                                                    tint = HeartRed,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clickable { activeFormType = null }
                                                )
                                            }

                                            OutlinedTextField(
                                                value = phoneInput,
                                                onValueChange = { phoneInput = it },
                                                label = { Text("Enter Mobile Phone Number") },
                                                placeholder = { Text("e.g. +234 803 123 4567") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                modifier = Modifier.fillMaxWidth().testTag("phone_input_field"),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            Button(
                                                onClick = {
                                                    if (phoneInput.length >= 7) {
                                                        coroutineScope.launch {
                                                            chosenMethod = "Phone"
                                                            isAuthenticating = true
                                                            delay(1800)
                                                            viewModel.setLoginDetails(phoneInput, "Phone")
                                                            isAuthenticating = false
                                                            currentStep = 6
                                                        }
                                                    }
                                                },
                                                enabled = phoneInput.length >= 7,
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("phone_submit_button")
                                            ) {
                                                Text("Request OTP Secure Code 📲", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Security Disclaimer bullet
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, "Secure Layer", tint = BrandGreen, modifier = Modifier.size(16.dp))
                            Text(
                                "Cryptographic ledger connection: SHA-256 local key validation.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBlack.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // STEP 6: Complete Profile Setup & Google Maps PINPOINT GPS Verification View
                currentStep == 6 -> {
                    val globalEmailOrPhone by viewModel.userPhoneOrEmail.collectAsState()
                    val globalMethod by viewModel.loginMethod.collectAsState()
                    
                    // Initialize default name if empty and using google
                    LaunchedEffect(globalMethod) {
                        if (fullNameInput.isEmpty()) {
                            fullNameInput = if (globalMethod == "Google") "Eniola Agbeyindo" else ""
                        }
                        if (locationInput.isEmpty()) {
                            locationInput = "Lekki Phase 1, Lagos, Nigeria"
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Setup Your Profile 📍",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryPurple,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Required to pinpoint delivery coordinates on Google Maps before entering Yanga Market.",
                                fontSize = 12.sp,
                                color = CharcoalBlack.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Identity Inputs Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, PrimaryPurple.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(containerColor = WarmCardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "AUTHORIZED GATEWAY DETAILS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryPurple
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (globalMethod == "Google") Icons.Default.Verified else Icons.Default.Lock,
                                        contentDescription = "Method Icon",
                                        tint = BrandOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Logged in via $globalMethod ($globalEmailOrPhone)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack.copy(alpha = 0.8f)
                                    )
                                }

                                Divider(color = PrimaryPurple.copy(alpha = 0.08f))

                                // Name Input (Required)
                                Text(
                                    text = "Your Full Name *",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBlack
                                )
                                OutlinedTextField(
                                    value = fullNameInput,
                                    onValueChange = { fullNameInput = it },
                                    placeholder = { Text("Enter your awesome name") },
                                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    leadingIcon = { Icon(Icons.Default.Person, "Name Input", tint = PrimaryPurple) }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Location String Search Box
                                Text(
                                    text = "Type Delivery Address / Location *",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBlack
                                )
                                OutlinedTextField(
                                    value = locationInput,
                                    onValueChange = { input -> 
                                        locationInput = input
                                        // Dynamic Location Autocomplete to adjust coordinates & simulate Google Maps lookup!
                                        val lowercase = input.lowercase()
                                        when {
                                            lowercase.contains("lekki") -> {
                                                latitude = 6.4281
                                                longitude = 3.4219
                                            }
                                            lowercase.contains("ikeja") -> {
                                                latitude = 6.5244
                                                longitude = 3.3792
                                            }
                                            lowercase.contains("yaba") -> {
                                                latitude = 6.5095
                                                longitude = 3.3711
                                            }
                                            lowercase.contains("abuja") -> {
                                                latitude = 9.0765
                                                longitude = 7.3986
                                            }
                                            lowercase.isNotBlank() -> {
                                                // Seed slightly shifted custom coordinate based on string hash
                                                val hash = input.hashCode().toDouble() / 1_000_000_000.0
                                                latitude = 6.45 + (hash * 0.1)
                                                longitude = 3.40 + (hash * 0.1)
                                            }
                                        }
                                    },
                                    placeholder = { Text("e.g. Lekki Phase 1, Lagos, Nigeria") },
                                    modifier = Modifier.fillMaxWidth().testTag("profile_location_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    leadingIcon = { Icon(Icons.Default.MyLocation, "Location Input", tint = BrandOrange) }
                                )
                            }
                        }

                        // GOOGLE MAP CONTEXT
                        Text(
                            text = "INTEGRATED GOOGLE MAP VIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // HIGH FIDELITY SIMULATED INTERACTIVE GOOGLE MAP COMPOSABLE!
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .border(2.dp, PrimaryPurple, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Draw high fidelity map using Canvas
                                YangaInteractiveGoogleMap(
                                    lat = latitude,
                                    lng = longitude,
                                    isSatelliteStyle = isMapSatellite,
                                    zoomLevel = mapZoomLevel,
                                    onMapClicked = { clickOffset, canvasSize ->
                                        // Clicking on Map pinpoints coordinate!
                                        // Convert relative clicking coordinates to simulated Lat/Lng shifts
                                        val percentX = clickOffset.x / canvasSize.width
                                        val percentY = clickOffset.y / canvasSize.height
                                        
                                        // Map center represents 6.45, 3.40. Add offsets
                                        latitude = 6.45 + (0.5 - percentY) * 0.15
                                        longitude = 3.40 + (percentX - 0.5) * 0.15
                                        
                                        // Give descriptive custom title representing pinning accuracy
                                        locationInput = "Pinpointed Coordinates: [${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}]"
                                    }
                                )

                                // Autocomplete suggested Preset Tags Overlay (Floating over Google Map)
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Lekki", "Ikeja", "Yaba", "Abuja").forEach { p ->
                                        Box(
                                            modifier = Modifier
                                                .background(PrimaryPurple, RoundedCornerShape(16.dp))
                                                .border(1.dp, SecondaryYellow, RoundedCornerShape(16.dp))
                                                .clickable {
                                                    when (p) {
                                                        "Lekki" -> {
                                                            locationInput = "Lekki Phase 1, Lagos, Nigeria"
                                                            latitude = 6.4281
                                                            longitude = 3.4219
                                                        }
                                                        "Ikeja" -> {
                                                            locationInput = "Ikeja, Lagos, Nigeria"
                                                            latitude = 6.5244
                                                            longitude = 3.3792
                                                        }
                                                        "Yaba" -> {
                                                            locationInput = "Yaba Hub, Lagos, Nigeria"
                                                            latitude = 6.5095
                                                            longitude = 3.3711
                                                        }
                                                        "Abuja" -> {
                                                            locationInput = "Wuse II, Abuja, Nigeria"
                                                            latitude = 9.0765
                                                            longitude = 7.3986
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(p, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Interactive Floating Controls Side Overlay
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Zoom In Button
                                    FloatingActionButton(
                                        onClick = { if (mapZoomLevel < 4) mapZoomLevel++ },
                                        containerColor = WarmCardWhite,
                                        contentColor = PrimaryPurple,
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Zoom In", modifier = Modifier.size(16.dp))
                                    }

                                    // Zoom Out Button
                                    FloatingActionButton(
                                        onClick = { if (mapZoomLevel > 1) mapZoomLevel-- },
                                        containerColor = WarmCardWhite,
                                        contentColor = PrimaryPurple,
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, "Zoom Out", modifier = Modifier.size(16.dp))
                                    }

                                    // STYLE Switcher (Standard / Satellite)
                                    FloatingActionButton(
                                        onClick = { isMapSatellite = !isMapSatellite },
                                        containerColor = if (isMapSatellite) PrimaryPurple else SecondaryYellow,
                                        contentColor = CharcoalBlack,
                                        modifier = Modifier.size(32.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMapSatellite) Icons.Default.Layers else Icons.Default.Map,
                                            contentDescription = "Map Style",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Interactive GPS Center Marker Target Indicator overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(CharcoalBlack.copy(alpha = 0.82f), RoundedCornerShape(topEnd = 12.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "🛰️ GOOGLE MAPS INTEGRATION",
                                            fontSize = 7.sp,
                                            color = SecondaryYellow,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "Coords: ${String.format("%.4f", latitude)}°N, ${String.format("%.4f", longitude)}°E",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        // Map verification checklist
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PlayfulCream),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isLocationVerified,
                                        onCheckedChange = { isLocationVerified = it },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                                    )
                                    Column {
                                        Text(
                                            text = "Confirm coordinate accuracy",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalBlack
                                        )
                                        Text(
                                            text = "Checks whether GPS pinpoint matches your target deliveries.",
                                            fontSize = 9.sp,
                                            color = CharcoalBlack.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Submit profile button
                        Button(
                            onClick = {
                                if (fullNameInput.isNotBlank() && locationInput.isNotBlank()) {
                                    viewModel.completeProfileAndLogIn(
                                        name = fullNameInput,
                                        location = locationInput,
                                        lat = latitude,
                                        lng = longitude
                                    )
                                }
                            },
                            enabled = fullNameInput.isNotBlank() && locationInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .border(2.dp, SecondaryYellow, RoundedCornerShape(12.dp))
                                .testTag("complete_profile_submit_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, "Submit Profile", tint = Color.White)
                                Text(
                                    text = "Final Profile Lock & Enter App 🔓",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Beautiful simulated modern high-contrast customizable GPS vector map
@Composable
fun YangaInteractiveGoogleMap(
    lat: Double,
    lng: Double,
    isSatelliteStyle: Boolean,
    zoomLevel: Int, // 1 to 4 scaling
    onMapClicked: (Offset, Size) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(lat, lng) {
                detectTapGestures { offset ->
                    onMapClicked(offset, this.size.let { Size(it.width.toFloat(), it.height.toFloat()) })
                }
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val size = Size(width, height)

        val routeBgColor = if (isSatelliteStyle) Color(0xFF1E1B4B) else Color(0xFFFEF08A).copy(alpha = 0.5f) // Yellowish cream standard
        val parkColor = if (isSatelliteStyle) Color(0xFF0F766E) else Color(0xFFBBF7D0) // Green fields standard
        val oceanColor = if (isSatelliteStyle) Color(0xFF172554) else Color(0xFF93C5FD) // Blue Standard water
        val streetColor = if (isSatelliteStyle) Color(0xFF312E81) else Color(0xFFFFFFFF) // White streets standard
        val gridLabelColor = if (isSatelliteStyle) BrightNeonPurple.copy(alpha = 0.40f) else PrimaryPurple.copy(alpha = 0.25f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Draw base landmass representation
            drawRect(
                color = routeBgColor,
                size = size
            )

            // Scaled zoom offset helpers
            val scale = when (zoomLevel) {
                1 -> 0.7f
                2 -> 1.0f
                3 -> 1.5f
                4 -> 2.2f
                else -> 1.0f
            }

            // 2. Draw Simulated Blue Water Canal/Lagoon representing Lagos Lagoon coastline
            val waterPath = Path().apply {
                moveTo(0f, height * 0.70f)
                quadraticTo(
                    width * 0.4f * scale, height * 0.55f * scale,
                    width * 0.8f * scale, height * 0.95f * scale
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = waterPath,
                color = oceanColor
            )

            // 3. Draw Simulated Parks and lush dynamic leafy green areas (Green rectangles and circles)
            drawCircle(
                color = parkColor,
                radius = 70f * scale,
                center = Offset(width * 0.2f, height * 0.3f)
            )
            drawRect(
                color = parkColor,
                topLeft = Offset(width * 0.7f, height * 0.1f),
                size = Size(100f * scale, 80f * scale)
            )

            // 4. Draw Simulated Grid streets & Highways (Highways are thicker, streets are thinner)
            // Draw Main Highway 1 (Diagonal route)
            drawLine(
                color = streetColor,
                start = Offset(0f, 0f),
                end = Offset(width, height),
                strokeWidth = 24f * scale
            )
            // Highway markings dash (Yellow dots)
            drawLine(
                color = BrandOrange.copy(alpha = 0.75f),
                start = Offset(0f, 0f),
                end = Offset(width, height),
                strokeWidth = 3f * scale,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )

            // Draw cross-grid lines (vertical)
            for (i in 1..4) {
                val x = (width / 5) * i
                drawLine(
                    color = streetColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 10f * scale
                )
            }
            // Draw cross-grid lines (horizontal)
            for (i in 1..3) {
                val y = (height / 4) * i
                drawLine(
                    color = streetColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 10f * scale
                )
            }

            // 5. Draw visual Map navigation grid identifiers
            drawOval(
                color = gridLabelColor,
                topLeft = Offset(width * 0.35f, height * 0.30f),
                size = Size(140f * scale, 140f * scale),
                style = Stroke(width = 2f)
            )

            // 6. Draw central dynamic pulsating target pinpoint representing current coordinates!
            // Calculate pixel offsets shifted slightly relative to lat/lng mapping
            val latCenter = 6.45 // center Lagos
            val lngCenter = 3.40
            
            // map coordinates onto Canvas screen pixels
            val pinX = (width / 2f) + ((lng - lngCenter) * 1200f * scale).toFloat()
            val pinY = (height / 2f) - ((lat - latCenter) * 1200f * scale).toFloat()

            // Safe bounded pinpoint fallback inside map viewport
            val boundedX = pinX.coerceIn(40f, width - 40f)
            val boundedY = pinY.coerceIn(40f, height - 40f)

            // Target search crosshairs (translucent purple/teal)
            drawCircle(
                color = if (isSatelliteStyle) BrightNeonPurple.copy(alpha = 0.18f) else PrimaryPurple.copy(alpha = 0.15f),
                radius = 50f,
                center = Offset(boundedX, boundedY)
            )
            drawCircle(
                color = if (isSatelliteStyle) BrightNeonPurple.copy(alpha = 0.35f) else PrimaryPurple.copy(alpha = 0.25f),
                radius = 28f,
                center = Offset(boundedX, boundedY)
            )

            // Dynamic pin tip shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(boundedX - 10f, boundedY + 12f),
                size = Size(20f, 8f)
            )

            // Draw custom Red Pin representation using paths
            val pinPath = Path().apply {
                moveTo(boundedX, boundedY) // Point tip on map
                cubicTo(
                    boundedX - 16f, boundedY - 24f,
                    boundedX - 16f, boundedY - 40f,
                    boundedX, boundedY - 40f
                )
                cubicTo(
                    boundedX + 16f, boundedY - 40f,
                    boundedX + 16f, boundedY - 24f,
                    boundedX, boundedY
                )
                close()
            }
            drawPath(
                path = pinPath,
                color = HeartRed
            )

            // Draw central pin shiny white dot
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(boundedX, boundedY - 28f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAccountChooserScreen(
    onAccountSelected: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var customEmail by remember { mutableStateOf("") }
    var isAddingAccount by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top alignment navigation back or Google close look
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CharcoalBlack)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google Logo: G o o g l e in colors
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAddingAccount) {
            Text(
                text = "Choose an account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "to continue to Yanga Market",
                fontSize = 14.sp,
                color = Color(0xFF5F6368),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Eniola Agbeyindo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAccountSelected("eniolaagbeyindo@gmail.com", "Eniola Agbeyindo") }
                    .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE8F0FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "E",
                        color = Color(0xFF1A73E8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Eniola Agbeyindo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3C4043)
                    )
                    Text(
                        text = "eniolaagbeyindo@gmail.com",
                        fontSize = 12.sp,
                        color = Color(0xFF5F6368)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Use another account
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAddingAccount = true }
                    .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF1F3F4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add account",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Use another account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A73E8)
                )
            }
        } else {
            // Typing custom account
            Text(
                text = "Sign in with Google",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type your credentials to continue to Yanga Market",
                fontSize = 14.sp,
                color = Color(0xFF5F6368),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = customEmail,
                onValueChange = { customEmail = it },
                label = { Text("Email or phone") },
                placeholder = { Text("e.g. user@gmail.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("google_custom_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A73E8),
                    unfocusedBorderColor = Color(0xFFDADCE0)
                ),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isAddingAccount = false }) {
                    Text("Choose existing", color = Color(0xFF1A73E8), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (customEmail.isNotBlank() && customEmail.contains("@")) {
                            val computedName = customEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                            onAccountSelected(customEmail, computedName)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = customEmail.isNotBlank() && customEmail.contains("@")
                ) {
                    Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "To continue, Google will share your name, email address, language preference, and profile picture with Yanga Market.",
            fontSize = 11.sp,
            color = Color(0xFF5F6368),
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}
