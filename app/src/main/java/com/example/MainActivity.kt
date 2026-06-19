package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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

/**
 * Lead Software Architect: Domain routing solution mimicking a HashRouter/NavLink browser history system.
 * Manages full navigation endpoints (with leading slashes, e.g. /home, /food, /events, /hospitals) 
 * and implements a functional backstack-based browser history state for seamless hardware back-button support.
 */
class YangaHashRouter(initialRoute: String = "/home") {
    var currentRoute by mutableStateOf(initialRoute)
        private set

    val backStack = mutableStateListOf<String>()

    init {
        // Initialize the browser history back stack with the starting route
        backStack.add(normalize(initialRoute))
    }

    /**
     * Navigates to a target route representation.
     * Sanitizes inputs to ensure browser paths, endpoints, and old key-strings map correctly.
     */
    fun navigate(route: String) {
        val normalized = normalize(route)
        if (currentRoute == normalized) return

        // To mimic browser navigation, push to the backstack
        backStack.add(normalized)
        currentRoute = normalized
        android.util.Log.d("YangaHashRouter", "NavLink navigate -> Push state: $normalized. Current History: $backStack")
    }

    /**
     * Pops from the backstack to simulate browser back transitions.
     * If there is history, navigate to the previous route; otherwise remain.
     * @return true if back-navigation was performed, false if stack is at root.
     */
    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLast()
            currentRoute = backStack.last()
            android.util.Log.d("YangaHashRouter", "History back -> Pop state. Current Route: $currentRoute. Remaining History: $backStack")
            return true
        }
        return false
    }

    /**
     * Normalizes inputs so slash paths and legacy string routes map perfectly to browser endpoints.
     */
    private fun normalize(route: String): String {
        val cleaned = if (route.startsWith("/")) route else "/$route"
        // Co-locate alias routes: /food aliases with /market
        return if (cleaned == "/food") "/market" else cleaned
    }
}

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
  val router = remember { YangaHashRouter("/home") }
  val currentRoute = router.currentRoute

  val isUserAuthenticated by viewModel.isUserAuthenticated.collectAsState()

  if (!isUserAuthenticated) {
    OnboardingScreen(viewModel = viewModel)
    return
  }

  // Capture hardware back button or gestural back swipe to navigate back through router history
  BackHandler(enabled = router.backStack.size > 1) {
    router.navigateBack()
  }

  // Observe global messaging state for ledger approvals or errors
  val errorBanner by viewModel.errorBannerMessage.collectAsState()
  val successBanner by viewModel.successBannerMessage.collectAsState()
  val isFetching by viewModel.isGraphQLFetching.collectAsState()

  // Navigation Items
  val navItems = listOf(
    NavigationTabItem("/home", "Home", Icons.Default.Widgets),
    NavigationTabItem("/market", "Market", Icons.Default.Storefront),
    NavigationTabItem("/events", "Events", Icons.Default.ConfirmationNumber),
    NavigationTabItem("/hospitals", "Care", Icons.Default.LocalHospital),
    NavigationTabItem("/services", "Freelance", Icons.Default.Work),
    NavigationTabItem("/vibes", "Vibes", Icons.Default.Campaign),
    NavigationTabItem("/wallet", "Wallet", Icons.Default.AccountBalanceWallet)
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
              router.navigate(item.route)
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
            modifier = Modifier.testTag("nav_item_${item.route.replace("/", "")}")
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
            "/home" -> DashboardScreen(
              viewModel = viewModel,
              onNavigate = { route ->
                viewModel.clearBanners()
                router.navigate(route)
              }
            )
            "/market" -> MarketScreen(viewModel = viewModel)
            "/events" -> EventsScreen(viewModel = viewModel)
            "/hospitals" -> HospitalScreen(viewModel = viewModel)
            "/services" -> ServicesScreen(viewModel = viewModel)
            "/vibes" -> VibesScreen(viewModel = viewModel)
            "/wallet" -> WalletScreen(viewModel = viewModel)
            "/yanga_rider" -> YangaRiderScreen(onNavigateBack = { router.navigateBack() })
            "/bulkbuy", "/bulkbuy5k", "/bulkbuy10k", "/bulkbuy15k", "/bulkbuy30k" -> {
              val initId = when (currentRoute) {
                "/bulkbuy10k" -> "box_10k"
                "/bulkbuy15k" -> "box_15k"
                "/bulkbuy30k" -> "box_30k"
                else -> "box_5k"
              }
              BulkBuyScreen(
                viewModel = viewModel,
                initialBoxId = initId,
                onNavigateBack = { router.navigateBack() }
              )
            }
            else -> DashboardScreen(viewModel = viewModel, onNavigate = { router.navigate(it) })
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
