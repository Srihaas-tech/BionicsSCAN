
package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.InventoryType
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel
import com.bionics.BionicsSCAN.viewmodel.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeltListScreen(
    viewModel: BeltViewModel,
    onScanClick: () -> Unit,
    onBeltClick: (Belt) -> Unit,
    onLabelsClick: () -> Unit
) {
    val belts by viewModel.belts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val selectedType by viewModel.selectedInventoryType.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Bionics Inventory", style = MaterialTheme.typography.titleLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (icon, tint, description) = when (syncStatus) {
                                SyncStatus.ONLINE -> Triple(Icons.Default.CloudDone, Color(0xFF4CAF50), "Synced")
                                SyncStatus.SYNCING -> Triple(Icons.Default.CloudSync, MaterialTheme.colorScheme.primary, "Syncing...")
                                SyncStatus.OFFLINE -> Triple(Icons.Default.CloudOff, Color.Gray, "Offline")
                                SyncStatus.PENDING_CHANGES -> Triple(Icons.Default.CloudSync, MaterialTheme.colorScheme.tertiary, "$pendingCount pending")
                                SyncStatus.SYNC_ERROR -> Triple(Icons.Default.CloudOff, MaterialTheme.colorScheme.error, "Sync Error")
                            }
                            
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.labelSmall,
                                color = tint
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshFromBackend() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (syncStatus == SyncStatus.SYNCING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onLabelsClick) {
                        Icon(Icons.Default.Print, contentDescription = "Labels")
                    }
                    Button(onClick = onScanClick, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text("Scan")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = InventoryType.entries.indexOf(selectedType),
                modifier = Modifier.fillMaxWidth()
            ) {
                InventoryType.entries.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { viewModel.setInventoryType(type) },
                        text = { Text(type.displayName, fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadBelts() }) {
                                Text("Retry")
                            }
                        }
                    }
                    belts.isEmpty() -> {
                        Text(
                            "No items found",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(belts) { belt ->
                                BeltItem(
                                    belt = belt,
                                    onClick = { onBeltClick(belt) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeltItem(
    belt: Belt,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Length: ${belt.length}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Barcode: ${belt.barcode}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Qty: ${belt.quantity}",
                fontWeight = FontWeight.Bold,
                color = if (belt.quantity > 0) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
