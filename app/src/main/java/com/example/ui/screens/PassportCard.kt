package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.AppointmentNotification
import com.example.data.network.PassportStatus
import com.example.data.network.PassportVaultItem
import com.example.data.network.YangaPassportProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PlayfulBg
import com.example.ui.theme.SecondaryYellow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun YangaPassportSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.passportStatus.collectAsState()
    val profile by viewModel.passportProfile.collectAsState()
    val vaultItems by viewModel.passportVaultItems.collectAsState()
    val notifications by viewModel.passportNotifications.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Profile/Identity, 1: Vault Storage, 2: Alerts Center

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, PrimaryPurple, RoundedCornerShape(24.dp))
            .testTag("yanga_passport_main_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- HEADER TITLE BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(SecondaryYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = "Passport Symbol",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Yanga Passport Gateway 🛂",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Secure decentralized identification & credentials",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (status == PassportStatus.LINKED) {
                        Surface(
                            color = Color(0xFF16A34A),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // --- TAB SELECTOR (Only if logged in) ---
            if (status == PassportStatus.LINKED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB))
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "My ID Badge", Icons.Default.VerifiedUser),
                        Triple(1, "Vault Storage", Icons.Default.Dataset),
                        Triple(2, "Alerts (${notifications.size})", Icons.Default.NotificationsActive)
                    )
                    tabs.forEach { (index, label, icon) ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) PrimaryPurple else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryPurple else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { activeTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else CharcoalBlack.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else CharcoalBlack
                                )
                            }
                        }
                    }
                }
                Divider(color = Color(0xFFE5E7EB))
            }

            // --- MAIN BODY SWITCHER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                when (status) {
                    PassportStatus.UNLINKED -> {
                        PassportRegistrationForm(onEnroll = { name, email, bio ->
                            viewModel.enrollPassport(name, email, bio)
                        })
                    }
                    PassportStatus.VERIFYING -> {
                        PassportLoadingEnrollment()
                    }
                    PassportStatus.LINKED -> {
                        profile?.let { prof ->
                            when (activeTab) {
                                0 -> PassportBadgeView(
                                    profile = prof,
                                    onUnlink = { viewModel.unlinkPassportProfile() }
                                )
                                1 -> PassportVaultView(
                                    vaultItems = vaultItems,
                                    onSaveSecret = { t, v, c -> viewModel.addToPassportVault(t, v, c) },
                                    onDeleteSecret = { viewModel.deleteFromPassportVault(it) }
                                )
                                2 -> PassportNotificationsView(
                                    notifications = notifications,
                                    onDismiss = { viewModel.dismissPassportNotification(it) },
                                    onRead = { viewModel.markPassportNotificationAsRead(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-VIEW 1: REGISTRATION COMPOSABLE ---
@Composable
fun PassportRegistrationForm(
    onEnroll: (String, String, Boolean) -> Unit
) {
    var regName by remember { mutableStateOf("Eniola Agbeyindo") }
    var regEmail by remember { mutableStateOf("eniolaagbeyindo@gmail.com") }
    var biometricEnabled by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecondaryYellow.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .border(1.dp, SecondaryYellow, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Safe Lock",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Lagos state requires verified digital identification credentials for instant biometric hospital admissions, pharmacy clearance, and event check-ins. Set up your Yanga Passport now.",
                    fontSize = 11.sp,
                    color = CharcoalBlack.copy(alpha = 0.8f),
                    lineHeight = 15.sp
                )
            }
        }

        OutlinedTextField(
            value = regName,
            onValueChange = { regName = it },
            label = { Text("Verify Identity Full Name") },
            placeholder = { Text("e.g. Eniola Agbeyindo") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
                .testTag("passport_reg_name_input")
        )

        OutlinedTextField(
            value = regEmail,
            onValueChange = { regEmail = it },
            label = { Text("Linked Secured Email") },
            placeholder = { Text("e.g. eniolaagbeyindo@gmail.com") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
                .testTag("passport_reg_email_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "biometrics icon",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Enable Biometric Signature",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                    Text(
                        text = "Approve transactions with fingerprint",
                        fontSize = 10.sp,
                        color = CharcoalBlack.copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = biometricEnabled,
                onCheckedChange = { biometricEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryPurple
                ),
                modifier = Modifier.testTag("passport_biometric_switch")
            )
        }

        Button(
            onClick = { onEnroll(regName, regEmail, biometricEnabled) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("passport_enroll_submit_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Generate Passport & Link Biometrics ⚡🤝",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// --- SUB-VIEW 2: LOADING COMPOSABLE ---
@Composable
fun PassportLoadingEnrollment() {
    val infiniteTransition = rememberInfiniteTransition(label = "face_scan_anim")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(PrimaryPurple.copy(alpha = 0.1f), CircleShape)
                .border(2.dp, PrimaryPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Facial recognition loader",
                tint = PrimaryPurple,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer(alpha = alphaAnim)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Authenticating with Lagos Security Registry...",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = CharcoalBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Enrolling digital public-key signature pair on-device securely. Creating cryptographic safe-hashes...",
            fontSize = 11.sp,
            color = CharcoalBlack.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// --- SUB-VIEW 3: IDENTITY BADGE COMPOSABLE ---
@Composable
fun PassportBadgeView(
    profile: YangaPassportProfile,
    onUnlink: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // High fidelity Passport card body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFDFDFD),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(2.dp, SecondaryYellow, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WEST AFRICAN DIGITAL REUNION",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "YANGA SUPERAPP HEALTH PASSPORT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalBlack
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(SecondaryYellow, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CLASS-A ID",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryPurple
                        )
                    }
                }

                Divider(color = Color(0xFFF1F1F1))

                // User details layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar profile
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .border(1.dp, PrimaryPurple, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Passport Photo Avatar",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Properties
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row {
                            Text("Full Name: ", fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                            Text(profile.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                        }
                        Row {
                            Text("Passport ID: ", fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                            Text(profile.passportId, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryPurple)
                        }
                        Row {
                            Text("Email Link: ", fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                            Text(profile.email, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                        }
                        Row {
                            Text("Issued: ", fontSize = 10.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                            Text(profile.issueDate, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        }
                    }
                }

                Divider(color = Color(0xFFF1F1F1))

                // Bottom security metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SECURE LOCAL CRYPTO SIGNATURE KEY",
                            fontSize = 8.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = profile.signatureToken,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalBlack
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Active biometric indicator",
                            tint = if (profile.biometricEnabled) Color(0xFF16A34A) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (profile.biometricEnabled) "BIO ACTIVE" else "NO PIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (profile.biometricEnabled) Color(0xFF16A34A) else Color.Gray
                        )
                    }
                }
            }
        }

        // Action options
        Button(
            onClick = onUnlink,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                .testTag("passport_unlink_btn")
        ) {
            Text(
                text = "Disconnect Passport Keys & Unlink Gateway 🔐🚫",
                color = Color(0xFFDC2626),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- SUB-VIEW 4: VAULT SECURE STORAGE COMPOSABLE ---
@Composable
fun PassportVaultView(
    vaultItems: List<PassportVaultItem>,
    onSaveSecret: (String, String, String) -> Unit,
    onDeleteSecret: (String) -> Unit
) {
    var secretTitle by remember { mutableStateOf("") }
    var secretValue by remember { mutableStateOf("") }
    var secretType by remember { mutableStateOf("Wallet Key Backup") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "🔒 Passport Secure Vault operates client-side only. All strings saved are masked and reside purely in sandboxed device databases. Excellent for ledger private recovery keys or medical pin references.",
                fontSize = 11.sp,
                color = CharcoalBlack.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )
        }

        // Vault item entry list
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Encrypted Document Store Locker (${vaultItems.size} items)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (vaultItems.isEmpty()) {
                    Text(
                        text = " اللوكر فارغ - No secret credentials archived.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    vaultItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        color = PrimaryPurple.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalBlack
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.value,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Gray
                                )
                            }

                            IconButton(
                                onClick = { onDeleteSecret(item.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Destroy",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Form to add new item
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.5.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Locker Cabinet Deposit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack
                )

                OutlinedTextField(
                    value = secretTitle,
                    onValueChange = { secretTitle = it },
                    label = { Text("Credential Title") },
                    placeholder = { Text("e.g. Health Insurance Code") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                OutlinedTextField(
                    value = secretValue,
                    onValueChange = { secretValue = it },
                    label = { Text("Private Masked Value") },
                    placeholder = { Text("e.g. NHIS-1849-291X") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                // Quick selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Wallet Key Backup", "Medical Card ID", "Access Voucher").forEach { cat ->
                        val isSelected = secretType == cat
                        Surface(
                            color = if (isSelected) PrimaryPurple else Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { secretType = cat }
                                .padding(vertical = 4.dp),
                            border = BorderStroke(1.dp, if (isSelected) PrimaryPurple else Color(0xFFE5E7EB))
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CharcoalBlack,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (secretTitle.isNotEmpty() && secretValue.isNotEmpty()) {
                            onSaveSecret(secretTitle, secretValue, secretType)
                            secretTitle = ""
                            secretValue = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Secure & Lock Credential 🔐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SUB-VIEW 5: ALERTS & REMINDERS COMPOSABLE ---
@Composable
fun PassportNotificationsView(
    notifications: List<AppointmentNotification>,
    onDismiss: (String) -> Unit,
    onRead: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFAF0), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Event alert icon",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Calendar handshakes automatically import your Care and Event RSVPs into Passport logs for real-time compliance alerts on Lagos timezone locks.",
                    fontSize = 11.sp,
                    color = Color(0xFF92400E)
                )
            }
        }

        if (notifications.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "empty alerts",
                    tint = Color.LightGray,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All schedules are synced. No pending appointments.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            notifications.forEach { model ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (model.isRead) Color(0xFFF9FAFB) else Color(0xFFFFFBEB)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (model.isRead) 1.dp else 1.5.dp,
                            color = if (model.isRead) Color(0xFFE5E7EB) else Color(0xFFFCD34D),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (model.isRead) Color.Gray else Color(0xFFF59E0B),
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = model.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CharcoalBlack
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = model.description,
                                    fontSize = 11.sp,
                                    color = CharcoalBlack.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Schedule Time icon",
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = model.appointmentTime,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!model.isRead) {
                                    IconButton(
                                        onClick = { onRead(model.notificationId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Acknowledge",
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDismiss(model.notificationId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss reminder",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
