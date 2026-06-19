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
    modifier: Modifier = Modifier
) {
    val balance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.walletTransactions.collectAsState()

    var fundInputStr by remember { mutableStateOf("") }
    var securityAuditsEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            YangaHeader(
                title = "Yanga Pay! 💳🔑",
                subtitle = "Manage secure digital assets & ledger validations",
                icon = Icons.Default.Shield,
                onIconClick = { securityAuditsEnabled = !securityAuditsEnabled }
            )
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
            val goldCoins = balance.toInt() / 100
            val silverCoins = balance.toInt() % 100
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)), // Pale yellow background
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎒 Yanga Coin Purse System",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryPurple
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "1 GP = 100 SP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
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
                                .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
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
                                    text = "$goldCoins GP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB45309)
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
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
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
                                    text = "$silverCoins SP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack
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
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Every transaction converts seamlessly: your balance of ₦${String.format("%,.2f", balance)} is equivalent to $goldCoins Gold and $silverCoins Silver coins.",
                        fontSize = 10.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f),
                        lineHeight = 14.sp
                    )
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Security audit", tint = PrimaryPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "CRYPTOGRAPHIC AUDIT SYSTEM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every transaction is stamped with an in-app HMAC SHA-256 verification hash utilizing our custom salt keys. This prevents ledger tampering and guarantees secure local transaction loops.",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.7f),
                            lineHeight = 15.sp
                        )
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

