package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun EventsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val balance by viewModel.walletBalance.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            YangaHeader(
                title = "Yanga Events! 🎟️✨",
                subtitle = "Discover premier concerts, culinary food exposures & coding networking meets",
                icon = Icons.Default.ConfirmationNumber
            )
        }

        // --- Wallet Info balance notice card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SecondaryYellow.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, PrimaryPurple, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Wallet balance", tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Yanga Pay Secure Verification",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Ticket fees are automatically debited from your secure wallet. Balance: ₦${String.format("%,.2f", balance)}",
                            fontSize = 10.sp,
                            color = CharcoalBlack.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- Events List Header ---
        item {
            Text(
                text = "Happening Soon in Lagos 🔥",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
        }

        item {
            DinnerCateringAndSandwichCustomizerCard(viewModel = viewModel)
        }

        // --- List items ---
        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
        } else {
            items(events, key = { it.title }) { event ->
                EventTicketCard(event = event, onPurchase = { viewModel.purchaseEventTicket(event) })
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EventTicketCard(
    event: com.example.domain.model.Event,
    onPurchase: () -> Unit
) {
    val isFree = event.price <= 0.0

    YangaVisuallyDistinctSection(
        title = event.title,
        subtitle = "Organized by ${event.host}",
        headerBadgeText = if (isFree) "FREE ACQUIRED" else "₦${String.format("%,.0f", event.price)}",
        headerBadgeColor = if (isFree) Color(0xFFDCFCE7) else SecondaryYellow,
        backgroundColor = Color.White,
        borderColor = PrimaryPurple,
        borderWidth = 2.0,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Beautiful FlowLayout wrapping all visual tag labels
        YangaFlowButtonsLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Venue Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF9C3)) // Pale Yellow brand block
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.venue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                }
            }

            // Date Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3E8FF)) // Purple brand block
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} at ${event.time}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalBlack
                    )
                }
            }

            // Attendance Capacity Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDCFCE7)) // Green brand block
                    .border(1.dp, Color(0xFF15803D).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "👥 ${event.rsvpCount} RSVP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF15803D)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = PrimaryPurple.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(10.dp))

        // Action Button
        Button(
            onClick = onPurchase,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(2.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                .testTag("reserve_event_${event.title.replace(" ", "_")}")
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = "Secure Ticket",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFree) "Register Free Seat" else "Buy Secure Entry Ticket",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun DinnerCateringAndSandwichCustomizerCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // 1. OO Carly DinnerEvent Instanciation
    val dinnerEvent = remember {
        com.example.domain.model.DinnerEvent(
            title = "Yanga Premium Dinner Gala",
            host = "Carly's Professional Caterers",
            date = "2026-07-25",
            time = "19:00",
            venue = "Eko Atlantic Hall, VI",
            initialGuests = 15,
            initialEventNumber = "A240"
        )
    }

    // 2. Local reactive states bound to OO mutators on change
    var eventTitle by remember { mutableStateOf("Yanga Premium Dinner Gala") }
    var eventNumber by remember { mutableStateOf("A240") }
    var guestsInputText by remember { mutableStateOf("15") }
    var contactPhone by remember { mutableStateOf("9208729182") }

    var selectedEntree by remember { mutableStateOf("Spicy Grilled Chicken") }
    val selectedSides = remember { mutableStateListOf("Jollof Rice", "Fried Plantain (Dodo)") }
    var selectedDessert by remember { mutableStateOf("Puff Puff with Ice Cream") }

    // Optional Features & Surcharges with respective Event Listeners
    var includePremiumSandwich by remember { mutableStateOf(false) }
    var includeDelivery by remember { mutableStateOf(false) }
    var includePriorityPrep by remember { mutableStateOf(false) }
    var includeEventInsurance by remember { mutableStateOf(false) }

    // 3. Fast-food sandwich listbox selections
    var selectedBread by remember { mutableStateOf("Brioche Roll") }
    var selectedFilling by remember { mutableStateOf("Smoked Suya Turkey") }
    var selectedSauce by remember { mutableStateOf("Spicy Yanga Mayo") }

    // 4. Declaring Explicit Listener Registries for the GUI (OOP Pattern requested)
    val deliveryItemListener = remember {
        object : CustomItemListener {
            override fun onItemStateChanged(selected: Boolean) {
                includeDelivery = selected
                android.util.Log.d("YangaListener", "Delivery Option listener triggered real-time state: $selected")
            }
        }
    }

    val priorityPrepItemListener = remember {
        object : CustomItemListener {
            override fun onItemStateChanged(selected: Boolean) {
                includePriorityPrep = selected
                android.util.Log.d("YangaListener", "Priority Prep Premium listener triggered real-time state: $selected")
            }
        }
    }

    val insuranceItemListener = remember {
        object : CustomItemListener {
            override fun onItemStateChanged(selected: Boolean) {
                includeEventInsurance = selected
                android.util.Log.d("YangaListener", "Event Insurance listener triggered real-time state: $selected")
            }
        }
    }

    val resetActionListener = remember {
        object : CustomActionListener {
            override fun actionPerformed() {
                eventTitle = "Yanga Premium Dinner Gala"
                eventNumber = "A240"
                guestsInputText = "15"
                contactPhone = "9208729182"
                selectedEntree = "Spicy Grilled Chicken"
                selectedSides.clear()
                selectedSides.add("Jollof Rice")
                selectedSides.add("Fried Plantain (Dodo)")
                selectedDessert = "Puff Puff with Ice Cream"
                includePremiumSandwich = false
                includeDelivery = false
                includePriorityPrep = false
                includeEventInsurance = false
                selectedBread = "Brioche Roll"
                selectedFilling = "Smoked Suya Turkey"
                selectedSauce = "Spicy Yanga Mayo"
                android.util.Log.d("YangaListener", "Reset Defaults ActionListener executed successfully!")
            }
        }
    }

    // Synchronize UI State back into Carly's OOP Model
    val guestsInt = guestsInputText.toIntOrNull() ?: 0
    dinnerEvent.setGuests(guestsInt)
    dinnerEvent.setEventNumber(eventNumber)
    dinnerEvent.setContactPhoneNumber(contactPhone)
    dinnerEvent.selectEntree(selectedEntree)
    dinnerEvent.selectSides(selectedSides.toTypedArray())
    dinnerEvent.selectDessert(selectedDessert)

    // Derived calculations from standard OOD methods & additional side/option/delivery listener state
    val calculatedBasePrice = dinnerEvent.getCalculatedPrice()
    
    // Surcharges computation
    val sandwichSurcharge = if (includePremiumSandwich) 8.0 else 0.0
    val extraSidesCount = (selectedSides.size - 2).coerceAtLeast(0)
    val extraSidesCost = extraSidesCount * 1500.0 // ₦1,500 flat per extra side dish
    
    val deliveryCost = if (includeDelivery) 1500.0 else 0.0
    val priorityPrepCost = if (includePriorityPrep) 3500.0 else 0.0
    val insuranceCost = if (includeEventInsurance) 2000.0 else 0.0

    val totalCateringPrice = calculatedBasePrice + (guestsInt * sandwichSurcharge) + extraSidesCost + deliveryCost + priorityPrepCost + insuranceCost
    val isLargeEvent = dinnerEvent.isLargeEvent()
    val formattedPhone = dinnerEvent.getContactPhoneNumber()

    YangaVisuallyDistinctSection(
        title = "Carly's Dinner Event & Fast-Food Builder 🎪🍽️",
        subtitle = "Build interactive catering quotations, guest requirements & sandwich selections using premium OOP logic:",
        headerBadgeText = "INTERACTIVE GUI",
        headerBadgeColor = Color(0xFFF3E8FF),
        backgroundColor = Color(0xFFFAF5FF), // pale purple
        borderColor = PrimaryPurple,
        borderWidth = 2.0,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Interactive Controls",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
            TextButton(
                onClick = { resetActionListener.actionPerformed() },
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryPurple),
                modifier = Modifier.height(28.dp).padding(0.dp).testTag("reset_defaults_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Icon", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Defaults", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Guest count and Event Number text fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = guestsInputText,
                onValueChange = { guestsInputText = it.filter { char -> char.isDigit() } },
                label = { Text("Guest Count", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("guests_text_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = eventNumber,
                onValueChange = { eventNumber = it.take(4) },
                label = { Text("Event ID (4 chars)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("event_id_text_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title and formatted phone number text fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = { Text("Event Title", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1.3f).testTag("title_text_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it.filter { char -> char.isDigit() }.take(10) },
                label = { Text("Contact Phone", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("contact_phone_text_field"),
                supportingText = { Text("Formats: $formattedPhone", fontSize = 9.sp, color = CharcoalBlack.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Listboxes Header
        Text(
            text = "Dinner Meal Choices (High-Fidelity GUI List Boxes) 🍖",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple
        )
        Text(
            text = "Select 1 entrée, at least 2 side dishes (extra sides are ₦1,500 flat), and 1 dessert:",
            fontSize = 10.sp,
            color = CharcoalBlack.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ENTRÉE LIST BOX (High-fidelity custom SELECT listbox scrollable widget)
        Text("Entrées (Select 1)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.5.dp, PrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                dinnerEvent.entrees.forEach { item ->
                    val isSelected = selectedEntree == item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEntree = item }
                            .background(if (isSelected) SecondaryYellow.copy(alpha = 0.3f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedEntree = item },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SIDE DISHES LIST BOX (Select as many side-dishes as desired, extra side dishes beyond 2 are ₦1,500 flat)
        Text("Side Dishes (2 standard included, extras: ₦1,500.00 each)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(
                    width = 1.5.dp,
                    color = if (selectedSides.size >= 2) Color(0xFF22C55E) else PrimaryPurple.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                )
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                dinnerEvent.sideDishes.forEach { item ->
                    val isSelected = selectedSides.contains(item)
                    
                    // Native registration of an item-state listener!
                    val sideCheckItemListener = remember(item) {
                        object : CustomItemListener {
                            override fun onItemStateChanged(selected: Boolean) {
                                if (selected) {
                                    if (!selectedSides.contains(item)) {
                                        selectedSides.add(item)
                                    }
                                } else {
                                    selectedSides.remove(item)
                                }
                                dinnerEvent.selectSides(selectedSides.toTypedArray())
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sideCheckItemListener.onItemStateChanged(!isSelected)
                            }
                            .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                sideCheckItemListener.onItemStateChanged(checked)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DESSERTS LIST BOX (Select 1)
        Text("Desserts (Select 1)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.5.dp, PrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                dinnerEvent.desserts.forEach { item ->
                    val isSelected = selectedDessert == item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDessert = item }
                            .background(if (isSelected) SecondaryYellow.copy(alpha = 0.3f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedDessert = item },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // INTERACTIVE SANDWICH CORNER
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFDBF).copy(alpha = 0.35f)), // warm white/yellow
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🥪 Customized Sandwich Add-On (+₦8/guest)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Switch(
                        checked = includePremiumSandwich,
                        onCheckedChange = { includePremiumSandwich = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = Color(0xFFF3E8FF))
                    )
                }

                if (includePremiumSandwich) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Customize your premium catering sandwiches in real-time below:",
                        fontSize = 10.sp,
                        color = CharcoalBlack.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sandwich Bread List Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bread Choice", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(75.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column {
                                    listOf("Brioche Roll", "Cheesy Sourdough", "Gluten-Free Wheat", "Traditional Baguette").forEach { choice ->
                                        val active = selectedBread == choice
                                        Text(
                                            text = choice,
                                            fontSize = 9.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            color = if (active) PrimaryPurple else CharcoalBlack,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedBread = choice }
                                                .background(if (active) SecondaryYellow.copy(alpha = 0.25f) else Color.Transparent)
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sandwich Filling List Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Filling Choice", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(75.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column {
                                    listOf("Smoked Suya Turkey", "Spicy Shredded Beef", "Pan-Seared Snail", "Grilled Paneer BBQ").forEach { choice ->
                                        val active = selectedFilling == choice
                                        Text(
                                            text = choice,
                                            fontSize = 9.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            color = if (active) PrimaryPurple else CharcoalBlack,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedFilling = choice }
                                                .background(if (active) SecondaryYellow.copy(alpha = 0.25f) else Color.Transparent)
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sandwich Sauce List Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sauce Choice", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(75.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column {
                                    listOf("Spicy Yanga Mayo", "Tangy Suya Pepper", "Creamy Garlic Spread", "Lagos Sweet Honey").forEach { choice ->
                                        val active = selectedSauce == choice
                                        Text(
                                            text = choice,
                                            fontSize = 9.sp,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            color = if (active) PrimaryPurple else CharcoalBlack,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedSauce = choice }
                                                .background(if (active) SecondaryYellow.copy(alpha = 0.25f) else Color.Transparent)
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // OPTIONAL FEATURES AND DELIVERY CHECKBOXES (WITH ACTIVE REGISTERED ITEM LISTENERS)
        Text(
            text = "Optional Superapp Delivery & Catering Upgrades 🚀",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple
        )
        Text(
            text = "Toggling these options registers real-time pricing listeners:",
            fontSize = 9.sp,
            color = CharcoalBlack.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(1.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Delivery Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { deliveryItemListener.onItemStateChanged(!includeDelivery) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeDelivery,
                    onCheckedChange = { deliveryItemListener.onItemStateChanged(it) },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple),
                    modifier = Modifier.testTag("delivery_checkbox")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Premium Superapp Delivery Option", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
                    Text("Secure professional dispatch to venue (+₦1,500.00)", fontSize = 9.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                }
            }

            // 2. Priority Chef Preparation Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { priorityPrepItemListener.onItemStateChanged(!includePriorityPrep) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includePriorityPrep,
                    onCheckedChange = { priorityPrepItemListener.onItemStateChanged(it) },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple),
                    modifier = Modifier.testTag("priority_prep_checkbox")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Priority Head-Chef Premium Service", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
                    Text("Expedited preparation & premium presentation (+₦3,500.00)", fontSize = 9.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                }
            }

            // 3. Event Refund Insurance Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { insuranceItemListener.onItemStateChanged(!includeEventInsurance) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeEventInsurance,
                    onCheckedChange = { insuranceItemListener.onItemStateChanged(it) },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple),
                    modifier = Modifier.testTag("event_insurance_checkbox")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Event Safeguard Reservation Insurance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
                    Text("No questions asked guest count refund guarantee (+₦2,000.00)", fontSize = 9.sp, color = CharcoalBlack.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CARLY'S OFFLINE-COMPUTED QUOTATION RECEIPT PANEL
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Carly's Professional Quotation Receipt 🏷️",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Catering Rate Code:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                    Text(
                        text = if (isLargeEvent) "Large Event Discounted (₦32/guest)" else "Standard Rate (₦35/guest)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLargeEvent) Color(0xFF16A34A) else CharcoalBlack
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Catering Base Cost:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                    Text(text = "₦${String.format("%,.2f", calculatedBasePrice)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (includePremiumSandwich) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Fast-food Sandwich Option:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                        Text(text = "₦${String.format("%,.2f", guestsInt * 8.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (extraSidesCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Extra Side Dishes (+$extraSidesCount):", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                        Text(text = "₦${String.format("%,.2f", extraSidesCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    }
                }

                if (includeDelivery) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Delivery Surcharge:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                        Text(text = "₦${String.format("%,.2f", deliveryCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (includePriorityPrep) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Chef Priority Surcharge:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                        Text(text = "₦${String.format("%,.2f", priorityPrepCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (includeEventInsurance) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Safeguard Insurance Fee:", fontSize = 11.sp, color = CharcoalBlack.copy(alpha = 0.6f))
                        Text(text = "₦${String.format("%,.2f", insuranceCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = PrimaryPurple.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Contract Pricing:", fontSize = 13.sp, fontWeight = FontWeight.Black, color = CharcoalBlack)
                    Text(
                        text = "₦${String.format("%,.2f", totalCateringPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secure checkout action
        val hasMinimumSides = selectedSides.size >= 2
        
        // Define Custom Action Listener for Booking Submission
        val bookActionListener = remember {
            object : CustomActionListener {
                override fun actionPerformed() {
                    val sidesStr = selectedSides.joinToString(" and ")
                    val deliveryText = if (includeDelivery) "Delivery Premium Enabled" else "No Delivery"
                    val extraText = "Catering Package: [Sides: $sidesStr, $deliveryText]" +
                            if (includePremiumSandwich) " + Sandwich [Bread: $selectedBread, Filling: $selectedFilling, Sauce: $selectedSauce]" else ""
                    
                    viewModel.bookDinnerCateringService(
                        title = eventTitle,
                        eventNumber = eventNumber,
                        guests = guestsInt,
                        totalPrice = totalCateringPrice,
                        contactPhone = formattedPhone,
                        entree = selectedEntree,
                        sides = sidesStr,
                        dessert = selectedDessert,
                        extraInfo = extraText
                    )
                }
            }
        }

        Button(
            onClick = {
                bookActionListener.actionPerformed()
            },
            enabled = hasMinimumSides && guestsInt > 0,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .border(2.dp, PrimaryPurple, RoundedCornerShape(10.dp))
                .testTag("book_catering_button")
        ) {
            Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = "Booking lock", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (!hasMinimumSides) {
                    "Select Standard 2 Sides First (Sides: ${selectedSides.size}/2)"
                } else if (guestsInt <= 0) {
                    "Provide Valid Guest Count"
                } else {
                    "Secure & Pay Catering Quotation"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

// --- Event-Driven GUI Listener Interfaces as required by Case Study Specs ---
interface CustomItemListener {
    fun onItemStateChanged(selected: Boolean)
}

interface CustomActionListener {
    fun actionPerformed()
}

