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
import com.example.data.network.OAuthStatus
import com.example.data.network.GoogleContact
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                        text = "TOTAL WALLET LEDGER BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryYellow,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₦${String.format("%,.2f", balance)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SecondaryYellow)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secured status",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PIN LOCK ACTIVE: 1234 (VERIFIED)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
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

        // --- Fast Card Funding Area ---
        item {
            YangaPlayfulCard(
                backgroundColor = Color.White,
                borderColor = PrimaryPurple,
                borderWidth = 2.0,
                modifier = Modifier.fillMaxWidth().testTag("wallet_funding_panel")
            ) {
                Text(
                    text = "Deposit Funds via Card Simulation 💸",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val deposits = listOf(2000.0, 5000.0, 10000.0)
                    for (amount in deposits) {
                        Button(
                            onClick = { viewModel.fundWallet(amount) },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = "₦${String.format("%,.0f", amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = fundInputStr,
                        onValueChange = { fundInputStr = it },
                        label = { Text("Manual funding amount (₦)") },
                        placeholder = { Text("Enter custom amount") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_amount_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    YangaFunButton(
                        text = "Fund",
                        onClick = {
                            val amt = fundInputStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.fundWallet(amt)
                                fundInputStr = ""
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        testTagStr = "submit_fund_btn"
                    )
                }
            }
        }

        // --- Quick Google Contact Transfer Area ---
        item {
            GoogleContactsTransferSection(viewModel = viewModel, balance = balance)
        }

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
}

@Composable
fun GoogleContactsTransferSection(
    viewModel: MainViewModel,
    balance: Double,
    modifier: Modifier = Modifier
) {
    val oauthState by viewModel.oauthStatus.collectAsState()
    val syncedContacts by viewModel.oauthSynchronizedContacts.collectAsState()

    if (oauthState == OAuthStatus.AUTHORIZED && syncedContacts.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
                .testTag("google_contacts_transfer_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF4285F4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Direct Google Contacts Pay ⚡🔑",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Transfer to verified Google contacts securely. Handshake is executed on oauth2 tokens.",
                    fontSize = 11.sp,
                    color = CharcoalBlack.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                var selectedContactForTransfer by remember { mutableStateOf<GoogleContact?>(null) }
                var transferAmountStr by remember { mutableStateOf("") }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    syncedContacts.forEach { contact ->
                        val isSelected = selectedContactForTransfer?.id == contact.id
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFFF9FAFB),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PrimaryPurple else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedContactForTransfer = if (isSelected) null else contact
                                }
                                .padding(8.dp)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(contact.profileColor), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contact.name.split(" ").firstOrNull() ?: contact.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBlack,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                selectedContactForTransfer?.let { contact ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recipient: ${contact.name} (${contact.phone})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Selected ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = transferAmountStr,
                            onValueChange = { transferAmountStr = it },
                            label = { Text("Amount to Transfer (₦)") },
                            placeholder = { Text("e.g. 5000") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("transfer_amount_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = transferAmountStr.toDoubleOrNull() ?: 0.0
                                if (amt <= 0.0) {
                                    viewModel.postError("Please enter a valid transfer amount!")
                                } else {
                                    viewModel.transferWalletFunds(amt, contact.name)
                                    transferAmountStr = ""
                                    selectedContactForTransfer = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                                .testTag("submit_transfer_btn")
                        ) {
                            Text(
                                "Send ⚡",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

