
package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel

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
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("FRC Belt Inventory")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudOff,
                            contentDescription = if (isSyncing) "Syncing" else "Offline",
                            tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncWithSpreadsheet() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Now",
                            tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(onClick = onLabelsClick) {
                        Text("Labels")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onScanClick) {
                        Text("Scan")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        "No belts found",
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
