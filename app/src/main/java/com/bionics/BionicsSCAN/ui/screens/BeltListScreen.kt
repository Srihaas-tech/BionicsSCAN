package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.InventoryType
import com.bionics.BionicsSCAN.service.BarcodeAccessibilityService
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel
import com.bionics.BionicsSCAN.viewmodel.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeltListScreen(
    viewModel: BeltViewModel,
    onScanClick: () -> Unit,
    onBeltClick: (Belt) -> Unit,
    onLabelsClick: () -> Unit,
    onLowStockClick: () -> Unit,
    onOutOfStockClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAccessibilityEnabled by remember { mutableStateOf(BarcodeAccessibilityService.isEnabled(context)) }
    var isDismissed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = BarcodeAccessibilityService.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val belts by viewModel.belts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val selectedType by viewModel.selectedInventoryType.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                // Branded Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Bionics 4909",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "FRC inventory",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row {
                        IconButton(onClick = onLabelsClick) {
                            Icon(Icons.Default.Print, contentDescription = "Labels")
                        }
                        IconButton(
                            onClick = onScanClick,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                        }
                    }
                }

                // Connection Banner
                ConnectionBanner(
                    syncStatus = syncStatus,
                    pendingCount = pendingCount,
                    lastSyncTime = lastSyncTime,
                    onRefresh = { viewModel.refreshFromBackend() }
                )

                // Background Scanner Service Setup Banner
                if (!isAccessibilityEnabled && !isDismissed) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Sensors,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Background Scanner Inactive",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Enable in Accessibility to scan from any app or home screen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            FilledTonalButton(
                                onClick = { BarcodeAccessibilityService.openAccessibilitySettings(context) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Enable", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(
                                onClick = { isDismissed = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Dashboard Metrics
            item {
                DashboardSection(
                    metrics = dashboardMetrics,
                    onLowStockClick = onLowStockClick,
                    onOutOfStockClick = onOutOfStockClick
                )
            }

            // Tabs and Search
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    ScrollableTabRow(
                        selectedTabIndex = InventoryType.entries.indexOf(selectedType),
                        edgePadding = 16.dp,
                        divider = {},
                        containerColor = Color.Transparent
                    ) {
                        InventoryType.entries.forEach { type ->
                            val count = categoryCounts[type] ?: 0
                            Tab(
                                selected = selectedType == type,
                                onClick = { viewModel.setInventoryType(type) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(type.displayName)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Badge(
                                            containerColor = if (selectedType == type) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(count.toString())
                                        }
                                    }
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search by size or barcode...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
            
            if (isLoading && belts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.5f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (error != null && belts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxHeight(0.5f).fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadBelts() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (belts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(0.5f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No items found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(belts, key = { it.id }) { belt ->
                    InventoryCard(
                        belt = belt,
                        onClick = { onBeltClick(belt) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionBanner(
    syncStatus: SyncStatus,
    pendingCount: Int,
    lastSyncTime: Long?,
    onRefresh: () -> Unit
) {
    val (color, label, icon) = when (syncStatus) {
        SyncStatus.ONLINE -> Triple(Color(0xFF4CAF50), "Synced", Icons.Default.CloudDone)
        SyncStatus.SYNCING -> Triple(MaterialTheme.colorScheme.primary, "Syncing...", Icons.Default.CloudSync)
        SyncStatus.OFFLINE -> Triple(Color.Gray, "Offline", Icons.Default.CloudOff)
        SyncStatus.PENDING_CHANGES -> Triple(MaterialTheme.colorScheme.tertiary, "$pendingCount pending", Icons.Default.CloudSync)
        SyncStatus.SYNC_ERROR -> Triple(MaterialTheme.colorScheme.error, "Sync Error", Icons.Default.Warning)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
            if (lastSyncTime != null) {
                val timeStr = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(lastSyncTime))
                Text("Last checked: $timeStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun DashboardSection(
    metrics: com.bionics.BionicsSCAN.viewmodel.DashboardMetrics,
    onLowStockClick: () -> Unit,
    onOutOfStockClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(modifier = Modifier.weight(1f), title = "Sizes", value = metrics.totalSizes.toString(), icon = Icons.Default.Category)
            MetricCard(modifier = Modifier.weight(1f), title = "Total units", value = metrics.totalUnits.toString(), icon = Icons.Default.Inventory)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Low stock",
                value = metrics.lowStockCount.toString(),
                icon = Icons.Default.TrendingDown,
                color = Color(0xFFF44336),
                onClick = onLowStockClick
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Out of stock",
                value = metrics.outOfStockCount.toString(),
                icon = Icons.Default.ErrorOutline,
                color = Color.Black,
                onClick = onOutOfStockClick
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun InventoryCard(
    belt: Belt,
    onClick: () -> Unit
) {
    val unit = when (belt.inventoryType) {
        InventoryType.BELT_9MM, InventoryType.BELT_15MM -> "mm"
        InventoryType.GEAR, InventoryType.SPROCKET -> "T"
    }

    val typeLabel = when (belt.inventoryType) {
        InventoryType.BELT_9MM -> "9mm Belt"
        InventoryType.BELT_15MM -> "15mm Belt"
        InventoryType.GEAR -> "Gear"
        InventoryType.SPROCKET -> "Sprocket"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${belt.length}$unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = typeLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = belt.barcode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = belt.quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (belt.quantity <= 0) Color.Red else MaterialTheme.colorScheme.onSurface
                )
                
                val (badgeColor, badgeLabel) = when {
                    belt.quantity <= 0 -> Color.Red to "OUT"
                    belt.quantity <= 5 -> Color(0xFFF44336) to "LOW"
                    else -> Color(0xFF4CAF50) to "IN STOCK"
                }
                
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
