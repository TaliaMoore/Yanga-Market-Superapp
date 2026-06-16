package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

        // --- Directory Title ---
        item {
            Text(
                text = "Participating Medical Centres 🩺",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
        }

        // --- Interactive Hospital Rows ---
        if (hospitals.isEmpty()) {
            item { LoaderPlaceholder() }
        } else {
            items(hospitals) { hospital ->
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
