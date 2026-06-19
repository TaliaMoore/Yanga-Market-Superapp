package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow

@Composable
fun HospitalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val hospitals by viewModel.hospitals.collectAsState()
    val searchQuery by viewModel.hospitalSearchQuery.collectAsState()
    val hospitalSearchError by viewModel.hospitalSearchError.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            YangaHeader(
                title = "Yanga Care! 🩺🏥",
                subtitle = "Discover specialized clinics, diagnostic labs & emergency hospital services near you",
                icon = Icons.Default.MedicalServices
            )
        }

        // --- Alert info banner ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // Soft green background
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Health notice",
                        tint = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Booking medical slots is completely subsidized. No wallet charges will be made for general consultations and lab checks. 🟢 Medical Space secured.",
                        fontSize = 11.sp,
                        color = Color(0xFF14532D),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // --- Shell-Style Interactive Command Line Terminal ---
        item {
            HospitalShellConsoleCard(viewModel = viewModel)
        }

        // --- Directory Title ---
        item {
            Text(
                text = "Participating Medical Centres 🩺",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
        }

        // --- Search bar styled elegantly ---
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateHospitalSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hospital_search_input"),
                placeholder = {
                    Text(
                        text = "Search by name, location, or services...",
                        fontSize = 13.sp,
                        color = CharcoalBlack.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = PrimaryPurple
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateHospitalSearchQuery("") },
                            modifier = Modifier.testTag("hospital_search_clear")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = CharcoalBlack.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // --- Interactive Hospital Rows ---
        if (hospitals.isEmpty()) {
            item {
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No results",
                                modifier = Modifier.size(48.dp),
                                tint = CharcoalBlack.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = hospitalSearchError ?: "No medical centers found matching your query.",
                                fontSize = 13.sp,
                                color = if (hospitalSearchError != null) Color(0xFFC2410C) else CharcoalBlack.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    LoaderPlaceholder()
                }
            }
        } else {
            items(hospitals, key = { it.name }) { hospital ->
                HospitalDiscoveryCard(hospital = hospital, onBook = { service, date ->
                    viewModel.bookHospitalService(hospital, service, date)
                })
            }
        }
    }
}

@Composable
fun HospitalDiscoveryCard(
    hospital: com.example.domain.model.Hospital,
    onBook: (String, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf(hospital.specialties.firstOrNull() ?: "General Clinic") }
    
    val dates = listOf("Tomorrow", "Next Monday", "Next Friday")
    var selectedDate by remember { mutableStateOf("Tomorrow") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title, distance, location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Medical cross",
                            tint = Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = hospital.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CharcoalBlack
                        )
                        Text(
                            text = "${hospital.location} • ${hospital.distanceKm}km away",
                            fontSize = 11.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Button(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryYellow, contentColor = CharcoalBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .border(1.2.dp, PrimaryPurple, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = if (isExpanded) "Close" else "Book",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Specialties tags listing
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                hospital.specialties.take(3).forEach { specialty ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = specialty,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            // Expanded Schedulers Block
            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = PrimaryPurple.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(10.dp))

                // Select specialized department
                Text(
                    text = "Select Specialized Medical Service:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    hospital.specialties.forEach { service ->
                        val active = service == selectedService
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0xFFEFF6FF) else Color(0xFFF9F9F9))
                                .border(1.dp, if (active) Color(0xFF2563EB) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedService = service }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = active,
                                onClick = { selectedService = service },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = service,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule dates
                Text(
                    text = "Preferred Date Slot:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    for (date in dates) {
                        val active = date == selectedDate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) PrimaryPurple else Color(0xFFF1F1F1))
                                .border(1.dp, if (active) PrimaryPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedDate = date }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else CharcoalBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Final trigger button
                Button(
                    onClick = {
                        onBook(selectedService, selectedDate)
                        isExpanded = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.5.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                        .testTag("submit_hospital_booking_${hospital.name.replace(" ", "_")}")
                ) {
                    Text(
                        text = "Schedule Diagnostic Booking & Secure Slot 🩺",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun HospitalShellConsoleCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val consoleInput by viewModel.terminalConsoleInput.collectAsState()
    val consoleLogs by viewModel.terminalLogs.collectAsState()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Dark console container
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, PrimaryPurple, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Screen Header
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
                            .size(10.dp)
                            .background(Color.Red, RoundedCornerShape(5.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Yellow, RoundedCornerShape(5.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Green, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "yanga-sh : hospital-admin",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = { viewModel.executeTerminalCommand("clear") },
                    modifier = Modifier.size(24.dp).testTag("terminal_clear_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart console / Clear",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Output Log Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(consoleLogs, key = { index, _ -> "hospital_console_log_$index" }) { index, log ->
                        Text(
                            text = log,
                            color = if (log.startsWith("$")) Color(0xFFE9D5FF) else if (log.contains("Error")) Color(0xFFFCA5A5) else if (log.contains("Success")) Color(0xFF86EFAC) else Color(0xFFF3F4F6),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text input with prompt prefix
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$",
                    color = PrimaryPurple,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                OutlinedTextField(
                    value = consoleInput,
                    onValueChange = { viewModel.updateTerminalInput(it) },
                    placeholder = {
                        Text(
                            text = "e.g., 1 Lagoon or help",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("terminal_console_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedContainerColor = Color.Black,
                        unfocusedContainerColor = Color.Black,
                        cursorColor = PrimaryPurple
                    ),
                    singleLine = true
                )

                Button(
                    onClick = { viewModel.executeTerminalCommand(consoleInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.testTag("terminal_execute_btn")
                ) {
                    Text(
                        text = "Run",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Command Buttons
            Text(
                text = "Preset Command Shortcuts:",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Preset 1: Lookup
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(6.dp))
                        .clickable { viewModel.updateTerminalInput("1 Lagoon") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "(1) Lookup Lagoon",
                        color = Color(0xFFE9D5FF),
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                // Preset 2: Add
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(6.dp))
                        .clickable {
                            viewModel.updateTerminalInput("2 Reddington Lekki, Admiralty Way Lekki, 6.2, Cardiology;Orthopedics")
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "(2) Add Reddington",
                        color = Color(0xFFD1FAE5),
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                // Preset 3: Remove
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(6.dp))
                        .clickable { viewModel.updateTerminalInput("3 Lagoon Hospital") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "(3) Remove Lagoon",
                        color = Color(0xFFFCA5A5),
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                // Help
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(6.dp))
                        .clickable { viewModel.executeTerminalCommand("help") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "help",
                        color = Color(0xFF93C5FD),
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}
