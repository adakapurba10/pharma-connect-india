package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Medicine
import com.example.data.Pharmacy
import com.example.data.SyncLog
import com.example.data.PaymentProof
import com.example.data.Order
import com.example.ui.theme.MedicalAmberAccent
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.theme.MedicalTealSecondary
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedInactive
import com.example.ui.theme.BlueInfo
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.launch

@Composable
fun MainPharmaScreen(viewModel: PharmaViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val activePharmacy by viewModel.activePharmacy.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                NavigationBarItem(
                    selected = currentRole == PharmaViewModel.AppRole.CUSTOMER,
                    onClick = { viewModel.setRole(PharmaViewModel.AppRole.CUSTOMER) },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Customer") },
                    label = { Text("Customer") },
                    modifier = Modifier.testTag("nav_customer")
                )
                NavigationBarItem(
                    selected = currentRole == PharmaViewModel.AppRole.PHARMACY,
                    onClick = { viewModel.setRole(PharmaViewModel.AppRole.PHARMACY) },
                    icon = { Icon(Icons.Filled.Storefront, contentDescription = "Pharmacy") },
                    label = { Text("Pharmacy") },
                    modifier = Modifier.testTag("nav_pharmacy")
                )
                NavigationBarItem(
                    selected = currentRole == PharmaViewModel.AppRole.ADMIN,
                    onClick = { viewModel.setRole(PharmaViewModel.AppRole.ADMIN) },
                    icon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Super Admin") },
                    label = { Text("Super Admin") },
                    modifier = Modifier.testTag("nav_admin")
                )
            }
        }
    ) { innerPadding ->
        val headsUpNotification by viewModel.activeHeadsUpNotification.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                PharmaViewModel.AppRole.CUSTOMER -> CustomerDashboard(viewModel)
                PharmaViewModel.AppRole.PHARMACY -> PharmacyDashboard(viewModel)
                PharmaViewModel.AppRole.ADMIN -> SuperAdminDashboard(viewModel)
            }

            // Real-time floating Notification overlay
            headsUpNotification?.let { notification ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable { viewModel.dismissHeadsUp() },
                        colors = CardDefaults.cardColors(
                            containerColor = when (notification.type) {
                                "Firebase Cloud Messaging" -> Color(0xFFFFFDE7) // soft yellow
                                "SMS" -> Color(0xFFE3F2FD) // soft blue
                                "WhatsApp" -> Color(0xFFE8F5E9) // soft green
                                "Email" -> Color(0xFFF3E5F5) // soft purple
                                else -> Color.White
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(
                            1.dp,
                            when (notification.type) {
                                "Firebase Cloud Messaging" -> Color(0xFFFBC02D)
                                "SMS" -> Color(0xFF1E88E5)
                                "WhatsApp" -> Color(0xFF43A047)
                                "Email" -> Color(0xFF8E24AA)
                                else -> Color.LightGray
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = when (notification.type) {
                                            "Firebase Cloud Messaging" -> Color(0xFFFBC02D).copy(alpha = 0.15f)
                                            "SMS" -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                                            "WhatsApp" -> Color(0xFF43A047).copy(alpha = 0.15f)
                                            "Email" -> Color(0xFF8E24AA).copy(alpha = 0.15f)
                                            else -> Color.Gray.copy(alpha = 0.15f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (notification.type) {
                                        "Firebase Cloud Messaging" -> Icons.Filled.NotificationsActive
                                        "SMS" -> Icons.Filled.Sms
                                        "WhatsApp" -> Icons.Filled.QuestionAnswer
                                        "Email" -> Icons.Filled.Email
                                        else -> Icons.Filled.Info
                                    },
                                    contentDescription = null,
                                    tint = when (notification.type) {
                                        "Firebase Cloud Messaging" -> Color(0xFFE65100)
                                        "SMS" -> Color(0xFF1565C0)
                                        "WhatsApp" -> Color(0xFF2E7D32)
                                        "Email" -> Color(0xFF6A1B9A)
                                        else -> Color.DarkGray
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = notification.sender,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    )
                                    Text(
                                        text = "Now",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                                Text(
                                    text = notification.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = notification.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.dismissHeadsUp() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CUSTOMER DASHBOARD ----------------

@Composable
fun CustomerDashboard(viewModel: PharmaViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedPharmacy by viewModel.selectedPharmacy.collectAsState()
    val selectedMedicine by viewModel.selectedMedicine.collectAsState()
    val pharmacies by viewModel.pharmacies.collectAsState()
    val cartItem by viewModel.cartItem.collectAsState()

    var showPrescriptionTab by remember { mutableStateOf(false) }
    var activeSmartSort by remember { mutableStateOf("Lowest Price") }

    Column(modifier = Modifier.fillMaxSize()) {
        // App header with beautiful clinical layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MedicalTealSecondary, MedicalTealPrimary)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocalPharmacy,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pharma Connect",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                    Text(
                        text = "Smart Pharmacy Network System",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }

                Row {
                    IconButton(
                        onClick = { showPrescriptionTab = !showPrescriptionTab },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (showPrescriptionTab) Icons.Filled.Search else Icons.Filled.Description,
                            contentDescription = "Upload Prescription",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        if (showPrescriptionTab) {
            PrescriptionOcrScreen(viewModel) {
                showPrescriptionTab = false
            }
        } else {
            // Main Search & Locate Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Large Search Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = "Smart Search",
                                tint = MedicalTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Smart Search Medicine Instantly",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedicalTealSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Locate optimal local prices, stock levels, rating, and distance.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("e.g. Crocin, Paracetamol 650, Atorvastatin...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_medicine_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MedicalTealPrimary,
                                cursorColor = MedicalTealPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Smart Filters Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Tune,
                                contentDescription = null,
                                tint = MedicalTealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sort & Filter Criteria",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTealSecondary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Horizontally scrollable row of modern filter chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf(
                                Triple("Nearby", Icons.Filled.DirectionsWalk, "Distance"),
                                Triple("Lowest Price", Icons.Filled.LocalPharmacy, "Cheapest price"),
                                Triple("Highest Stock", Icons.Filled.Inventory, "Most in stock"),
                                Triple("Fastest Delivery", Icons.Filled.LocalShipping, "Quick delivery"),
                                Triple("Open Shop", Icons.Filled.AccessTime, "Only open shops"),
                                Triple("Customer Rating", Icons.Filled.Star, "Top rated")
                            )

                            items(options) { (name, icon, desc) ->
                                val isSelected = activeSmartSort == name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { activeSmartSort = name },
                                    label = {
                                        Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isSelected) Color.White else MedicalTealPrimary
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MedicalTealPrimary,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        labelColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }

                        // Trending Searches / Crocin suggestion when query is empty
                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Trending Smart Searches",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Crocin", "Paracetamol 650", "Amoxicillin").forEach { medicineName ->
                                    Box(
                                        modifier = Modifier
                                            .background(MedicalTealPrimary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setSearchQuery(medicineName) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.TrendingUp,
                                                contentDescription = null,
                                                tint = MedicalTealPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = medicineName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MedicalTealSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MedicalTealPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (activeSmartSort) {
                                        "Nearby" -> "Smart Search: Sorting by nearest distance (walk-in meters)"
                                        "Lowest Price" -> "Smart Search: Sorting by cheapest medicine selling price"
                                        "Highest Stock" -> "Smart Search: Sorting by maximum inventory count"
                                        "Fastest Delivery" -> "Smart Search: Sorting by estimated delivery speed & eligibility"
                                        "Open Shop" -> "Smart Search: Prioritizing currently open shops first"
                                        "Customer Rating" -> "Smart Search: Sorting by highest customer rating (out of 5 stars)"
                                        else -> "Smart Search: Active filter applied"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MedicalTealPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // GPS & Live Map Integration Visualizer
                InteractiveMapSimulation(
                    viewModel = viewModel,
                    pharmacies = pharmacies,
                    selectedPharmacy = selectedPharmacy,
                    onPharmacySelect = { viewModel.selectPharmacy(it) }
                )

                // Results list or general list
                if (searchQuery.isNotEmpty()) {
                    val sortedResults = remember(searchResults, activeSmartSort, pharmacies) {
                        when (activeSmartSort) {
                            "Nearby" -> {
                                searchResults.sortedBy { medicine ->
                                    val pharmacy = pharmacies.find { it.id == medicine.pharmacyId }
                                    pharmacy?.distanceMeters ?: Int.MAX_VALUE
                                }
                            }
                            "Lowest Price" -> {
                                searchResults.sortedBy { it.sellingPrice }
                            }
                            "Highest Stock" -> {
                                searchResults.sortedByDescending { it.stockCount }
                            }
                            "Fastest Delivery" -> {
                                // Prioritize home delivery, then sort by distance
                                searchResults.sortedWith(compareBy(
                                    { !(pharmacies.find { p -> p.id == it.pharmacyId }?.hasHomeDelivery ?: false) },
                                    { pharmacies.find { p -> p.id == it.pharmacyId }?.distanceMeters ?: Int.MAX_VALUE }
                                ))
                            }
                            "Open Shop" -> {
                                // Prioritize open shops, then sort by price
                                searchResults.sortedWith(compareBy(
                                    { !(pharmacies.find { p -> p.id == it.pharmacyId }?.isOpen ?: false) },
                                    { it.sellingPrice }
                                ))
                            }
                            "Customer Rating" -> {
                                searchResults.sortedByDescending { medicine ->
                                    val pharmacy = pharmacies.find { it.id == medicine.pharmacyId }
                                    pharmacy?.rating ?: 0f
                                }
                            }
                            else -> searchResults.sortedBy { it.sellingPrice }
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Matching Medicine Offers (${sortedResults.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        if (sortedResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Outlined.SentimentDissatisfied,
                                        contentDescription = "Not found",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No medicines found for \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                                    )
                                }
                            }
                        } else {
                            sortedResults.forEach { medicine ->
                                MedicineOfferCard(
                                    medicine = medicine,
                                    pharmacies = pharmacies,
                                    onSelect = {
                                        viewModel.selectMedicine(medicine)
                                        // Auto-select pharmacy on map too
                                        val pharma = pharmacies.find { it.id == medicine.pharmacyId }
                                        viewModel.selectPharmacy(pharma)
                                    },
                                    onOrderClick = {
                                        viewModel.addToCart(medicine)
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    // Nearby Pharmacies General Overview
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Nearby Registered Pharmacies (${pharmacies.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        pharmacies.forEach { pharmacy ->
                            PharmacyOverviewCard(
                                pharmacy = pharmacy,
                                isSelected = selectedPharmacy?.id == pharmacy.id,
                                onClick = { viewModel.selectPharmacy(pharmacy) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // Medicine Detail Sheet Modal
    selectedMedicine?.let { med ->
        val pharma = pharmacies.find { it.id == med.pharmacyId }
        MedicineDetailsModal(
            medicine = med,
            pharmacy = pharma,
            viewModel = viewModel,
            onDismiss = { viewModel.selectMedicine(null) }
        )
    }

    // Order Checkout Dialog
    cartItem?.let { item ->
        OrderCheckoutDialog(
            medicine = item,
            viewModel = viewModel,
            onDismiss = { viewModel.addToCart(item) } // handled by viewmodel trigger
        )
    }
}

@Composable
fun InteractiveMapSimulation(
    viewModel: PharmaViewModel,
    pharmacies: List<Pharmacy>,
    selectedPharmacy: Pharmacy?,
    onPharmacySelect: (Pharmacy) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val activeMapProvider by viewModel.activeMapProvider.collectAsState()
    val isTrafficLayerEnabled by viewModel.isTrafficLayerEnabled.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (activeMapProvider) {
                "Google Maps" -> Color(0xFFE8F0FE)
                "OpenStreetMap" -> Color(0xFFF1EEE9)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = BorderStroke(1.dp, MedicalTealPrimary.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Drawn Grid map
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(pharmacies) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            pharmacies.forEach { pharmacy ->
                                val x = centerX + (pharmacy.longitude - 88.3639).toFloat() * 12000f
                                val y = centerY - (pharmacy.latitude - 22.5726).toFloat() * 12000f
                                val dx = offset.x - x
                                val dy = offset.y - y
                                if (dx * dx + dy * dy < 1200f) {
                                    onPharmacySelect(pharmacy)
                                }
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val centerX = width / 2
                val centerY = height / 2

                // Draw special styling backgrounds
                if (activeMapProvider == "OpenStreetMap") {
                    drawRect(
                        color = Color(0xFFE1EFE0),
                        topLeft = Offset(centerX - 180f, centerY - 120f),
                        size = androidx.compose.ui.geometry.Size(120f, 100f)
                    )
                    drawRect(
                        color = Color(0xFFE1EFE0),
                        topLeft = Offset(centerX + 150f, centerY + 80f),
                        size = androidx.compose.ui.geometry.Size(140f, 90f)
                    )
                }

                // Draw Grid lines
                val gridSpacing = 50f
                for (x in 0..(width / gridSpacing).toInt()) {
                    drawLine(
                        color = if (activeMapProvider == "Google Maps") Color(0x224285F4) else Color(0x1F8B5A2B),
                        start = Offset(x * gridSpacing, 0f),
                        end = Offset(x * gridSpacing, height),
                        strokeWidth = if (activeMapProvider == "Google Maps") 1f else 1.5f
                    )
                }
                for (y in 0..(height / gridSpacing).toInt()) {
                    drawLine(
                        color = if (activeMapProvider == "Google Maps") Color(0x224285F4) else Color(0x1F8B5A2B),
                        start = Offset(0f, y * gridSpacing),
                        end = Offset(width, y * gridSpacing),
                        strokeWidth = if (activeMapProvider == "Google Maps") 1f else 1.5f
                    )
                }

                // Draw Traffic Flow Layer (G-Map specific)
                if (activeMapProvider == "Google Maps" && isTrafficLayerEnabled) {
                    drawLine(
                        color = Color(0xFF34A853),
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color(0xFFFBBC05),
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, height),
                        strokeWidth = 4f
                    )
                }

                // Draw user location (center)
                drawCircle(
                    color = Color(0xFF4285F4).copy(alpha = 0.2f),
                    radius = 35f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color(0xFF4285F4),
                    radius = 8f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(centerX, centerY)
                )

                // Draw radar ring scanning
                drawCircle(
                    color = if (activeMapProvider == "Google Maps") Color(0xFF4285F4).copy(alpha = 0.08f) else Color(0xFF2E7D32).copy(alpha = 0.08f),
                    radius = (width / 3) * (pulseAnim / 18f),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )

                // Draw pharmacy positions
                pharmacies.forEach { pharmacy ->
                    val x = centerX + (pharmacy.longitude - 88.3639).toFloat() * 12000f
                    val y = centerY - (pharmacy.latitude - 22.5726).toFloat() * 12000f

                    val isSelected = selectedPharmacy?.id == pharmacy.id
                    val nodeColor = if (pharmacy.isOpen) {
                        if (isSelected) MedicalAmberAccent else if (activeMapProvider == "Google Maps") Color(0xFFEA4335) else Color(0xFFE65100)
                    } else {
                        Color.Gray
                    }

                    if (pharmacy.isOpen) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.3f),
                            radius = pulseAnim + 4f,
                            center = Offset(x, y)
                        )
                    }

                    if (isSelected) {
                        drawLine(
                            color = if (activeMapProvider == "Google Maps") Color(0xFF4285F4) else Color(0xFFE65100),
                            start = Offset(centerX, centerY),
                            end = Offset(x, y),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                    }

                    drawCircle(
                        color = nodeColor,
                        radius = if (isSelected) 10f else 7f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }

            // Top details and Map provider toggler
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF3B82F6), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeMapProvider,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Style: ", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Box(
                        modifier = Modifier
                            .background(
                                if (activeMapProvider == "Google Maps") Color(0xFF4285F4) else Color.DarkGray,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { viewModel.setMapProvider("Google Maps") }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("G-Map", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (activeMapProvider == "OpenStreetMap") Color(0xFF2E7D32) else Color.DarkGray,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { viewModel.setMapProvider("OpenStreetMap") }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("OSM", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Info panel overlay for active selection
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .fillMaxWidth(0.95f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedPharmacy != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Filled.Store,
                                contentDescription = null,
                                tint = MedicalTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = selectedPharmacy.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = MedicalAmberAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "${selectedPharmacy.rating} • ${selectedPharmacy.distanceMeters}m Away",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.triggerSimulatedNotification(
                                    type = viewModel.activeNotificationChannel.value,
                                    title = "Directions Sent",
                                    message = "Sleek maps routing details dispatched for ${selectedPharmacy.name} via ${activeMapProvider}!"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Filled.Navigation, contentDescription = "Nav", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navigate", fontSize = 11.sp)
                        }
                    } else {
                        Text(
                            text = "Tap any pharmacy node or search above to load distance",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineOfferCard(
    medicine: Medicine,
    pharmacies: List<Pharmacy>,
    onSelect: () -> Unit,
    onOrderClick: () -> Unit
) {
    val pharmacy = pharmacies.find { it.id == medicine.pharmacyId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedicalTealSecondary
                    )
                    Text(
                        text = "Salt: ${medicine.saltComposition}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Manufacturer: ${medicine.manufacturer}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price display badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${medicine.sellingPrice}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MedicalTealPrimary
                        )
                    )
                    if (medicine.discountPercent > 0) {
                        Text(
                            text = "₹${medicine.mrp} (Save ${medicine.discountPercent}%)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pharmacy details
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pharmacy?.name ?: "Unknown Pharmacy",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Gold Rating Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFFFF9C4), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFBC02D),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${pharmacy?.rating ?: 4.0f}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE65100)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.DirectionsWalk,
                            contentDescription = "Distance",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${pharmacy?.distanceMeters ?: 0}m • ",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Text(
                            text = if (pharmacy?.isOpen == true) "Open Now" else "Closed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (pharmacy?.isOpen == true) GreenActive else RedInactive,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hasDelivery = pharmacy?.hasHomeDelivery == true
                        Icon(
                            imageVector = if (hasDelivery) Icons.Filled.LocalShipping else Icons.Filled.Storefront,
                            contentDescription = "Delivery",
                            tint = if (hasDelivery) MedicalTealPrimary else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasDelivery) {
                                val eta = 15 + ((pharmacy?.distanceMeters ?: 0) / 100)
                                "Home Delivery: ~$eta mins"
                            } else {
                                "In-Store Pickup Only"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (hasDelivery) MedicalTealPrimary else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Stock details
                Box(
                    modifier = Modifier
                        .background(
                            if (medicine.stockCount > 20) GreenActive.copy(alpha = 0.1f)
                            else if (medicine.stockCount > 0) MedicalAmberAccent.copy(alpha = 0.1f)
                            else RedInactive.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (medicine.stockCount > 0) "Stock: ${medicine.stockCount}" else "Out of Stock",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (medicine.stockCount > 20) GreenActive
                            else if (medicine.stockCount > 0) MedicalAmberAccent
                            else RedInactive,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { /* Simulated Call */ },
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Call Pharmacy", tint = MedicalTealSecondary)
                }

                IconButton(
                    onClick = { /* Simulated WhatsApp */ },
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onOrderClick,
                    enabled = medicine.stockCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Order", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Order Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PharmacyOverviewCard(
    pharmacy: Pharmacy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MedicalTealPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MedicalTealPrimary else Color.Gray.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Pharmacy photo with fallback icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MedicalTealPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pharmacy.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pharmacy.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (pharmacy.subscriptionTier == "Premium" || pharmacy.subscriptionTier == "Enterprise") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = "Verified Sponsor",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = MedicalAmberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${pharmacy.rating} (${pharmacy.reviewsCount} reviews)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                Text(
                    text = "License: ${pharmacy.licenseNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (pharmacy.isOpen) "OPEN" else "CLOSED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (pharmacy.isOpen) GreenActive else RedInactive,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${pharmacy.distanceMeters}m Away",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// ---------------- MEDICINE DETAILS MODAL ----------------

@Composable
fun MedicineDetailsModal(
    medicine: Medicine,
    pharmacy: Pharmacy?,
    viewModel: PharmaViewModel,
    onDismiss: () -> Unit
) {
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MedicalTealPrimary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                medicine.name,
                fontWeight = FontWeight.ExtraBold,
                color = MedicalTealSecondary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MedicalTealPrimary.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Selling Price", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${medicine.sellingPrice}", fontWeight = FontWeight.ExtraBold, color = MedicalTealPrimary, fontSize = 16.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MRP (Max Retail Price)", fontSize = 12.sp, color = Color.Gray)
                            Text("₹${medicine.mrp}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount Value", fontSize = 12.sp, color = Color.Gray)
                            Text("${medicine.discountPercent}% Off", fontSize = 12.sp, color = GreenActive, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Product Specifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalTealSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                SpecificationRow("Active Salt Composition", medicine.saltComposition)
                SpecificationRow("Manufacturer", medicine.manufacturer)
                SpecificationRow("Expiry Date", medicine.expiryDate)
                SpecificationRow("Batch Code", medicine.batchNumber)
                SpecificationRow("Indian Alternatives", medicine.alternativesText)

                if (pharmacy != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Offered By", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedicalTealSecondary)
                    Text(pharmacy.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("GST: ${pharmacy.gstNumber}", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI suggestions button & display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = "AI icon",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Pharma Connect AI Helper",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Get an instant clinical overview of this salt, warnings, and alternative options.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E3A8A)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isAiLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing pharmaceutical databases...", fontSize = 12.sp)
                            }
                        } else if (aiResponse != null) {
                            Text(
                                text = aiResponse!!,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Button(
                                onClick = { viewModel.askAiAboutMedicine(medicine) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Ask Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(0.4f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
    }
}

// ---------------- ORDER CHECKOUT DIALOG ----------------

@Composable
fun OrderCheckoutDialog(
    medicine: Medicine,
    viewModel: PharmaViewModel,
    onDismiss: () -> Unit
) {
    val quantity by viewModel.cartQuantity.collectAsState()
    val success by viewModel.orderPlacedSuccessfully.collectAsState()
    val activeGateway by viewModel.activePaymentGateway.collectAsState()
    val razorpayKey by viewModel.razorpayApiKey.collectAsState()
    val phonePeMid by viewModel.phonePeMerchantId.collectAsState()
    val stripeKey by viewModel.stripePublishableKey.collectAsState()
    val upiVpa by viewModel.upiVpaId.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isHomeDelivery by remember { mutableStateOf(true) }

    var otpSent by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (success) {
                Button(
                    onClick = {
                        viewModel.addToCart(medicine) // dismisses checkout logic by setting null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary)
                ) {
                    Text("OK")
                }
            } else {
                Button(
                    onClick = {
                        if (!isVerified) {
                            if (!otpSent) {
                                otpSent = true
                            } else if (enteredOtp == "1234") {
                                isVerified = true
                            }
                        } else {
                            viewModel.checkoutOrder(name, phone, address, isHomeDelivery)
                        }
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank() && (!isHomeDelivery || address.isNotBlank()),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary)
                ) {
                    Text(
                        if (!otpSent) "Send Mobile OTP"
                        else if (!isVerified) "Verify & Login"
                        else "Place Order (₹${medicine.sellingPrice * quantity})"
                    )
                }
            }
        },
        dismissButton = {
            if (!success) {
                TextButton(onClick = { viewModel.addToCart(medicine) }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        },
        title = {
            Text(if (success) "Order Placed Successfully! 🎉" else "Online Medicine Order", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (success) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = GreenActive,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Your order for $quantity x ${medicine.name} has been placed. You can check the details on the Pharmacy dashboard.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Text(medicine.name, fontWeight = FontWeight.Bold, color = MedicalTealPrimary)
                    Text("Manufacturer: ${medicine.manufacturer}", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Select Quantity", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.decrementCartQuantity() }) {
                                Icon(Icons.Filled.Remove, contentDescription = "Sub")
                            }
                            Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { viewModel.incrementCartQuantity() }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Social Login Simulation Bar
                    if (!otpSent) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                name = "Google User"
                                phone = "+91 99000 11223"
                                isVerified = true
                                otpSent = true
                            }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Google Login", tint = Color(0xFFDB4437))
                            }
                            Text("or Sign In with Google / Apple to skip OTP", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }

                    if (otpSent && !isVerified) {
                        Text("OTP sent to $phone. Enter '1234' to verify:", color = MedicalTealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = { enteredOtp = it },
                            label = { Text("Enter OTP (1234)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = GreenActive, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mobile OTP Verified", color = GreenActive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isHomeDelivery, onCheckedChange = { isHomeDelivery = it })
                            Text("Home Delivery Request")
                        }

                        if (isHomeDelivery) {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Delivery Address") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Select Integrated Payment Gateway",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MedicalTealSecondary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Gateway Choice cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val gateways = listOf("Razorpay", "PhonePe", "UPI", "Stripe")
                            gateways.forEach { gateway ->
                                val isSelected = activeGateway == gateway
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setPaymentGateway(gateway) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MedicalTealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MedicalTealPrimary else Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = when (gateway) {
                                                "Razorpay" -> Icons.Filled.CreditCard
                                                "PhonePe" -> Icons.Filled.AccountBalanceWallet
                                                "UPI" -> Icons.Filled.QrCodeScanner
                                                else -> Icons.Filled.Payment
                                            },
                                            contentDescription = gateway,
                                            tint = if (isSelected) MedicalTealPrimary else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = gateway,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MedicalTealPrimary else Color.Gray
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom payment sheet style based on active gateway
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (activeGateway) {
                                    "Razorpay" -> Color(0xFFF1F3F9)
                                    "PhonePe" -> Color(0xFFF5EBFB)
                                    "UPI" -> Color(0xFFE8F5E9)
                                    else -> Color(0xFFFAFAFA)
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                when (activeGateway) {
                                    "Razorpay" -> Color(0xFF3F51B5)
                                    "PhonePe" -> Color(0xFF673AB7)
                                    "UPI" -> Color(0xFF2E7D32)
                                    else -> Color.LightGray
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = when (activeGateway) {
                                            "Razorpay" -> "Razorpay Secure SDK Active"
                                            "PhonePe" -> "PhonePe Transaction SDK"
                                            "UPI" -> "Instant UPI Gateway"
                                            else -> "Stripe Elements Terminal"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = when (activeGateway) {
                                            "Razorpay" -> Color(0xFF3F51B5)
                                            "PhonePe" -> Color(0xFF673AB7)
                                            "UPI" -> Color(0xFF2E7D32)
                                            else -> Color.DarkGray
                                        }
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Secure",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (activeGateway) {
                                        "Razorpay" -> "Live API Key: $razorpayKey"
                                        "PhonePe" -> "Merchant ID: $phonePeMid"
                                        "UPI" -> "Target UPI VPA: $upiVpa"
                                        else -> "Publishable Key: $stripeKey"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                when (activeGateway) {
                                    "Razorpay" -> {
                                        Text("Select Saved Card or Netbanking details. Fully verified via OTP flow.", fontSize = 11.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("HDFC Debit Card ending in **** 9912", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    "PhonePe" -> {
                                        Text("Redirect flow will launch PhonePe App/Web directly on verification.", fontSize = 11.sp)
                                        Text("PhonePe Wallet Balance: ₹2,500 available", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                                    }
                                    "UPI" -> {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Text("Scan QR or request deep link payment confirmation", fontSize = 11.sp, textAlign = TextAlign.Center)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            // Simulated QR code
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .background(Color.White, RoundedCornerShape(8.dp))
                                                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "QR Code", modifier = Modifier.size(60.dp), tint = Color.Black)
                                                    Text("₹${medicine.sellingPrice * quantity}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = "4242 •••• •••• 4242",
                                                onValueChange = {},
                                                enabled = false,
                                                label = { Text("Card Number") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedTextField(
                                                    value = "12/29",
                                                    onValueChange = {},
                                                    enabled = false,
                                                    label = { Text("Expiry") },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = "•••",
                                                    onValueChange = {},
                                                    enabled = false,
                                                    label = { Text("CVC") },
                                                    modifier = Modifier.weight(1f)
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
        }
    )
}

// ---------------- PRESCRIPTION OCR SCREEN ----------------

@Composable
fun PrescriptionOcrScreen(viewModel: PharmaViewModel, onClose: () -> Unit) {
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()

    var prescriptionText by remember { mutableStateOf("") }

    val samplePrescriptions = listOf(
        "Rx: Paracetamol 650mg TDS x 3 days, Cetirizine 10mg HS x 5 days",
        "Rx: Amoxicillin 500mg BD for 5 days, Pantoprazole 40mg OD before meal",
        "Rx: Metformin 500mg BD after lunch and dinner, Atorvastatin 10mg HS"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AI Prescription Scanner",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Text(
                "Upload photo or paste doctor's prescription instructions below. Gemini AI will parse the handwriting and identify local pharmacies with live stocks.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sample Selector Row
            Text("Sample Notes (Click to load):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                samplePrescriptions.forEachIndexed { index, sample ->
                    AssistChip(
                        onClick = { prescriptionText = sample },
                        label = { Text("Sample ${index + 1}", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prescriptionText,
                onValueChange = { prescriptionText = it },
                placeholder = { Text("Type or load doctor's prescription notes here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isAiLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini OCR parsing prescription...", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.uploadAndAnalyzePrescription(prescriptionText)
                    },
                    enabled = prescriptionText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Parse")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Parse Prescription", fontWeight = FontWeight.Bold)
                }
            }

            if (aiResponse != null && !isAiLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Gemini AI Analysis Result:", fontWeight = FontWeight.Bold, color = MedicalTealSecondary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(aiResponse!!, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}


// ---------------- PHARMACY DASHBOARD ----------------

@Composable
fun PharmacyDashboard(viewModel: PharmaViewModel) {
    val pharmacies by viewModel.pharmacies.collectAsState()
    val activePharmacyId by viewModel.activePharmacyId.collectAsState()
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val syncLogs by viewModel.syncLogs.collectAsState()

    val selectedErp by viewModel.selectedErp.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isAutoSyncEnabled by viewModel.isAutoSyncEnabled.collectAsState()

    var showSubscriptionSheet by remember { mutableStateOf(false) }
    var showDirectPaymentSheet by remember { mutableStateOf(false) }

    val activeOrders = orders.filter { it.pharmacyId == activePharmacyId }
    val activeLogs = syncLogs.filter { it.pharmacyId == activePharmacyId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Dashboard Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Store,
                            contentDescription = "Store",
                            tint = MedicalTealPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                activePharmacy?.name ?: "Select Pharmacy",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        activePharmacy?.subscriptionTier ?: "Free",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "License: ${activePharmacy?.licenseNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }

                    // Select pharmacy profile
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Config", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            pharmacies.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name) },
                                    onClick = {
                                        viewModel.setActivePharmacyId(p.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ERP Connector Control Center - "The Connector" as per user vision
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp),
            border = BorderStroke(1.dp, MedicalTealPrimary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "ERP Inventory Connector",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Sync Marg, RetailGraph, or Tally databases in 1-Click.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(MedicalTealPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "LIVE CLOUD SYNC",
                            color = MedicalTealPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ERP Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Marg ERP", "RetailGraph", "Tally ERP").forEach { erp ->
                        val isSelected = selectedErp == erp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) MedicalTealPrimary else Color.Gray.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                        )
                                .clickable { viewModel.setErp(erp) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                erp,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // retro erp desktop window representation
                ErpDesktopVisual(
                    selectedErp = selectedErp,
                    pharmacy = activePharmacy,
                    isSyncing = isSyncing
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sync action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.triggerOneClickSync(selectedErp) },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing Cloud...")
                        } else {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("One Click Sync", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Auto Sync toggle
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Auto 30s", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = isAutoSyncEnabled,
                                onCheckedChange = { viewModel.setAutoSyncEnabled(it) },
                                modifier = Modifier.scale(0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Statistics row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Total Medicines",
                value = "${activePharmacy?.totalMedicinesCount ?: 0}",
                color = MedicalTealPrimary,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Pending Orders",
                value = "${activeOrders.count { it.status == "Pending" }}",
                color = MedicalAmberAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions panel (Pricing subscription, coupons, reports)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Business Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showSubscriptionSheet = true }) {
                        Icon(Icons.Filled.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subscription Pricing", fontWeight = FontWeight.Bold, color = MedicalTealPrimary)
                    }

                    TextButton(onClick = { /* Coupon modal */ }) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage Coupons", fontWeight = FontWeight.Bold, color = MedicalTealPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showDirectPaymentSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Direct QR & Bank Payments", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Orders from Customer requests
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Active Customer Orders (${activeOrders.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (activeOrders.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No orders placed yet. Placed orders will show up here instantly.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                activeOrders.forEach { order ->
                    CustomerOrderCard(order = order, onStatusChange = { newStatus ->
                        viewModel.updateOrderStatus(order.id, newStatus)
                    })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local ERP Sync History Logs
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Local Connector Logs (Sync history)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (activeLogs.isEmpty()) {
                        Text("No sync sessions run yet. Trigger sync to view logs.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        activeLogs.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${log.erpType} Sync Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${log.itemsSyncedCount} Medicines Synced successfully", fontSize = 11.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Duration: ${log.durationMs}ms", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = log.status,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (log.status == "Success") GreenActive else RedInactive,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Subscription pricing selection modal dialog
    if (showSubscriptionSheet) {
        SubscriptionPricingDialog(
            activeTier = activePharmacy?.subscriptionTier ?: "Free",
            onSelect = {
                viewModel.selectSubscriptionTier(it)
                showSubscriptionSheet = false
            },
            onDismiss = { showSubscriptionSheet = false }
        )
    }

    if (showDirectPaymentSheet) {
        DirectPaymentDialog(
            viewModel = viewModel,
            onDismiss = { showDirectPaymentSheet = false }
        )
    }
}

@Composable
fun ErpDesktopVisual(
    selectedErp: String,
    pharmacy: Pharmacy?,
    isSyncing: Boolean
) {
    // Retro desktop app terminal representation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFF334155), RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Retro Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFEF4444), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFBBF24), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF34D399), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$selectedErp Desktop Client v8.2 - Connected",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Icon(Icons.Filled.Monitor, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            }

            // Retro terminal content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = ">> Local SQLite db location: C:\\Program Files\\${selectedErp.replace(" ", "")}\\data\\local_stocks.db",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF38BDF8)
                )

                if (isSyncing) {
                    val syncAnim = rememberInfiniteTransition().animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                    )

                    Text(
                        text = ">> [SYNC IN PROGRESS] Uploading inventory block size 4.2KB...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.alpha(syncAnim.value)
                    )
                    Text(
                        text = ">> Reading local stock logs: Paracetamol 650, Ibuprofen 400, Metformin 500...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = ">> Status: Ready to synchronize. ${pharmacy?.totalMedicinesCount ?: 0} local entries matched.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF34D399)
                    )
                    Text(
                        text = ">> Click 'One Click Sync' below to update prices and stock levels in real-time.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: com.example.data.Order,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.medicineName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MedicalTealSecondary
                    )
                    Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 11.sp, color = Color.Gray)
                }

                // Price display
                Text(
                    "₹${order.totalPrice}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MedicalTealPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Box
                Box(
                    modifier = Modifier
                        .background(
                            when (order.status) {
                                "Pending" -> MedicalAmberAccent.copy(alpha = 0.1f)
                                "Confirmed" -> BlueInfo.copy(alpha = 0.1f)
                                "Delivered" -> GreenActive.copy(alpha = 0.1f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        order.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = when (order.status) {
                            "Pending" -> MedicalAmberAccent
                            "Confirmed" -> BlueInfo
                            "Delivered" -> GreenActive
                            else -> Color.Gray
                        }
                    )
                }

                // Actions buttons to proceed lifecycle
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (order.status == "Pending") {
                        Button(
                            onClick = { onStatusChange("Confirmed") },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueInfo),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Confirm Order", fontSize = 10.sp)
                        }
                    } else if (order.status == "Confirmed") {
                        Button(
                            onClick = { onStatusChange("Delivered") },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Deliver", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SUBSCRIPTION PRICING DIALOG ----------------

@Composable
fun SubscriptionPricingDialog(
    activeTier: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
        title = {
            Text("Pharmacy Subscription Plans", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Free Plan
                SubscriptionTierCard(
                    tierName = "Free (First 3 Months)",
                    price = "₹0",
                    features = listOf("Basic Search Listing", "Limited Stock Info", "Max 5 Orders/mo"),
                    isActive = activeTier == "Free",
                    onSelect = { onSelect("Free") }
                )

                // Basic Plan
                SubscriptionTierCard(
                    tierName = "Basic Plan",
                    price = "₹499/Month",
                    features = listOf("Priority Listing", "Live stock updates", "Unlimited Orders"),
                    isActive = activeTier == "Basic",
                    onSelect = { onSelect("Basic") }
                )

                // Premium Plan
                SubscriptionTierCard(
                    tierName = "Premium Plan",
                    price = "₹999/Month",
                    features = listOf("Top Placement Search", "Priority Ad Banner Space", "Advanced Pharmacy Analytics"),
                    isActive = activeTier == "Premium",
                    onSelect = { onSelect("Premium") }
                )

                // Enterprise Plan
                SubscriptionTierCard(
                    tierName = "Enterprise Plan",
                    price = "₹1999/Month",
                    features = listOf("Sync API Integration", "Multiple Branches Mapping", "Unlimited Staff accounts"),
                    isActive = activeTier == "Enterprise",
                    onSelect = { onSelect("Enterprise") }
                )
            }
        }
    )
}

@Composable
fun SubscriptionTierCard(
    tierName: String,
    price: String,
    features: List<String>,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MedicalTealPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) MedicalTealPrimary else Color.Gray.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tierName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(price, fontWeight = FontWeight.Black, color = MedicalTealPrimary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            features.forEach { f ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = GreenActive, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(f, fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MedicalTealPrimary, RoundedCornerShape(4.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ACTIVE SUBSCRIPTION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }
    }
}

// ---------------- DIRECT QR & BANK PAYMENTS DIALOG ----------------

@Composable
fun IndianBankQRCodeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Indian Bank Branding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1F40)) // Deep Indian Bank Blue
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hindi Name
                Text(
                    text = "इंडियन बैंक",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                // Logo Icon
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFF1F5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = Color(0xFFE65100), radius = size.minDimension / 4f, center = Offset(size.width / 2, size.height / 3))
                        drawCircle(color = Color(0xFFE65100), radius = size.minDimension / 4f, center = Offset(size.width / 3, size.height * 2 / 3))
                        drawCircle(color = Color(0xFFE65100), radius = size.minDimension / 4f, center = Offset(size.width * 2 / 3, size.height * 2 / 3))
                    }
                }
                // English Name
                Text(
                    text = "Indian Bank",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Subtitle
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Indian Bank QR Code",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0C1F40)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // White Box containing details & QR Code
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Name: HEALTH CARE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Pay Directly to: 7435557693@indianbk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // QR Code Drawing
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val moduleSize = size.width / 21f // 21x21 grid representation

                            // Draw three Finder Patterns
                            fun drawFinder(x: Float, y: Float) {
                                drawRect(color = Color.Black, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(moduleSize * 7, moduleSize * 7))
                                drawRect(color = Color.White, topLeft = Offset(x + moduleSize, y + moduleSize), size = androidx.compose.ui.geometry.Size(moduleSize * 5, moduleSize * 5))
                                drawRect(color = Color.Black, topLeft = Offset(x + moduleSize * 2, y + moduleSize * 2), size = androidx.compose.ui.geometry.Size(moduleSize * 3, moduleSize * 3))
                            }

                            // Top Left Finder
                            drawFinder(0f, 0f)
                            // Top Right Finder
                            drawFinder(size.width - moduleSize * 7, 0f)
                            // Bottom Left Finder
                            drawFinder(0f, size.height - moduleSize * 7)

                            // Alignment pattern (small one near bottom-right)
                            val ax = size.width - moduleSize * 9
                            val ay = size.height - moduleSize * 9
                            drawRect(color = Color.Black, topLeft = Offset(ax, ay), size = androidx.compose.ui.geometry.Size(moduleSize * 5, moduleSize * 5))
                            drawRect(color = Color.White, topLeft = Offset(ax + moduleSize, ay + moduleSize), size = androidx.compose.ui.geometry.Size(moduleSize * 3, moduleSize * 3))
                            drawRect(color = Color.Black, topLeft = Offset(ax + moduleSize * 2, ay + moduleSize * 2), size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize))

                            // Draw randomized pixels to represent the QR code payload data
                            val random = java.util.Random(1337) // Seed for consistent QR look
                            for (row in 0 until 21) {
                                for (col in 0 until 21) {
                                    // Skip finder pattern zones
                                    if (row < 8 && col < 8) continue
                                    if (row < 8 && col > 12) continue
                                    if (row > 12 && col < 8) continue
                                    // Skip alignment pattern zone
                                    if (row in 12..16 && col in 12..16) continue

                                    if (random.nextBoolean()) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(col * moduleSize, row * moduleSize),
                                            size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer: Scan and Pay Using INDOASIS BHIM Paytm PhonePe GPay
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scan and Pay Using",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Badges row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, start = 6.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("INDoasis", color = Color(0xFF0F172A), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(Color(0xFF0F172A), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("BHIM", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(Color(0xFF0284C7), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("paytm", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(Color(0xFF581C87), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("PhonePe", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(Color(0xFF047857), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("GPay", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectPaymentDialog(
    viewModel: PharmaViewModel,
    onDismiss: () -> Unit
) {
    val activePharmacyId by viewModel.activePharmacyId.collectAsState()
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val paymentProofs by viewModel.paymentProofs.collectAsState()

    var paymentType by remember { mutableStateOf("Subscription") } // "Subscription" or "Commission"
    var amountText by remember { mutableStateOf("499") }
    var utrNumber by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf("2026-07-05") }
    var selectedTier by remember { mutableStateOf("Basic") } // "Basic", "Premium", "Enterprise"
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Calculate commission owed
    val activeOrders = orders.filter { it.pharmacyId == activePharmacyId }
    val deliveredOrders = activeOrders.filter { it.status == "Delivered" }
    val commissionOwed = deliveredOrders.sumOf { it.totalPrice } * 0.05

    // Automatically adjust pre-filled amount based on payment selections
    LaunchedEffect(paymentType, selectedTier, commissionOwed) {
        if (paymentType == "Subscription") {
            amountText = when (selectedTier) {
                "Basic" -> "499"
                "Premium" -> "999"
                "Enterprise" -> "1999"
                else -> "0"
            }
        } else {
            amountText = String.format("%.0f", commissionOwed)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (utrNumber.trim().isEmpty()) {
                        errorMessage = "Please enter the Transaction Reference (UTR) Number."
                        successMessage = ""
                    } else {
                        val amountParsed = amountText.toDoubleOrNull() ?: 0.0
                        if (amountParsed <= 0.0) {
                            errorMessage = "Please enter a valid payment amount."
                            successMessage = ""
                        } else {
                            viewModel.submitPaymentProof(
                                paymentType = paymentType,
                                amount = amountParsed,
                                utrNumber = utrNumber.trim(),
                                paymentDate = paymentDate,
                                tierRequested = if (paymentType == "Subscription") selectedTier else ""
                            )
                            successMessage = "Payment proof submitted successfully! Super Admin will verify and approve shortly."
                            errorMessage = ""
                            utrNumber = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary)
            ) {
                Text("Submit Proof")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCode, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("QR & Direct Bank Transfer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Indian Bank Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MedicalTealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Indian Bank Account Details", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MedicalTealPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val rowModifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        @Composable
                        fun DetailRow(label: String, valText: String) {
                            Row(rowModifier, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, fontSize = 10.sp, color = Color.Gray)
                                Text(valText, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                            }
                        }
                        
                        DetailRow("Bank Name", "Indian Bank")
                        DetailRow("Branch Name", "NISCHINDRAPUR")
                        DetailRow("IFSC Code", "IDIB000N601")
                        DetailRow("Account No.", "7435557693")
                        DetailRow("Account Holder", "HEALTH CARE")
                        DetailRow("UPI ID", "7435557693@indianbk")
                    }
                }

                // Section 2: QR Code Visualizer
                IndianBankQRCodeCard()

                // Success or Error banners
                if (successMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF81C784), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(successMessage, color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFE57373), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(errorMessage, color = Color(0xFFC62828), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Section 3: Payment Verification Submission Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Log Your Payment Proof", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)

                        // Selector for Payment Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = paymentType == "Subscription",
                                onClick = { paymentType = "Subscription" },
                                label = { Text("Subscription", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = paymentType == "Commission",
                                onClick = { paymentType = "Commission" },
                                label = { Text("Order Commission", fontSize = 11.sp) }
                            )
                        }

                        if (paymentType == "Subscription") {
                            // Selected Tier dropdown or chips
                            Text("Select Target Subscription Tier:", fontSize = 10.sp, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Basic" to "₹499", "Premium" to "₹999", "Enterprise" to "₹1999").forEach { (tier, price) ->
                                    FilterChip(
                                        selected = selectedTier == tier,
                                        onClick = { selectedTier = tier },
                                        label = { Text("$tier ($price)", fontSize = 10.sp) }
                                    )
                                }
                            }
                        } else {
                            // Commission detail
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Total Delivered Orders: ${deliveredOrders.size}\nTotal Store Sales: ₹${deliveredOrders.sumOf { it.totalPrice }.toInt()}\nComputed 5% Network Fee due: ₹${String.format("%.2f", commissionOwed)}",
                                    color = Color(0xFF1D4ED8),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        // Text fields
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount Paid (₹)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = utrNumber,
                            onValueChange = { utrNumber = it },
                            label = { Text("UTR / Transaction Reference No. *", fontSize = 11.sp) },
                            placeholder = { Text("e.g. 334208154673", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = paymentDate,
                            onValueChange = { paymentDate = it },
                            label = { Text("Payment Date", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Section 4: History
                val myProofs = paymentProofs.filter { it.pharmacyId == activePharmacyId }
                if (myProofs.isNotEmpty()) {
                    Text("Your Payment Log History", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        myProofs.forEach { proof ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (proof.paymentType == "Subscription") "Upgrade to ${proof.tierRequested}" else "Order Commission Payment",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                        Text("UTR: ${proof.utrNumber}", fontSize = 9.sp, color = Color.Gray)
                                        Text("Date: ${proof.paymentDate}", fontSize = 9.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${proof.amount.toInt()}", fontWeight = FontWeight.Black, fontSize = 11.sp, color = MedicalTealPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        val (bgColor, textColor) = when (proof.status) {
                                            "Approved" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                            "Rejected" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                                            else -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(bgColor, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(proof.status, color = textColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


// ---------------- SUPER ADMIN GLOBAL DASHBOARD ----------------

@Composable
fun PaymentVerificationPanel(
    viewModel: PharmaViewModel,
    paymentProofs: List<PaymentProof>
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Payment & Fee Verification Queue",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (paymentProofs.isEmpty()) {
                    Text("No payment submissions or receipts to verify yet.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    paymentProofs.forEach { proof ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(proof.pharmacyName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                        Text(
                                            text = if (proof.paymentType == "Subscription") "Subscription Upgrade to ${proof.tierRequested}" else "Order Commission Fee Payment",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MedicalTealPrimary
                                        )
                                    }
                                    Text("₹${proof.amount.toInt()}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MedicalTealPrimary)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("UTR / Ref: ${proof.utrNumber}", fontSize = 9.sp, color = Color.DarkGray)
                                        Text("Date Paid: ${proof.paymentDate}", fontSize = 9.sp, color = Color.Gray)
                                    }

                                    // Actions based on status
                                    if (proof.status == "Pending") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { viewModel.approvePaymentProof(proof.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("Approve", fontSize = 10.sp, color = Color.White)
                                            }
                                            Button(
                                                onClick = { viewModel.rejectPaymentProof(proof.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = RedInactive),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("Reject", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    } else {
                                        val statusColor = when (proof.status) {
                                            "Approved" -> GreenActive
                                            "Rejected" -> RedInactive
                                            else -> Color.Gray
                                        }
                                        Text(
                                            text = proof.status,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            fontSize = 11.sp
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
}

@Composable
fun SuperAdminDashboard(viewModel: PharmaViewModel) {
    val pharmacies by viewModel.pharmacies.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val syncLogs by viewModel.syncLogs.collectAsState()
    val paymentProofs by viewModel.paymentProofs.collectAsState()

    val totalUsers = 1240 // Simulated static metric
    val commissions = orders.sumOf { it.totalPrice } * 0.05 // 5% Commission
    val subscriptionsRevenue = pharmacies.sumOf {
        when (it.subscriptionTier) {
            "Basic" -> 499.0
            "Premium" -> 999.0
            "Enterprise" -> 1999.0
            else -> 0.0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Admin Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF3F51B5), Color(0xFF303F9F))
                    )
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Super Admin Monitor", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Text("Global Pharmacy Network Infrastructure", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f)))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Financial summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Subscription Revenue",
                value = "₹${subscriptionsRevenue.toInt()}",
                color = GreenActive,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Order Commissions (5%)",
                value = "₹${commissions.toInt()}",
                color = GreenActive,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Global stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Registered Pharmacies",
                value = "${pharmacies.size}",
                color = MedicalTealPrimary,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Total Client Users",
                value = "$totalUsers",
                color = BlueInfo,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HealthTech SaaS launch strategy & ERP connector planner
        HealthTechLaunchPlanner(viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Verification Section
        PaymentVerificationPanel(viewModel = viewModel, paymentProofs = paymentProofs)

        Spacer(modifier = Modifier.height(16.dp))

        // Global network log view
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Global Sync Operations Network Activity",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (syncLogs.isEmpty()) {
                        Text("No global operations logged yet.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        syncLogs.forEach { log ->
                            val pharmaName = pharmacies.find { it.id == log.pharmacyId }?.name ?: "Unknown"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(pharmaName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Synched ${log.itemsSyncedCount} stock items using ${log.erpType}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = log.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.status == "Success") GreenActive else RedInactive
                                    )
                                    Text("Ping: ${log.durationMs}ms", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HealthTechLaunchPlanner(viewModel: PharmaViewModel) {
    val isMargActive by viewModel.isMargConnectorActive.collectAsState()
    val isTallyActive by viewModel.isTallyXmlConnectorActive.collectAsState()
    val isRetailGraphActive by viewModel.isRetailGraphSqlConnectorActive.collectAsState()

    val isAesActive by viewModel.isAesEncryptionActive.collectAsState()
    val isAnonymizationActive by viewModel.isDataAnonymizationActive.collectAsState()

    val trialMonths by viewModel.trialPeriodMonths.collectAsState()
    val monthlyPrice by viewModel.monthlySubscriptionPrice.collectAsState()
    val isPromoCampaignActive by viewModel.isOnboardingDiscountActive.collectAsState()

    var activeTab by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MedicalTealPrimary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = MedicalTealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "SaaS Launch & ERP Integration Planner",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Model ERP connectors, HIPAA data security, and Subscription campaigns.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("ERP Drivers", "Data Security", "SaaS Promo")
                tabs.forEachIndexed { index, label ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) MedicalTealPrimary else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { activeTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.DarkGray
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (activeTab) {
                0 -> {
                    // ERP Sync Drivers & Connector Structures
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Authorized ERP Connectors & Database Structures",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTealSecondary
                        )
                        Text(
                            "Configure separate database mappers for different billing systems. When a pharmacy triggers a sync request, our SaaS driver pulls authentic updates securely.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // Driver 1: Marg ERP
                        ErpDriverConfigRow(
                            name = "Marg ERP Driver",
                            technology = "Local SQLite DB Direct Streamer",
                            isActive = isMargActive,
                            onToggle = { viewModel.toggleMargConnector() }
                        )

                        // Driver 2: Tally ERP
                        ErpDriverConfigRow(
                            name = "Tally XML SOAP Mapper",
                            technology = "Structured XML Parsing Service",
                            isActive = isTallyActive,
                            onToggle = { viewModel.toggleTallyXmlConnector() }
                        )

                        // Driver 3: RetailGraph SQL Driver
                        ErpDriverConfigRow(
                            name = "RetailGraph SQL Connector",
                            technology = "Direct SQL Instance Bridge",
                            isActive = isRetailGraphActive,
                            onToggle = { viewModel.toggleRetailGraphSqlConnector() }
                        )
                    }
                }
                1 -> {
                    // Data Security & HIPAA Privacy controls
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Prescription Vault & Privacy Safeguards",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTealSecondary
                        )
                        Text(
                            "User trust is crucial. Prescriptions and medical credentials undergo rigorous cloud-level safety controls to prevent breaches.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // AES-256 Toggle
                        SecuritySettingCard(
                            title = "AES-256 Prescription Encryption",
                            description = "Field-level high-grade database encryption for all user uploaded documents and prescription text.",
                            isActive = isAesActive,
                            onToggle = { viewModel.toggleAesEncryption() }
                        )

                        // HIPAA/GDPR Anonymization
                        SecuritySettingCard(
                            title = "Compliance Anonymization Layer",
                            description = "Strips personal identifying info (PII) from analytical logs to comply with strict medical privacy laws.",
                            isActive = isAnonymizationActive,
                            onToggle = { viewModel.toggleDataAnonymization() }
                        )
                    }
                }
                2 -> {
                    // Subscription Campaign Settings
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Subscription Model Strategy Planner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTealSecondary
                        )
                        Text(
                            "Attract more pharmacies with introductory pricing. Offer a generous trial, then convert them to our main flat rate subscription model.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // Config Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Free Onboarding Campaign", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Switch(
                                        checked = isPromoCampaignActive,
                                        onCheckedChange = { viewModel.toggleOnboardingDiscount() },
                                        modifier = Modifier.scale(0.7f)
                                    )
                                }

                                if (isPromoCampaignActive) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Introductory Offer:", fontSize = 11.sp)
                                        Text(
                                            "$trialMonths Months FREE, then ₹$monthlyPrice/Month",
                                            fontWeight = FontWeight.Bold,
                                            color = MedicalTealPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Quick control buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.setSubscriptionStrategy(3, 499) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Set 3 Mo / ₹499", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.setSubscriptionStrategy(6, 399) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Set 6 Mo / ₹399", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErpDriverConfigRow(
    name: String,
    technology: String,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE6F4EA) else Color.Gray.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, if (isActive) Color(0xFF34A853).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isActive) Color(0xFF34A853) else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Text(technology, fontSize = 9.sp, color = Color.Gray)
            }
            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(0.7f)
            )
        }
    }
}

@Composable
fun SecuritySettingCard(
    title: String,
    description: String,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFECEFF1) else Color.Gray.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(description, fontSize = 9.sp, color = Color.Gray, lineHeight = 12.sp)
            }
            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(0.7f)
            )
        }
    }
}

// Simple modifier helper
private fun Modifier.scale(scale: Float) = this.then(
    Modifier.pointerInput(Unit) {} // fallback
)
