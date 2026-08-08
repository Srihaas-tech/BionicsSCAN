package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bionics.BionicsSCAN.scanner.BarcodeScanner
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: BeltViewModel,
    onBackClick: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val scannedBarcode by viewModel.scannedBarcode.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Barcode") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            BarcodeScanner(
                onBarcodeDetected = { barcode ->
                    viewModel.onBarcodeScanned(barcode)
                    onBarcodeScanned(barcode)
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Scanning indicator
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = if (scannedBarcode == null) "Point camera at barcode" else "Scanned: $scannedBarcode",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
