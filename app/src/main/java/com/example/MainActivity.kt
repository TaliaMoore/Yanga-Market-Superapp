package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.YangaStatusBanners
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        YangaSuperappShell()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YangaSuperappShell() {
  val viewModel: MainViewModel = viewModel()
  var currentRoute by remember { mutableStateOf("home") }

  // Observe global messaging state for ledger approvals or errors
  val errorBanner by viewModel.errorBannerMessage.collectAsState()
  val successBanner by viewModel.successBannerMessage.collectAsState()
  val isFetching by viewModel.isGraphQLFetching.collectAsState()

  // Navigation Items
  val navItems = listOf(
    NavigationTabItem("home", "Home", Icons.Default.Widgets),
    NavigationTabItem("market", "Market", Icons.Default.Storefront),
    NavigationTabItem("events", "Events", Icons.Default.ConfirmationNumber),
    NavigationTabItem("hospitals", "Care", Icons.Default.LocalHospital),
    NavigationTabItem("vibes", "Vibes", Icons.Default.Campaign),
    NavigationTabItem("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
  )

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing), // Protect from camera notch clipping at the top
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = PrimaryPurple,
        tonalElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .border(2.dp, PrimaryPurple.copy(alpha = 0.15f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .testTag("yanga_bottom_nav_bar")
      ) {
        navItems.forEach { item ->
          val selected = currentRoute == item.route
          NavigationBarItem(
            selected = selected,
            onClick = {
              viewModel.clearBanners()
              currentRoute = item.route
            },
            icon = {
              Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) PrimaryPurple else CharcoalBlack.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
              )
            },
            label = {
              Text(
                text = item.label,
                fontSize = 10.sp,
                color = if (selected) PrimaryPurple else CharcoalBlack.copy(alpha = 0.60f),
                style = MaterialTheme.typography.labelSmall
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = SecondaryYellow
            ),
            modifier = Modifier.testTag("nav_item_${item.route}")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // --- High-fidelity Status Banner notification system (shared across screens) ---
        YangaStatusBanners(
          errorMessage = errorBanner,
          successMessage = successBanner,
          onDismiss = { viewModel.clearBanners() }
        )

        // --- Loader Bar representing active GraphQL async transactions/queries ---
        if (isFetching) {
          LinearProgressIndicator(
            color = PrimaryPurple,
            trackColor = SecondaryYellow,
            modifier = Modifier
              .fillMaxWidth()
              .height(3.dp)
              .testTag("graphql_loading_indicator")
          )
        }

        // --- Active Screen Fragment Routing ---
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          when (currentRoute) {
            "home" -> DashboardScreen(
              viewModel = viewModel,
              onNavigate = { route ->
                viewModel.clearBanners()
                currentRoute = route
              }
            )
            "market" -> MarketScreen(viewModel = viewModel)
            "events" -> EventsScreen(viewModel = viewModel)
            "hospitals" -> HospitalScreen(viewModel = viewModel)
            "vibes" -> VibesScreen(viewModel = viewModel)
            "wallet" -> WalletScreen(viewModel = viewModel)
            else -> DashboardScreen(viewModel = viewModel, onNavigate = { currentRoute = it })
          }
        }
      }
    }
  }
}

data class NavigationTabItem(
  val route: String,
  val label: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)
