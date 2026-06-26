package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import com.example.ui.theme.PlayfulBg
import androidx.compose.foundation.Image
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToDraw: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.walletTransactions.collectAsState()

    var fundInputStr by remember { mutableStateOf("") }
    var securityAuditsEnabled by remember { mutableStateOf(false) }
    var showPurseExplanation by remember { mutableStateOf(false) }

    // Avatar pickers states
    val customerPresetId by viewModel.customerPresetId.collectAsState()
    val customerCustomUri by viewModel.customerCustomUri.collectAsState()
    val freelancePresetId by viewModel.freelancePresetId.collectAsState()
    val freelanceCustomUri by viewModel.freelanceCustomUri.collectAsState()

    var showAvatarDialogForCustomer by remember { mutableStateOf(false) }
    var showAvatarDialogForFreelance by remember { mutableStateOf(false) }

    val customerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateCustomerCustomUri(uri.toString())
        }
    }

    val freelancePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateFreelanceCustomUri(uri.toString())
        }
    }

    // Flutterwave simulation states
    var showFlutterwaveDialog by remember { mutableStateOf(false) }
    var flutterwaveAmountStr by remember { mutableStateOf("") }
    var flutterwaveEmailStr by remember { mutableStateOf(viewModel.userPhoneOrEmail.value.ifEmpty { "customer@yanga.market" }) }
    var isSimulatingPayment by remember { mutableStateOf(false) }
    var simulatedPaymentStep by remember { mutableStateOf(1) } // 1: Amount/Details, 2: Checkout Overlay, 3: Processing, 4: OTP, 5: Success Receipt
    
    var flwCardNo by remember { mutableStateOf("") }
    var flwCardExpiry by remember { mutableStateOf("") }
    var flwCardCvv by remember { mutableStateOf("") }
    var flwCardPin by remember { mutableStateOf("") }
    var flwOtpCode by remember { mutableStateOf("") }
    var flwOtpError by remember { mutableStateOf(false) }
    var flwSimulatedTxId by remember { mutableStateOf("") }

    var customPublicKey by remember { mutableStateOf("FLWPUBK_TEST-e08df9c8a77a94f1b-X") }
    var customEncryptionKey by remember { mutableStateOf("FLWENCK_TEST-5f82ac840be3") }
    var isSandboxMode by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    YangaHeader(
                        title = "Yanga Profile & Pay! 👤💳",
                        subtitle = "Manage your global Yanga handle & secure digital assets"
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { securityAuditsEnabled = !securityAuditsEnabled },
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (securityAuditsEnabled) SecondaryYellow else Color(0xFFF3F4F6), CircleShape)
                            .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), CircleShape)
                            .testTag("security_shield_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SecondaryYellow, CircleShape)
                            .border(1.5.dp, PrimaryPurple, CircleShape)
                            .testTag("go_to_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- Profile & Identity Settings ---
        item {
            val userNameState by viewModel.userName.collectAsState()
            var localNameInput by remember(userNameState) { mutableStateOf(userNameState) }

            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = PrimaryPurple,
                borderWidth = 2.0,
                modifier = Modifier.fillMaxWidth().testTag("profile_identity_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(SecondaryYellow, CircleShape)
                                .border(1.5.dp, PrimaryPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Yanga Community Profile Settings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryPurple
                            )
                            if (userNameState.isBlank()) {
                                Text(
                                    text = "⚠️ Please set your username below to broadcast vibes!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            } else {
                                Text(
                                    text = "Your active handle: @$userNameState",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Avatar Row
                    Text(
                        text = "Customize Profile Pictures 🎨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Customer Profile Image Circle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Customer Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(PlayfulBg)
                                    .border(2.5.dp, PrimaryPurple, CircleShape)
                                    .clickable { showAvatarDialogForCustomer = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (customerCustomUri.isNotEmpty()) {
                                    AsyncImage(
                                        model = customerCustomUri,
                                        contentDescription = "Customer Custom Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val imageId = if (customerPresetId == 2) R.drawable.img_avatar_cute_boy_1782427722051 else R.drawable.img_avatar_unicorn_1782427705994
                                    Image(
                                        painter = painterResource(id = imageId),
                                        contentDescription = "Customer Preset Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Change ✏️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        }

                        // Freelance Profile Image Circle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Freelancer Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(PlayfulBg)
                                    .border(2.5.dp, SecondaryYellow, CircleShape)
                                    .clickable { showAvatarDialogForFreelance = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (freelanceCustomUri.isNotEmpty()) {
                                    AsyncImage(
                                        model = freelanceCustomUri,
                                        contentDescription = "Freelance Custom Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val imageId = if (freelancePresetId == 1) R.drawable.img_avatar_unicorn_1782427705994 else R.drawable.img_avatar_cute_boy_1782427722051
                                    Image(
                                        painter = painterResource(id = imageId),
                                        contentDescription = "Freelance Preset Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Change ✏️", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryYellow)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = localNameInput,
                        onValueChange = { localNameInput = it.trim().replace(" ", "") },
                        label = { Text("Choose Username Handle (no spaces)") },
                        placeholder = { Text("e.g. tunde_dev") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    YangaFunButton(
                        text = "Save Profile Username",
                        onClick = {
                            if (localNameInput.isNotBlank()) {
                                viewModel.updateUserName(localNameInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTagStr = "save_username_btn"
                    )
                }
            }
        }

        // --- Core Balance Display ---
        item {
            YangaPlayfulCard(
                backgroundColor = PrimaryPurple,
                borderColor = SecondaryYellow,
                borderWidth = 1.5,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AVAILABLE BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryYellow,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "₦${String.format("%,.2f", balance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Button(
                            onClick = {
                                // Initialize Flutterwave Dialog states
                                flutterwaveAmountStr = ""
                                flwCardNo = ""
                                flwCardExpiry = ""
                                flwCardCvv = ""
                                flwCardPin = ""
                                flwOtpCode = ""
                                flwOtpError = false
                                flwSimulatedTxId = "FLW-MOCK-TX-${System.currentTimeMillis() % 1000000}"
                                simulatedPaymentStep = 1
                                showFlutterwaveDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryYellow,
                                contentColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wallet_deposit_btn")
                        ) {
                            Text(
                                text = "Deposit 💳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Playful Coin Purse Coin System ---
        item {
            val silverCoinsState by viewModel.silverCoins.collectAsState()
            val goldCoinsState by viewModel.goldCoins.collectAsState()
            
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)), // Pale yellow background
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, PrimaryPurple, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🎒 Yanga Coin Purse System",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryPurple
                            )
                            IconButton(
                                onClick = { showPurseExplanation = true },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("purse_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Purse Information",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .border(1.dp, Color(0xFFD97706), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "1 GP = 100 SP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Gold Coins Box
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.5.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🪙",
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$goldCoinsState GP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.testTag("wallet_gold_coins_display")
                                )
                                Text(
                                    text = "Gold Pieces",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB45309).copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Silver Coins Box
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🪙",
                                        fontSize = 20.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$silverCoinsState SP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    modifier = Modifier.testTag("wallet_silver_coins_display")
                                )
                                Text(
                                    text = "Silver Pieces",
                                    fontSize = 10.sp,
                                    color = CharcoalBlack.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Convert Button
                        Button(
                            onClick = { viewModel.convertSilverToGold() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("convert_silver_gold_btn"),
                            enabled = silverCoinsState >= 100,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Convert SP to GP 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Draw Button
                        Button(
                            onClick = onNavigateToDraw,
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryPurple),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("go_to_draw_btn"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Enter Draw 🎟️", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                        }
                    }
                }
            }
        }



        // --- Quick Google Contact Transfer Area (Removed for security flow) ---

        // --- Ledger Audit Security toggle info notice ---
        if (securityAuditsEnabled) {
            item {
                val auditReport by viewModel.walletAuditReport.collectAsState()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (auditReport.isSystemAuthentic) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp, 
                            if (auditReport.isSystemAuthentic) Color(0xFF22C55E) else Color(0xFFEF4444), 
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (auditReport.isSystemAuthentic) Icons.Default.CheckCircle else Icons.Default.Warning, 
                                    contentDescription = "Security audit", 
                                    tint = if (auditReport.isSystemAuthentic) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CRYPTOGRAPHIC CORE AUDIT", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (auditReport.isSystemAuthentic) Color(0xFF15803D) else Color(0xFF991B1B)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (auditReport.isSystemAuthentic) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (auditReport.isSystemAuthentic) "LEDGER OK ✓" else "TAMPERED ⚠️",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (auditReport.isSystemAuthentic) Color(0xFF15803D) else Color(0xFF991B1B)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "A core structural validation scan has verified all ${auditReport.totalTransactionsCount} logged transactions following modular OOP architecture rules using HMAC-SHA256 checksum tags.",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.8f),
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Divider(color = (if (auditReport.isSystemAuthentic) Color(0xFF22C55E) else Color(0xFFEF4444)).copy(alpha = 0.15f))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "LEDGER HEALTH STATUS",
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (auditReport.isSystemAuthentic) "100.0% Integrity Intact" else "INTEGRITY COMPROMISED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (auditReport.isSystemAuthentic) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ANOMALIES FOUND",
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${auditReport.anomalyCount} violations",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (auditReport.anomalyCount == 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Audit log title ---
        item {
            Text(
                text = "Transaction Audit Ledger 📜",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
        }

        // --- Empty Transactions State ---
        if (transactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Empty ledger",
                                tint = PrimaryPurple.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your digital ledger is empty. Complete transactions in foods or events to record history.",
                                fontSize = 12.sp,
                                color = CharcoalBlack.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(tx = tx, showAuditDetails = securityAuditsEnabled)
            }
        }
    }

    if (showPurseExplanation) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showPurseExplanation = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(3.dp, PrimaryPurple, RoundedCornerShape(24.dp))
                    .testTag("purse_explanation_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ℹ️ Purse Explanation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        IconButton(
                            onClick = { showPurseExplanation = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PrimaryPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "For the Yanga Coin Purse system, each time you make a transaction or buy something off the app, you get silver pieces! Here is how it works:",
                        fontSize = 12.sp,
                        color = CharcoalBlack,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "🛍️ Purchases" to "For each item you purchase in a transaction, you earn exactly 1 Silver coin. Buying 5 items earns you 5 Silver pieces!",
                        "🤝 Freelancing status" to "Citizens registered as active freelancers gain silver coins automatically from superapp activity.",
                        "⭐ Review submissions" to "Submitting or receiving a 5-star review rewards you with +5 Silver Pieces!",
                        "🔥 Vibe checks" to "If any post you publish on 'Let's Share Vibes' board hits 100+ likes, you gain +1 Silver coin!",
                        "🪙 Gold Upgrades" to "You can convert 100 Silver Pieces to 1 Gold Piece seamlessly.",
                        "🎟️ Draw entries" to "A Gold Piece can be traded directly for tickets to enter the Golden Draw where you can win incredible prizes (like Suzuki Delivery Motorcycles or ₦50,000 cash rewards)!"
                    ).forEach { (title, desc) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                            Text(desc, fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f), lineHeight = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPurseExplanation = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("close_purse_info_dialog_btn")
                    ) {
                        Text("Got It! 👍", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // --- Customer Avatar Picker Dialog ---
    if (showAvatarDialogForCustomer) {
        AlertDialog(
            onDismissRequest = { showAvatarDialogForCustomer = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text("Customer Profile Photo", fontWeight = FontWeight.Bold, color = PrimaryPurple) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select a cute 2D icon preset or upload from gallery:", fontSize = 12.sp, color = CharcoalBlack)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateCustomerPreset(1)
                                showAvatarDialogForCustomer = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🦄", fontSize = 24.sp)
                                Text("Unicorn", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateCustomerPreset(2)
                                showAvatarDialogForCustomer = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👦", fontSize = 24.sp)
                                Text("Cute Boy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Or Choose Custom Representation:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                    
                    Button(
                        onClick = {
                            customerPicker.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                    ) {
                        Text("Pick Image from Gallery 📸", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.updateCustomerCustomUri("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80")
                            showAvatarDialogForCustomer = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Premium Photo 👩✨", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Button(
                        onClick = { showAvatarDialogForCustomer = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalBlack.copy(alpha = 0.1f), contentColor = CharcoalBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // --- Freelancer Avatar Picker Dialog ---
    if (showAvatarDialogForFreelance) {
        AlertDialog(
            onDismissRequest = { showAvatarDialogForFreelance = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text("Freelance Profile Photo", fontWeight = FontWeight.Bold, color = SecondaryYellow) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select a cute 2D icon preset or upload from gallery:", fontSize = 12.sp, color = CharcoalBlack)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateFreelancePreset(1)
                                showAvatarDialogForFreelance = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).border(1.5.dp, SecondaryYellow, RoundedCornerShape(10.dp))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🦄", fontSize = 24.sp)
                                Text("Unicorn", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryYellow)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.updateFreelancePreset(2)
                                showAvatarDialogForFreelance = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayfulBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).border(1.5.dp, SecondaryYellow, RoundedCornerShape(10.dp))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👦", fontSize = 24.sp)
                                Text("Cute Boy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryYellow)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Or Choose Custom Representation:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                    
                    Button(
                        onClick = {
                            freelancePicker.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.5.dp, SecondaryYellow, RoundedCornerShape(10.dp))
                    ) {
                        Text("Pick Image from Gallery 📸", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.updateFreelanceCustomUri("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80")
                            showAvatarDialogForFreelance = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Premium Photo 👨✨", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Button(
                        onClick = { showAvatarDialogForFreelance = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalBlack.copy(alpha = 0.1f), contentColor = CharcoalBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // --- Flutterwave Payment Dialog ---
    if (showFlutterwaveDialog) {
        AlertDialog(
            onDismissRequest = { if (simulatedPaymentStep != 3) showFlutterwaveDialog = false },
            confirmButton = {},
            dismissButton = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = simulatedPaymentStep != 3,
                dismissOnClickOutside = simulatedPaymentStep != 3
            ),
            text = {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (simulatedPaymentStep == 1) {
                            // Step 1: Amount & Billing info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Flutterwave Deposit 💳", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryPurple)
                                IconButton(onClick = { showFlutterwaveDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }

                            Text("Enter deposit details to simulate secure online payments via Flutterwave Gateway:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.7f))

                            OutlinedTextField(
                                value = flutterwaveAmountStr,
                                onValueChange = { flutterwaveAmountStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Funding Amount (₦)") },
                                placeholder = { Text("e.g. 5000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = flutterwaveEmailStr,
                                onValueChange = { flutterwaveEmailStr = it },
                                label = { Text("Billing Email Address") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(2000, 5000, 10000).forEach { pAmt ->
                                    Button(
                                        onClick = { flutterwaveAmountStr = pAmt.toString() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PlayfulBg, contentColor = PrimaryPurple),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    ) {
                                        Text("₦$pAmt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Advanced Config Collapsible
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PlayfulBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("⚙️ FLUTTERWAVE CONFIGURATION (MOCK)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Public Key: $customPublicKey", fontSize = 8.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                                    Text("Sandbox Mode: Enabled (Active test payload)", fontSize = 8.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    val amt = flutterwaveAmountStr.toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        simulatedPaymentStep = 2
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Initiate Secured Pay ⚡", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        else if (simulatedPaymentStep == 2) {
                            // Step 2: Flutterwave Gateway Checkout UI Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9FBFD))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            ) {
                                Column {
                                    // Flutterwave Branding Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF39C12)) // Flutterwave Orange Accent
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("flutterwave", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                                        Text("SECURE CHECKOUT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp)
                                    }

                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Yanga Market Superapp Limited", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CharcoalBlack)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Email: $flutterwaveEmailStr", fontSize = 9.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                                            Text("Amount: ₦$flutterwaveAmountStr", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                        }

                                        Divider(color = Color(0xFFE2E8F0))

                                        Text("Pay with Card", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryPurple)

                                        OutlinedTextField(
                                            value = flwCardNo,
                                            onValueChange = { flwCardNo = it.filter { c -> c.isDigit() }.take(16) },
                                            label = { Text("CARD NUMBER", fontSize = 9.sp) },
                                            placeholder = { Text("5061 2345 6789 0123") },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = flwCardExpiry,
                                                onValueChange = { flwCardExpiry = it.take(5) },
                                                label = { Text("EXPIRY (MM/YY)", fontSize = 9.sp) },
                                                placeholder = { Text("12/28") },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                            OutlinedTextField(
                                                value = flwCardCvv,
                                                onValueChange = { flwCardCvv = it.filter { c -> c.isDigit() }.take(3) },
                                                label = { Text("CVV", fontSize = 9.sp) },
                                                placeholder = { Text("321") },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        OutlinedTextField(
                                            value = flwCardPin,
                                            onValueChange = { flwCardPin = it.filter { c -> c.isDigit() }.take(4) },
                                            label = { Text("CARD PIN (4-Digits)", fontSize = 9.sp) },
                                            placeholder = { Text("e.g. 9081") },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Button(
                                            onClick = {
                                                if (flwCardNo.length == 16 && flwCardCvv.length == 3) {
                                                    simulatedPaymentStep = 3
                                                } else {
                                                    // Allow custom simulation bypass
                                                    flwCardNo = "5061123456789012"
                                                    flwCardCvv = "321"
                                                    simulatedPaymentStep = 3
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF39C12)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                        ) {
                                            Text("SIMULATE SECURE PAY 🔒", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🔒 SECURED BY FLUTTERWAVE GATEWAY v3.0", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        else if (simulatedPaymentStep == 3) {
                            // Step 3: Processing loading
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFFF39C12))
                                Text("Contacting bank gateway...", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalBlack)
                                Text("Simulating secured Flutterwave encryption protocols...", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                
                                // Automatically advance after brief simulator delay
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(2000)
                                    simulatedPaymentStep = 4
                                }
                            }
                        }

                        else if (simulatedPaymentStep == 4) {
                            // Step 4: OTP challenge
                            Text("3D Secure Verification 🔐", fontWeight = FontWeight.Black, fontSize = 15.sp, color = PrimaryPurple)
                            Text("Enter the test authorization code to complete your simulated Flutterwave payment:", fontSize = 11.sp, color = CharcoalBlack)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = PlayfulBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SIMULATOR TEST CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                    Text("12345", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                    Text("Use this test code to approve sandbox debit.", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            OutlinedTextField(
                                value = flwOtpCode,
                                onValueChange = { 
                                    flwOtpCode = it.filter { c -> c.isDigit() }
                                    flwOtpError = false
                                },
                                label = { Text("Enter OTP Code") },
                                placeholder = { Text("e.g. 12345") },
                                shape = RoundedCornerShape(10.dp),
                                isError = flwOtpError,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (flwOtpError) {
                                Text("Incorrect security code. Please input '12345' to simulate success.", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (flwOtpCode == "12345") {
                                        simulatedPaymentStep = 5
                                    } else {
                                        flwOtpError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text("Approve Payment", fontWeight = FontWeight.Bold)
                            }
                        }

                        else if (simulatedPaymentStep == 5) {
                            // Step 5: Success Receipt screen
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(54.dp).background(Color(0xFFDCFCE7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
                                }

                                Text("Payment Successful! 🎉", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF16A34A))
                                Text("Simulated Flutterwave secure payload transfer complete.", fontSize = 10.sp, color = Color.Gray)

                                Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 8.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth().background(PlayfulBg).padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("REFERENCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text(flwSimulatedTxId, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("MERCHANT ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text("FLW-YNG-MOCK-9941", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("CREDITED VALUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text("₦$flutterwaveAmountStr", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PrimaryPurple)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val amt = flutterwaveAmountStr.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            viewModel.fundWallet(amt)
                                        }
                                        showFlutterwaveDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Text("Back to Ledger", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TransactionRow(
    tx: com.example.data.database.WalletTransactionEntity,
    showAuditDetails: Boolean
) {
    val df = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val formattedDate = df.format(Date(tx.timestamp))
    val isDeposit = tx.type == "FUND"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDeposit) Color(0xFFDCFCE7) else Color(0xFFF3E8FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDeposit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = tx.type,
                            tint = if (isDeposit) Color(0xFF16A34A) else PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tx.description,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalBlack,
                            maxLines = 1
                        )
                        val txGold = tx.amount.toInt() / 100
                        val txSilver = tx.amount.toInt() % 100
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🪙 $txGold GP, $txSilver SP",
                                fontSize = 11.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = CharcoalBlack.copy(alpha = 0.3f)
                            )
                            Text(
                                text = formattedDate,
                                fontSize = 10.sp,
                                color = CharcoalBlack.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Text(
                    text = "${if (isDeposit) "+" else "-"}₦${String.format("%,.2f", tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDeposit) Color(0xFF16A34A) else CharcoalBlack
                )
            }

            if (showAuditDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = PrimaryPurple.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHA256 SECURE ADDR: ${tx.id}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "INTEGRITY HASH: ${tx.securityHash}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }
        }
    }
}



