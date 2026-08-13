package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockFilterScreen(
    title: String,
    viewModel: BeltViewModel,
    filterType: String, // "LOW_STOCK" or "OUT_OF_STOCK"
    onBackClick: () -> Unit,
    onBeltClick: (Belt) -> Unit
) {
    val items by if (filterType == "LOW_STOCK") {
        viewModel.lowStockItems.collectAsState()
    } else {
        viewModel.outOfStockItems.collectAsState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filterType == "LOW_STOCK") "No low stock items found." else "No out of stock items found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items, key = { it.id }) { belt ->
                    InventoryCard(
                        belt = belt,
                        onClick = { onBeltClick(belt) }
                    )
                }
            }
        }
    }
}
