package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FavoriteRestaurant
import com.example.data.ProductReview
import com.example.data.UserProfile
import com.example.data.repository.Product
import com.example.data.repository.Restaurant
import com.example.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalalAppContainer() {
    val viewModel: HalalViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoritesList.collectAsStateWithLifecycle()

    val currentPalette = viewModel.accentPalettes.getOrNull(userProfile.accentColorIndex)
        ?: viewModel.accentPalettes.first()

    MyApplicationTheme(
        customPrimaryColor = Color(currentPalette.primary),
        customContainerColor = Color(currentPalette.container)
    ) {
        val configuration = LocalConfiguration.current
        val isTablet = configuration.screenWidthDp > 600

        var currentTab by remember { mutableStateOf("dashboard") } // "dashboard", "scan", "restaurants", "products", "profile"

        if (isTablet) {
            // Adaptive Tablet Layout: Side Navigation Rail + Main Area
            Row(modifier = Modifier.fillMaxSize().navigationBarsPadding().statusBarsPadding()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = "Halal App Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Halal Finder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    NavigationRailItem(
                        selected = currentTab == "dashboard",
                        onClick = { currentTab = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("tablet_nav_home")
                    )
                    NavigationRailItem(
                        selected = currentTab == "scan",
                        onClick = { currentTab = "scan" },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Additives") },
                        label = { Text("Scan") },
                        modifier = Modifier.testTag("tablet_nav_scan")
                    )
                    NavigationRailItem(
                        selected = currentTab == "restaurants",
                        onClick = { currentTab = "restaurants" },
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Certified Diners") },
                        label = { Text("Diners") },
                        modifier = Modifier.testTag("tablet_nav_restaurants")
                    )
                    NavigationRailItem(
                        selected = currentTab == "products",
                        onClick = { currentTab = "products" },
                        icon = { Icon(Icons.Default.Category, contentDescription = "Browse Catalog") },
                        label = { Text("Products") },
                        modifier = Modifier.testTag("tablet_nav_products")
                    )
                    NavigationRailItem(
                        selected = currentTab == "profile",
                        onClick = { currentTab = "profile" },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile Settings") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("tablet_nav_profile")
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    TabContent(currentTab, viewModel, isTablet) { currentTab = it }
                }
            }
        } else {
            // Adaptive Phone Layout: Bottom Bar + Scrollable Core View
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == "dashboard",
                            onClick = { currentTab = "dashboard" },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                            label = { Text("Home", style = TextStyleCompact) },
                            modifier = Modifier.testTag("phone_nav_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == "scan",
                            onClick = { currentTab = "scan" },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Ingredients") },
                            label = { Text("Scan", style = TextStyleCompact) },
                            modifier = Modifier.testTag("phone_nav_scan")
                        )
                        NavigationBarItem(
                            selected = currentTab == "restaurants",
                            onClick = { currentTab = "restaurants" },
                            icon = { Icon(Icons.Default.Restaurant, contentDescription = "Diners") },
                            label = { Text("Diners", style = TextStyleCompact) },
                            modifier = Modifier.testTag("phone_nav_restaurants")
                        )
                        NavigationBarItem(
                            selected = currentTab == "products",
                            onClick = { currentTab = "products" },
                            icon = { Icon(Icons.Default.Category, contentDescription = "Products") },
                            label = { Text("Products", style = TextStyleCompact) },
                            modifier = Modifier.testTag("phone_nav_products")
                        )
                        NavigationBarItem(
                            selected = currentTab == "profile",
                            onClick = { currentTab = "profile" },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", style = TextStyleCompact) },
                            modifier = Modifier.testTag("phone_nav_profile")
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    TabContent(currentTab, viewModel, isTablet) { currentTab = it }
                }
            }
        }
    }
}

val TextStyleCompact = TextStyle(fontSize = 11.sp)

@Composable
fun TabContent(tab: String, viewModel: HalalViewModel, isTablet: Boolean, onTabChange: (String) -> Unit = {}) {
    AnimatedContent(
        targetState = tab,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "tab_transition"
    ) { targetTab ->
        when (targetTab) {
            "dashboard" -> DashboardScreen(viewModel, isTablet, onTabChange)
            "scan" -> ScannerScreen(viewModel, isTablet)
            "restaurants" -> RestaurantsScreen(viewModel, isTablet)
            "products" -> ProductsCatalogScreen(viewModel, isTablet)
            "profile" -> ProfileSettingsScreen(viewModel, isTablet)
        }
    }
}

// -------------------------------------------------------------
// DASHBOARD / HOME SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: HalalViewModel, isTablet: Boolean, onTabChange: (String) -> Unit = {}) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val nextPrayer by viewModel.nextPrayerState.collectAsStateWithLifecycle()
    val restaurants by viewModel.restaurantsList.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoritesList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. TOP APP BAR (Bento Styled Header)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Verified Badge (Green box rounded-xl)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF386B40)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "HalalScan Finder",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Text(
                        text = "As-salamu alaykum, ${userProfile.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1D1B20).copy(alpha = 0.6f)
                    )
                }
            }

            // Quick profile/settings action button
            IconButton(
                onClick = { onTabChange("profile") },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDF3E8))
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF1D1B20)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. PRAYER TIMES CARD (Wide Bento Card: col-span-6 row-span-2)
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E8D1)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "NEXT PRAYER",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF386B40),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${nextPrayer.first} · ${nextPrayer.second}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00210E)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "London, UK",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00210E).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Fajr 04:12 • Dhuhr 13:05",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00210E).copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom strip: Prayer summary horizontal
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    viewModel.prayerTimes.forEach { prayer ->
                        val isNext = prayer.name == nextPrayer.first
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNext) Color(0xFF386B40).copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = prayer.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Bold,
                                color = if (isNext) Color(0xFF386B40) else Color(0xFF00210E).copy(alpha = 0.5f)
                            )
                            Text(
                                text = prayer.timeString.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNext) Color(0xFF386B40) else Color(0xFF00210E)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. MAIN BENTO GRID AREA (Two Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // LEFT COLUMN
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // A. SCANNER CARD (Primary action Bento Tile)
                Card(
                    onClick = { onTabChange("scan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .testTag("bento_scan_tile"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF386B40)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Scan Product",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Check additives & ingredients instantly",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                // B. SAVED PLACES TILE
                val savedCount = favoritesList.size
                Card(
                    onClick = { onTabChange("restaurants") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("bento_favorites_tile"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp),
                    border = borderStrokeCompact()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color(0xFF386B40),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF386B40))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "FAVORITES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF386B40),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (savedCount > 0) "$savedCount Saved Places" else "No Saved Places",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1D1B20).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // RIGHT COLUMN
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // C. NEARBY RESTAURANTS TILE (Square)
                val firstRest = restaurants.firstOrNull() ?: Restaurant(
                    id = "demo_burger_lab",
                    name = "The Burger Lab",
                    cuisine = "Burgers & Grill",
                    address = "Commercial Rd, London",
                    certification = "HMC Certified",
                    rating = 4.9,
                    distance = "2.4km"
                )

                Card(
                    onClick = { onTabChange("restaurants") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .testTag("bento_restaurant_tile"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp),
                    border = borderStrokeCompact()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFF386B40),
                                modifier = Modifier.size(20.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = firstRest.distance,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column {
                            Text(
                                text = firstRest.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val certLabel = firstRest.certification.substringBefore(" ")
                            Text(
                                text = "$certLabel · ★${firstRest.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // D. COMMUNITY ALERTS (Small Warning Ribbon)
                Card(
                    onClick = { onTabChange("products") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .testTag("bento_alert_tile"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE7E7)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFCDD2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Report,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Review Alert",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF780000)
                            )
                            Text(
                                text = "E120 found in 'Strawberry Bliss'",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC62828),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // E. PROFILE SETTINGS / NOTIFICATIONS TILE
                Card(
                    onClick = { onTabChange("profile") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("bento_profile_tile"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF3E8)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF386B40)),
                                contentAlignment = Alignment.Center
                            ) {
                                val initial = userProfile.name.take(2).uppercase()
                                Text(
                                    text = if (initial.isNotEmpty()) initial else "HE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userProfile.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Pro Member",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Bottom configuration mini tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(10.dp))
                                    Text("Alerts On", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, maxLines = 1)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(10.dp))
                                    Text("Emerald UI", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // F. COMMUNITY STATS (Tip container)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = borderStrokeCompact()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PrivacyTip,
                    contentDescription = "Halal Tips",
                    tint = Color(0xFF386B40),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Check of Halal Certification is updated dynamically by verified Muslim associations locally. Support community diners near you!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1D1B20).copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun borderStrokeCompact() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5D8))


// -------------------------------------------------------------
// CORE SCREEN: INGREDIENTS SCANNER & ANALYZER (AI POWERED)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: HalalViewModel, isTablet: Boolean) {
    var productName by remember { mutableStateOf("") }
    var ingredientsInput by remember { mutableStateOf("") }
    val scanUiState by viewModel.scanUiState.collectAsStateWithLifecycle()

    if (isTablet) {
        // Tablet Layout: Split horizontal panel (Inputs on left, AI Analysis on right)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Scan Ingredients AI",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Paste lists of ingredients, E-number formulas or scan label text.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Product Name (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        placeholder = { Text("e.g. Biscuit, Fruit Jelly") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("product_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ingredients Text / E-Numbers",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = ingredientsInput,
                        onValueChange = { ingredientsInput = it },
                        placeholder = { Text("Paste ingredients text here, or additives like E471, Gelatin, Carmine...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(vertical = 4.dp)
                            .testTag("ingredients_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Laser scanner effect mock action
                    ScannerCanvasMock()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Demo clickers
                    Text(
                        text = "Try Samples",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InputChip(
                            selected = false,
                            onClick = {
                                productName = "Gummy Bears"
                                ingredientsInput = "Pork Gelatin, Citric Acid, E120 Carmine, Sugar, Water"
                            },
                            label = { Text("🍬 Gelatin Bears") }
                        )
                        InputChip(
                            selected = false,
                            onClick = {
                                productName = "Spicy Chips"
                                ingredientsInput = "Potatoes, Vegetable Palm Oil, Paprika, E621 Msg, Salt"
                            },
                            label = { Text("🥔 Plant Chips") }
                        )
                        InputChip(
                            selected = false,
                            onClick = {
                                productName = "Sponge Cake"
                                ingredientsInput = "Flour, Sugar, Glycerol (E422), Emulsifiers (E471), Whole Whey"
                            },
                            label = { Text("🎂 Sponge Cake") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                productName = ""
                                ingredientsInput = ""
                                viewModel.clearScan()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                        Button(
                            onClick = { viewModel.performScan(productName, ingredientsInput) },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("scan_button")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = "Run Scan")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze Scan")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Analysis Panel on Right side of tablet
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    RenderScanResults(scanUiState, viewModel)
                }
            }
        }
    } else {
        // Phone Layout (Linear scroll view)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Halal Scanner AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Scan label ingredients in real-time or look up E-factors.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Label Title (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_product_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ingredientsInput,
                        onValueChange = { ingredientsInput = it },
                        label = { Text("List of Food Elements / Ingredients") },
                        placeholder = { Text("Pork gelatin, Emulsifier E471, lecithin, Carmine E120, etc.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("phone_ingredients_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Quick Sample Clicks
                    Text(text = "Try Quick Presets", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InputChip(
                            selected = false,
                            onClick = {
                                productName = "Vanilla Gummy Bears"
                                ingredientsInput = "Gelatin E441, Sugar, E120 Color, Artifical Flavors"
                            },
                            label = { Text("🍬 Gelatin") }
                        )
                        InputChip(
                            selected = false,
                            onClick = {
                                productName = "Crunchy Potato Chips"
                                ingredientsInput = "Sliced Potatoes, Palm Oil, Food Seasonings, Salt"
                            },
                            label = { Text("🥔 Veggie Chips") }
                        )
                        InputChip(
                            selected = false,
                            onClick = {
                                productName = "Sponge Cookies"
                                ingredientsInput = "Flour, Caramel, E471 Emulsifier, Whey Powder"
                            },
                            label = { Text("🍪 E471 Emulsifiers") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        productName = ""
                        ingredientsInput = ""
                        viewModel.clearScan()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { viewModel.performScan(productName, ingredientsInput) },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("phone_scan_button")
                ) {
                    Icon(Icons.Default.Verified, contentDescription = "Run analyze scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Scan")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            RenderScanResults(scanUiState, viewModel)
        }
    }
}

@Composable
fun ScannerCanvasMock() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val padX = 40f
                val strokeW = 4f

                // Corner bracket simulation
                // Top-Left corner
                drawLine(Color.Gray, Offset(padX, padX), Offset(padX + 30f, padX), strokeWidth = strokeW)
                drawLine(Color.Gray, Offset(padX, padX), Offset(padX, padX + 30f), strokeWidth = strokeW)

                // Top-Right corner
                drawLine(Color.Gray, Offset(canvasWidth - padX, padX), Offset(canvasWidth - padX - 30f, padX), strokeWidth = strokeW)
                drawLine(Color.Gray, Offset(canvasWidth - padX, padX), Offset(canvasWidth - padX, padX + 30f), strokeWidth = strokeW)

                // Bottom-Left corner
                drawLine(Color.Gray, Offset(padX, canvasHeight - padX), Offset(padX + 30f, canvasHeight - padX), strokeWidth = strokeW)
                drawLine(Color.Gray, Offset(padX, canvasHeight - padX), Offset(padX, canvasHeight - padX - 30f), strokeWidth = strokeW)

                // Bottom-Right corner
                drawLine(Color.Gray, Offset(canvasWidth - padX, canvasHeight - padX), Offset(canvasWidth - padX - 30f, canvasHeight - padX), strokeWidth = strokeW)
                drawLine(Color.Gray, Offset(canvasWidth - padX, canvasHeight - padX), Offset(canvasWidth - padX, canvasHeight - padX - 30f), strokeWidth = strokeW)

                // Laser line (Green)
                drawLine(
                    color = Color(0xFF00C853),
                    start = Offset(50f, canvasHeight / 2),
                    end = Offset(canvasWidth - 50f, canvasHeight / 2),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            Text(
                "CAMERA AI BARCODE OVERLAY ACTIVE",
                color = Color.DarkGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RenderScanResults(state: ScanUiState, viewModel: HalalViewModel) {
    when (state) {
        is ScanUiState.Idle -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.DocumentScanner,
                    contentDescription = "Analysis scanner ready",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Scanner Active & Ready",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Input food ingredients above and hit analyze to evaluate Halal status.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
        is ScanUiState.Loading -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Analyzing food chemical matrices...", style = MaterialTheme.typography.bodyMedium)
                Text("Consulting Halal AI intelligence model...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        is ScanUiState.Success -> {
            val result = state.result
            val statusColor = when (result.overallStatus) {
                "HALAL" -> Color(0xFF2E7D32)
                "HARAM" -> Color(0xFFC62828)
                else -> Color(0xFFEF6C00)
            }
            val statusEmoji = when (result.overallStatus) {
                "HALAL" -> "✅"
                "HARAM" -> "❌"
                else -> "⚠️"
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = statusEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = result.productName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SCAN CLASSIFICATION: ${result.overallStatus}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Matching Confidence: ${result.confidence}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ingredients Breakdown List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                result.items.forEach { item ->
                    val itemCol = when (item.status) {
                        "HALAL" -> Color(0xFF2E7D32)
                        "HARAM" -> Color(0xFFC62828)
                        else -> Color(0xFFEF6C00)
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = itemCol, contentColor = Color.White) {
                                    Text(item.status, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        is ScanUiState.Error -> {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = "Scan error", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = state.message, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CERTIFIED DINERS / RESTAURANTS LOCATOR (WITH MOCK GEOMETRIC MAP)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantsScreen(viewModel: HalalViewModel, isTablet: Boolean) {
    val restaurants by viewModel.restaurantsList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.restaurantSearchQuery.collectAsStateWithLifecycle()
    val activeCuisine by viewModel.cuisineFilter.collectAsStateWithLifecycle()
    val activeCert by viewModel.certFilter.collectAsStateWithLifecycle()

    var activeRestaurantForReviews by remember { mutableStateOf<Restaurant?>(null) }

    if (activeRestaurantForReviews != null) {
        // Overlay/Panel back stack layout for reviews submission
        RestaurantReviewsDialog(
            restaurant = activeRestaurantForReviews!!,
            onDismiss = { activeRestaurantForReviews = null },
            viewModel = viewModel
        )
    }

    if (isTablet) {
        // Tablet dynamic: Left lists of restaurants with filter chips, Right side Mock Interactive GPS Map
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Certified Diners Hub",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Nearby certified eateries and verified establishments.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setRestaurantSearch(it) },
                    placeholder = { Text("Search by name or street...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable helper filters row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Pakistani & Indian", "Turkish Genuine", "Arabian & Levantine", "Gourmet Burgers", "Vegan & Seafood Custom").forEach { cuisine ->
                        val isMatched = activeCuisine == "All" || cuisine.contains(activeCuisine) || activeCuisine.contains(cuisine)
                        FilterChip(
                            selected = activeCuisine == cuisine || (cuisine == "All" && activeCuisine == "All"),
                            onClick = { viewModel.setCuisineFilter(cuisine) },
                            label = { Text(cuisine) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(restaurants) { rest ->
                        RestaurantCard(
                            restaurant = rest,
                            onToggleFav = { viewModel.toggleFavoriteRestaurant(rest) },
                            onWriteReview = { activeRestaurantForReviews = rest }
                        )
                    }
                    if (restaurants.isEmpty()) {
                        item {
                            EmptyStateComponent(text = "No dinners matching active criteria.")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right column: GPS Map Visual Grid Vector
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9))) {
                        val w = size.width
                        val h = size.height

                        // Grid roads lines
                        drawLine(Color.White, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 30f)
                        drawLine(Color.White, Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 30f)
                        drawLine(Color.White, Offset(w * 0.4f, 0f), Offset(w * 0.4f, h), strokeWidth = 30f)
                        drawLine(Color.White, Offset(w * 0.8f, 0f), Offset(w * 0.8f, h), strokeWidth = 30f)

                        // User position pulse (Cosmic green)
                        drawCircle(Color(0xFF00E676), radius = 24f, center = Offset(w * 0.4f, h * 0.5f))
                        drawCircle(Color(0xFF00E676).copy(alpha = 0.3f), radius = 42f, center = Offset(w * 0.4f, h * 0.5f), style = Stroke(width = 4f))

                        // Pins for restaurants
                        drawCircle(Color(0xFFC62828), radius = 12f, center = Offset(w * 0.2f, h * 0.25f)) // Rest 1
                        drawCircle(Color(0xFFC62828), radius = 12f, center = Offset(w * 0.65f, h * 0.35f)) // Rest 2
                        drawCircle(Color(0xFFC62828), radius = 12f, center = Offset(w * 0.45f, h * 0.8f)) // Rest 3
                    }

                    // Floating GPS metadata on map
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("GPS MOCK ACTIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Accuracy: 3 meters", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Little overlay listing pins descriptions
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .fillMaxWidth(0.9f),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 4.dp
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Tracking current position", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Map includes certified Turkish kebab grills, luxury Middle-Eastern bistros and vegetarian choices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Phone view Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Certified Diners",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Find verified Muslim restaurants and halal diners near you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setRestaurantSearch(it) },
                label = { Text("Find grills, burgers, dim sum, etc.") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Query searches") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Scrollable Assist Filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pakistani & Indian", "Turkish Authentic", "Arabian & Levantine", "Gourmet Burgers", "Vegan & Seafood Custom").forEach { cuisine ->
                    FilterChip(
                        selected = activeCuisine == cuisine || (cuisine == "All" && activeCuisine == "All"),
                        onClick = { viewModel.setCuisineFilter(cuisine) },
                        label = { Text(cuisine) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(restaurants) { rest ->
                    RestaurantCard(
                        restaurant = rest,
                        onToggleFav = { viewModel.toggleFavoriteRestaurant(rest) },
                        onWriteReview = { activeRestaurantForReviews = rest }
                    )
                }
                if (restaurants.isEmpty()) {
                    item {
                        EmptyStateComponent(text = "No matching certified restaurants found inside zone.")
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onToggleFav: () -> Unit,
    onWriteReview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Halal logo",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = restaurant.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onToggleFav) {
                    Icon(
                        imageVector = if (restaurant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Pin to favorites and saved locations",
                        tint = if (restaurant.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = restaurant.certification,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(Icons.Default.Star, contentDescription = "rating reviews", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${restaurant.rating} (${restaurant.reviewsCount} reviews)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, contentDescription = "Distance marker", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(restaurant.distance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                TextButton(
                    onClick = onWriteReview,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Comment, contentDescription = "Write a community review", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Write Review")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMMUNITY PRODUCT REVIEWS CATALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsCatalogScreen(viewModel: HalalViewModel, isTablet: Boolean) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val reviews by viewModel.selectedProductReviews.collectAsStateWithLifecycle()

    var ratingInput by remember { mutableStateOf(5) }
    var reviewTextInput by remember { mutableStateOf("") }

    if (isTablet) {
        // Wide Screen Tablet Detail split screen
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // LEFT COLUMN: Product Item search and table catalog grid list
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Halal Products Space",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Verify barcode ratings and community comments.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setProductSearch(it) },
                    placeholder = { Text("Search by name, category or E-number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar catalog") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products) { prod ->
                        val isSelected = selectedProduct?.id == prod.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectProduct(prod) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (prod.status) {
                                                "HALAL" -> Color(0xFFE8F5E9)
                                                "HARAM" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF3E0)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(prod.status) {
                                            "HALAL" -> "✅"
                                            "HARAM" -> "❌"
                                            else -> "⚠️"
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(prod.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // RIGHT COLUMN: Active reviews and detail
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                if (selectedProduct != null) {
                    val prod = selectedProduct!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(prod.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Category: ${prod.category} • Barcode: ${prod.barcode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ingredients block
                        Text("Product Ingredients:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(prod.ingredients, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(12.dp))

                        // Input review text area
                        Text("Write Community Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { stars ->
                                Icon(
                                    imageVector = if (stars <= ratingInput) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Count ratings star",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { ratingInput = stars },
                                    tint = Color(0xFFFFB300)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reviewTextInput,
                            onValueChange = { reviewTextInput = it },
                            placeholder = { Text("Sharing community tips, warning labels or direct scans verified ingredients...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (reviewTextInput.isNotBlank()) {
                                    viewModel.submitProductReview(prod.id, prod.name, ratingInput, reviewTextInput)
                                    reviewTextInput = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Post Review")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("User Reviews List (${reviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        reviews.forEach { r ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                            Text(getAvatarEmoji(r.userAvatar), fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(r.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Row {
                                            (1..r.rating).forEach { _ ->
                                                Icon(Icons.Default.Star, contentDescription = "Active review star", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(r.reviewText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a product catalog listing to inspect ingredients and reviews.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    } else {
        // Phone Layout (Linear screen stack)
        if (selectedProduct == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Halal Products",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Verify ingredients review ratings of popular products.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setProductSearch(it) },
                    placeholder = { Text("Type item or barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Query searches") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products) { prod ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectProduct(prod) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (prod.status) {
                                                "HALAL" -> Color(0xFFE8F5E9)
                                                "HARAM" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF3E0)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(prod.status) {
                                            "HALAL" -> "✅"
                                            "HARAM" -> "❌"
                                            else -> "⚠️"
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(prod.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val prod = selectedProduct!!
            // Selected Product Detail mode page
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectProduct(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return back to list")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Product Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(prod.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Category: ${prod.category} • Barcode: ${prod.barcode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Status Badge card info
                val statusBg = when (prod.status) {
                    "HALAL" -> Color(0xFFE8F5E9)
                    "HARAM" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFFFF3E0)
                }
                val statusTextCol = when (prod.status) {
                    "HALAL" -> Color(0xFF2E7D32)
                    "HARAM" -> Color(0xFFC62828)
                    else -> Color(0xFFEF6C00)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "STATUS: ${prod.status}",
                            fontWeight = FontWeight.Bold,
                            color = statusTextCol,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ingredients
                Text("Formula Ingredients List:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(prod.ingredients, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Composing Product reviews
                Text("Write Product Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    (1..5).forEach { stars ->
                        Icon(
                            imageVector = if (stars <= ratingInput) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Submit stars index",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { ratingInput = stars },
                            tint = Color(0xFFFFB300)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewTextInput,
                    onValueChange = { reviewTextInput = it },
                    label = { Text("Comment feedback...") },
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (reviewTextInput.isNotBlank()) {
                            viewModel.submitProductReview(prod.id, prod.name, ratingInput, reviewTextInput)
                            reviewTextInput = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Post Review")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Reviews (${reviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                reviews.forEach { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                    Text(getAvatarEmoji(r.userAvatar), fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Row {
                                    (1..r.rating).forEach { _ ->
                                        Icon(Icons.Default.Star, contentDescription = "stars active rating", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(r.reviewText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// USER PROFILE & SYSTEM SETTINGS SCREEN with Theme configuration,
// saving notifications and saved location highlights.
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(viewModel: HalalViewModel, isTablet: Boolean) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val favorites by viewModel.favoritesList.collectAsStateWithLifecycle()

    var editingName by remember(profile.name) { mutableStateOf(profile.name) }
    var isEditingMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Personal Settings profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Customize naming, themes, notifications and saved favorites.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Center Profile Card avatar with edit choices
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive avatar emoji container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getAvatarEmoji(profile.avatarName),
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar slider switcher row
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Default", "Avatar 1", "Avatar 2", "Avatar 3", "Avatar 4", "Avatar 5").forEach { avatarName ->
                        val active = profile.avatarName == avatarName
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.updateProfileAvatar(avatarName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getAvatarEmoji(avatarName), fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditingMode) {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_edit_field"),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                viewModel.updateProfileName(editingName)
                                isEditingMode = false
                            }) {
                                Icon(Icons.Default.Done, contentDescription = "Save edit name profile")
                            }
                        }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { isEditingMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit name settings", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CUSTOM ACCENT THEME COLOR PICKER
        Text(
            text = "Islamic custom accent color Theme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select a custom color to personalize the interface experience.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            viewModel.accentPalettes.forEachIndexed { idx, palette ->
                val selected = idx == profile.accentColorIndex
                Column(
                    modifier = Modifier.clickable { viewModel.updateThemeIndex(idx) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(palette.primary))
                            .drawBehind {
                                if (selected) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = 28f,
                                        style = Stroke(width = 4f)
                                    )
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = palette.name.substringBefore(" "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NOTIFICATION SETTINGS PREFERENCES
        Text(
            text = "Alerts & Notification Preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = "Athan alerts notifications", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Athan Prayer times Notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Receive silent notification sound when prayer times trigger.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = profile.prayerNotificationsEnabled,
                        onCheckedChange = { viewModel.togglePrayerNotification(it) },
                        modifier = Modifier.testTag("prayer_notifications_switch")
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Campaign updates info announcements", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Halal Alerts & Warnings", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Receive community warnings about suspicious additives.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = profile.generalNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleGeneralNotification(it) },
                        modifier = Modifier.testTag("general_notifications_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SAVED FAVORITE LOCATIONS list
        Text(
            text = "Saved certified locations (${favorites.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your bookmarks of restaurants and dinners verified Halal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (favorites.isEmpty()) {
                    Text(
                        "No saved diners. Head over to Diners tab and save favorite locations.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    favorites.forEach { f ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = "eatery icons bookmark", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(f.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("${f.cuisine} • ${f.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                viewModel.toggleFavoriteRestaurant(
                                    Restaurant(f.id, f.name, f.cuisine, f.address, f.certification, f.rating, f.distance)
                                )
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "remove from bookmark saved location", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// -------------------------------------------------------------
// HELPER COMPOSABLES AND UTILITIES
// -------------------------------------------------------------
fun getAvatarEmoji(name: String): String {
    return when (name) {
        "Avatar 1" -> "🧑‍⚕️"
        "Avatar 2" -> "🧕"
        "Avatar 3" -> "👳"
        "Avatar 4" -> "👨"
        "Avatar 5" -> "🙋"
        else -> "🕌"
    }
}

@Composable
fun EmptyStateComponent(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.ManageSearch, contentDescription = "No items matched search inputs", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// Reviews popup drawer dialog mapping inputs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantReviewsDialog(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    viewModel: HalalViewModel
) {
    var textInput by remember { mutableStateOf("") }
    var ratingChosen by remember { mutableStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Review ${restaurant.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Submit certification feedback, service quality or Halal status alerts to guide nearby community seekers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { stars ->
                        Icon(
                            imageVector = if (stars <= ratingChosen) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "stars submissions",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { ratingChosen = stars },
                            tint = Color(0xFFFFB300)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("E.g. Clean certified meats, amazing Turkish authentic spices and very courteous staff!") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        // Submit review mock action
                        viewModel.submitProductReview(
                            productId = restaurant.id,
                            productName = restaurant.name,
                            rating = ratingChosen,
                            reviewText = textInput
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
