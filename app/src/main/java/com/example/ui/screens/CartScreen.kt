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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.YangaHeader
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.PlayfulCream
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryYellow
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val rawWalletBalance by viewModel.walletBalance.collectAsState()

    val cartCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.price * it.quantity }
    
    // Choose delivery amount or event免 charge
    val hasEventItemsOnly = remember(cartItems) {
        cartItems.isNotEmpty() && cartItems.all { it.itemType == "EVENT" }
    }
    val deliveryFee = if (cartItems.isEmpty() || hasEventItemsOnly) 0.0 else 450.0
    val grandTotal = cartSubtotal + deliveryFee

    var selectedPaymentMethod by remember { mutableStateOf("WALLET") } // WALLET, BANK_TRANSFER, COD

    val checkoutPinState by viewModel.checkoutPin.collectAsState()

    var showPinPromptDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var confirmNewPinInput by remember { mutableStateOf("") }
    var setupPinErrorMsg by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // --- Top Row with Back Button ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlayfulCream)
                    .border(1.5.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "My Shopping Basket",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CharcoalBlack
            )
            Spacer(modifier = Modifier.weight(1f))
            Badge(
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = "$cartCount items",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Divider(color = PrimaryPurple.copy(alpha = 0.1f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "🛒✨",
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Basket is Empty!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add delicious marketplace food, fresh farm fruits, secure event vibes, or delivery rides to checkout.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = CharcoalBlack.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Explore Market", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // List of item details
                items(cartItems, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PlayfulCream),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val badgeColor = if (item.itemType == "EVENT") SecondaryYellow else Color(0xFFDCFCE7)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.itemType,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CharcoalBlack
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "₦${String.format("%,.2f", item.price)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalBlack.copy(alpha = 0.7f)
                                )
                            }
                            
                            // Adjust Quantities Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { viewModel.modifyCartQuantity(item, -1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Decrease",
                                        tint = PrimaryPurple
                                    )
                                }
                                Text(
                                    text = item.quantity.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.modifyCartQuantity(item, 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = "Increase",
                                        tint = PrimaryPurple
                                    )
                                }
                            }
                        }
                    }
                }

                // Billing breakdown block
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Payment Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = PlayfulCream),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bag Subtotal", fontSize = 12.sp, color = CharcoalBlack.copy(alpha = 0.65f))
                                Text("₦${String.format("%,.2f", cartSubtotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Transport / Service Charge", fontSize = 12.sp, color = CharcoalBlack.copy(alpha = 0.65f))
                                Text(
                                    text = if (deliveryFee == 0.0) "FREE" else "₦${String.format("%,.2f", deliveryFee)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (deliveryFee == 0.0) Color(0xFF15803D) else CharcoalBlack
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = PrimaryPurple.copy(alpha = 0.12f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount Due", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalBlack)
                                Text(
                                    text = "₦${String.format("%,.2f", grandTotal)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                }

                // Choose Payment Method Block
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Choose Payment Option",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CharcoalBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WALLET Option
                        PaymentMethodRow(
                            title = "Secure Yanga Pay Wallet",
                            subtitle = "Automatic debit. Wallet Balance: ₦${String.format("%,.2f", rawWalletBalance)}",
                            selected = selectedPaymentMethod == "WALLET",
                            icon = Icons.Default.Wallet,
                            onClick = { selectedPaymentMethod = "WALLET" }
                        )

                        // BANK_TRANSFER Option
                        PaymentMethodRow(
                            title = "Direct Bank Transfer",
                            subtitle = "Instantly pay to Yanga virtual bank account",
                            selected = selectedPaymentMethod == "BANK_TRANSFER",
                            icon = Icons.Default.AccountBalance,
                            onClick = { selectedPaymentMethod = "BANK_TRANSFER" }
                        )

                        // CASH_ON_DELIVERY Option
                        PaymentMethodRow(
                            title = "Host Cash on Delivery / POS",
                            subtitle = "Pay physical cash or card swipe upon arrival",
                            selected = selectedPaymentMethod == "COD",
                            icon = Icons.Default.DirectionsBike,
                            onClick = { selectedPaymentMethod = "COD" }
                        )
                    }

                    // Virtual Bank details block if Bank Transfer is chosen
                    if (selectedPaymentMethod == "BANK_TRANSFER") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SecondaryYellow.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, SecondaryYellow, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Yanga Virtual Bank Details 🏦",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Transfer ₦${String.format("%,.2f", grandTotal)} to our designated account below:",
                                    fontSize = 10.sp,
                                    color = CharcoalBlack
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Bank: Yanga Microfinance Bank Ltd\nAccount Name: Yanga Market Super Ltd\nAccount Number: 9942039281",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalBlack,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your order will be instantly verified and processed upon receiving payload transfer.",
                                    fontSize = 10.sp,
                                    color = CharcoalBlack.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Checkout Actions button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (checkoutPinState.isEmpty()) {
                                // Pin not configured yet, force pin setup first!
                                newPinInput = ""
                                confirmNewPinInput = ""
                                setupPinErrorMsg = ""
                                showPinSetupDialog = true
                            } else {
                                // Pin is configured, prompt for it!
                                enteredPin = ""
                                pinError = false
                                showPinPromptDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("cart_checkout_page_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirm",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPaymentMethod == "WALLET") {
                                "Complete Wallet Payment"
                            } else if (selectedPaymentMethod == "BANK_TRANSFER") {
                                "I have transferred ₦${String.format("%,.0f", grandTotal)}"
                            } else {
                                "Place Order - Cash on Delivery"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // --- Pin Setup Dialog ---
    if (showPinSetupDialog) {
        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text("Configure Checkout PIN 🔐", fontWeight = FontWeight.Bold, color = PrimaryPurple) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("A 4-digit security PIN is required to complete and approve transactions on Yanga Market. Configure yours below:", fontSize = 11.sp, color = CharcoalBlack)

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { 
                            if (it.length <= 4) newPinInput = it.filter { c -> c.isDigit() }
                            setupPinErrorMsg = ""
                        },
                        label = { Text("Enter 4-Digit PIN") },
                        placeholder = { Text("e.g. 1234") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmNewPinInput,
                        onValueChange = { 
                            if (it.length <= 4) confirmNewPinInput = it.filter { c -> c.isDigit() }
                            setupPinErrorMsg = ""
                        },
                        label = { Text("Confirm 4-Digit PIN") },
                        placeholder = { Text("e.g. 1234") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (setupPinErrorMsg.isNotEmpty()) {
                        Text(setupPinErrorMsg, fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPinSetupDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalBlack.copy(alpha = 0.1f), contentColor = CharcoalBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (newPinInput.length != 4) {
                                    setupPinErrorMsg = "PIN must be exactly 4 digits long!"
                                } else if (newPinInput != confirmNewPinInput) {
                                    setupPinErrorMsg = "PINs do not match!"
                                } else {
                                    viewModel.setCheckoutPin(newPinInput)
                                    showPinSetupDialog = false
                                    // Successfully configured! Immediately trigger verification check to approve
                                    enteredPin = ""
                                    pinError = false
                                    showPinPromptDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Save & Continue", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }

    // --- Pin Verification Dialog ---
    if (showPinPromptDialog) {
        AlertDialog(
            onDismissRequest = { showPinPromptDialog = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text("Enter Security PIN 🔐", fontWeight = FontWeight.Bold, color = PrimaryPurple) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Enter your 4-digit transaction approval PIN to finalize your purchase:", fontSize = 11.sp, color = CharcoalBlack)

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { 
                            if (it.length <= 4) enteredPin = it.filter { c -> c.isDigit() }
                            pinError = false
                        },
                        label = { Text("Approval PIN") },
                        placeholder = { Text("Enter 4 digits") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError) {
                        Text("⚠️ Incorrect security PIN! Please try again.", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPinPromptDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalBlack.copy(alpha = 0.1f), contentColor = CharcoalBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (enteredPin == checkoutPinState) {
                                    showPinPromptDialog = false
                                    viewModel.checkoutCart(paymentMethod = selectedPaymentMethod)
                                    onNavigateBack()
                                } else {
                                    pinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Approve Payment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val borderColor = if (selected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.15f)
    val borderWidth = if (selected) 2.dp else 1.dp
    val backgroundColor = if (selected) SecondaryYellow.copy(alpha = 0.12f) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PrimaryPurple,
                unselectedColor = PrimaryPurple.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = "Option Icon",
            tint = PrimaryPurple,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalBlack
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = CharcoalBlack.copy(alpha = 0.6f)
            )
        }
    }
}
