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
import com.example.R
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

    var selectedDoctorType by remember { mutableStateOf("All Doctors") }
    var selectedLocationType by remember { mutableStateOf("All Areas") }

    val filteredHospitals = remember(hospitals, searchQuery, selectedDoctorType, selectedLocationType) {
        hospitals.filter { hospital ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                hospital.name.contains(searchQuery, ignoreCase = true) ||
                hospital.location.contains(searchQuery, ignoreCase = true) ||
                hospital.specialties.any { it.contains(searchQuery, ignoreCase = true) }
            }

            val matchesDoctor = when (selectedDoctorType) {
                "All Doctors" -> true
                "Pediatrician" -> hospital.specialties.any { it.contains("Pediatr", ignoreCase = true) }
                "Gynecologist" -> hospital.specialties.any { it.contains("Gynecol", ignoreCase = true) || it.contains("natal", ignoreCase = true) }
                "Optician" -> hospital.specialties.any { it.contains("Optic", ignoreCase = true) || it.contains("Ophthal", ignoreCase = true) }
                "Cardiologist" -> hospital.specialties.any { it.contains("Cardio", ignoreCase = true) }
                "Dentist" -> hospital.specialties.any { it.contains("Dentis", ignoreCase = true) || it.contains("Dental", ignoreCase = true) }
                "General Practitioner" -> hospital.specialties.any { it.contains("General", ignoreCase = true) || it.contains("Practitioner", ignoreCase = true) }
                else -> true
            }

            val matchesLocation = when (selectedLocationType) {
                "All Areas" -> true
                "Closest (< 3km)" -> hospital.distanceKm <= 3.0
                "Lagos Island" -> hospital.location.contains("Lagos Island", ignoreCase = true)
                "Eti-Osa (VI/Lekki)" -> hospital.location.contains("Eti-Osa", ignoreCase = true) || hospital.location.contains("VI", ignoreCase = true) || hospital.location.contains("Lekki", ignoreCase = true)
                "Ikeja" -> hospital.location.contains("Ikeja", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesDoctor && matchesLocation
        }
    }

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

        // --- Filters Section ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose Doctor Specialty 🩺",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                    
                    val doctorTypes = listOf(
                        "All Doctors" to "🩺 All Doctors",
                        "Pediatrician" to "👶 Pediatrician",
                        "Gynecologist" to "🤰 Gynecologist",
                        "Optician" to "👁️ Optician",
                        "Cardiologist" to "🫀 Cardiologist",
                        "Dentist" to "🦷 Dentist",
                        "General Practitioner" to "👨‍⚕️ Gen Practitioner"
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        doctorTypes.forEach { (typeKey, label) ->
                            val isSelected = selectedDoctorType == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDoctorType = typeKey },
                                label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = CharcoalBlack
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = PrimaryPurple.copy(alpha = 0.3f),
                                    selectedBorderColor = PrimaryPurple
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "Filter by Location & Area 📍",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                    
                    val locationTypes = listOf(
                        "All Areas" to "🌍 All Areas",
                        "Closest (< 3km)" to "⚡ Closest (< 3km)",
                        "Lagos Island" to "🏙️ Lagos Island",
                        "Eti-Osa (VI/Lekki)" to "🏝️ VI / Lekki",
                        "Ikeja" to "🏡 Ikeja"
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        locationTypes.forEach { (locKey, label) ->
                            val isSelected = selectedLocationType == locKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLocationType = locKey },
                                label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryYellow,
                                    selectedLabelColor = CharcoalBlack,
                                    containerColor = Color.White,
                                    labelColor = CharcoalBlack
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = PrimaryPurple.copy(alpha = 0.3f),
                                    selectedBorderColor = PrimaryPurple
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- Directory Title ---
        item {
            Text(
                text = "Participating Medical Centres 🏥",
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
                        text = "Search by name, LGA, specialty...",
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
        if (filteredHospitals.isEmpty()) {
            item {
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
                            text = "No medical centers match your search or filter options.",
                            fontSize = 13.sp,
                            color = CharcoalBlack.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredHospitals, key = { it.name }) { hospital ->
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
            // Hospital image with picture
            val imageRes = getHospitalImageRes(hospital.name)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = imageRes),
                contentDescription = hospital.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))

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

fun getHospitalImageRes(name: String): Int {
    return when {
        name.contains("Nicholas", ignoreCase = true) -> R.drawable.img_hosp_st_nicholas_1782285464859
        name.contains("Reddington", ignoreCase = true) -> R.drawable.img_hosp_reddington_1782285482444
        name.contains("Evercare", ignoreCase = true) -> R.drawable.img_hosp_evercare_1782285499453
        name.contains("Ikeja", ignoreCase = true) -> R.drawable.img_hosp_ikeja_med_1782285513084
        else -> R.drawable.img_hosp_st_nicholas_1782285464859
    }
}
