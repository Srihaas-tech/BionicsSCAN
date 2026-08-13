package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.InventoryType
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel
import com.bionics.BionicsSCAN.utils.BarcodeGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeltDetailScreen(
    belt: Belt,
    viewModel: BeltViewModel,
    onBackClick: () -> Unit
) {
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isCheckingIn by viewModel.isCheckingIn.collectAsState()
    val isCheckingOut by viewModel.isCheckingOut.collectAsState()
    val belts by viewModel.belts.collectAsState()
    val currentBelt = belts.find { it.id == belt.id } ?: belt
    
    val pendingTransactions by viewModel.getPendingTransactionsForPart(currentBelt.id).collectAsState(initial = emptyList())

    val unit = when (currentBelt.inventoryType) {
        InventoryType.BELT_9MM, InventoryType.BELT_15MM -> "mm"
        InventoryType.GEAR, InventoryType.SPROCKET -> "T"
    }

    val typeLabel = when (currentBelt.inventoryType) {
        InventoryType.BELT_9MM -> "9mm Belt"
        InventoryType.BELT_15MM -> "15mm Belt"
        InventoryType.GEAR -> "Gear"
        InventoryType.SPROCKET -> "Sprocket"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title and Category
            item {
                Column {
                    Text(
                        text = currentBelt.inventoryType.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$typeLabel ${currentBelt.length}$unit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    val (badgeColor, badgeLabel) = when {
                        currentBelt.quantity <= 0 -> Color.Red to "OUT OF STOCK"
                        currentBelt.quantity <= 5 -> Color(0xFFF44336) to "LOW STOCK"
                        else -> Color(0xFF4CAF50) to "IN STOCK"
                    }
                    
                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Barcode Rendering
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val barcodeBitmap = BarcodeGenerator.generateCode128Barcode(currentBelt.barcode, width = 600, height = 150)
                        if (barcodeBitmap != null) {
                            Image(
                                bitmap = barcodeBitmap.asImageBitmap(),
                                contentDescription = "Barcode",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            text = currentBelt.barcode,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color.Black
                        )
                    }
                }
            }

            // Quantity Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Current Quantity", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = currentBelt.quantity.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.checkoutBelt(currentBelt.id) },
                                enabled = currentBelt.quantity > 0 && isCheckingOut == null && isCheckingIn == null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                if (isCheckingOut == currentBelt.id) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checking out...")
                                } else {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check out")
                                }
                            }
                            
                            Button(
                                onClick = { viewModel.checkinBelt(currentBelt.id) },
                                enabled = isCheckingIn == null && isCheckingOut == null,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                if (isCheckingIn == currentBelt.id) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checking in...")
                                } else {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check in")
                                }
                            }
                        }
                        
                        successMessage?.let {
                            Text(
                                it,
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 12.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        error?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }

            // Item Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Item Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        
                        InfoRow("Category", currentBelt.inventoryType.displayName)
                        InfoRow(if (unit == "mm") "Size" else "Teeth", "${currentBelt.length}$unit")
                        InfoRow("Barcode", currentBelt.barcode)
                        InfoRow("Data source", "Bionic Inventory")
                    }
                }
            }

            // Recent Activity
            item {
                Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (pendingTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                            Text("No recent activity found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(pendingTransactions) { tx ->
                    ActivityItem(tx)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActivityItem(tx: com.bionics.BionicsSCAN.database.PendingTransactionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, color) = if (tx.quantityChange > 0) {
            Icons.Default.AddCircle to Color(0xFF4CAF50)
        } else {
            Icons.Default.RemoveCircle to Color(0xFFF44336)
        }
        
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (tx.quantityChange > 0) "Checked in" else "Checked out",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                "Pending sync",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = (if (tx.quantityChange > 0) "+" else "") + tx.quantityChange.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}
