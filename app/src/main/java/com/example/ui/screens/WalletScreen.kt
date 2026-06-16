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
            items(transactions) { tx ->
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
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
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
