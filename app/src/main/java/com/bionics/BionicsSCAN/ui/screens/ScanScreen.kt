package com.bionics.BionicsSCAN.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bionics.BionicsSCAN.scanner.BarcodeScanner
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: BeltViewModel,
    onBackClick: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    var isPaused by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var lookupError by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Scanner Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                BarcodeScanner(
                    onBarcodeDetected = { barcode ->
                        if (!isPaused && !isSearching) {
                            onBarcodeScanned(barcode)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    isPaused = isPaused
                )

                // Overlay with guides
                ScannerOverlay(isPaused = isPaused)

                // Status Badge
                Surface(
                    color = if (isPaused) Color.Gray else Color(0xFF4CAF50),
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, shape = MaterialTheme.shapes.extraSmall)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPaused) "CAMERA STOPPED" else "CAMERA READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Camera Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilledTonalButton(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isPaused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isPaused) "Start Camera" else "Stop Camera")
                    }
                }
            }

            // Manual Entry Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Manual Lookup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualBarcode,
                        onValueChange = { 
                            manualBarcode = it.uppercase() 
                            lookupError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. B9-325 or GR-48") },
                        label = { Text("Barcode ID") },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                IconButton(
                                    onClick = {
                                        if (manualBarcode.isNotBlank()) {
                                            isSearching = true
                                            scope.launch {
                                                val belt = viewModel.getBeltByBarcode(manualBarcode)
                                                if (belt != null) {
                                                    onBarcodeScanned(manualBarcode)
                                                } else {
                                                    lookupError = "Item not found in inventory."
                                                }
                                                isSearching = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Find")
                                }
                            }
                        },
                        singleLine = true,
                        isError = lookupError != null
                    )
                    if (lookupError != null) {
                        Text(
                            text = lookupError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay(isPaused: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val linePosition by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "linePosition"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val rectSize = width * 0.7f
        val left = (width - rectSize) / 2
        val top = (height - rectSize) / 2
        val right = left + rectSize
        val bottom = top + rectSize

        // Dim background
        with(drawContext.canvas.nativeCanvas) {
            val checkPoint = saveLayer(null, null)
            drawRect(Color.Black.copy(alpha = 0.6f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(rectSize, rectSize),
                cornerRadius = CornerRadius(12.dp.toPx()),
                blendMode = BlendMode.Clear
            )
            restoreToCount(checkPoint)
        }

        // Guides
        val cornerLength = 40.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val guideColor = if (isPaused) Color.Gray else Color(0xFF4CAF50)

        // Top Left
        drawLine(guideColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(guideColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
        
        // Top Right
        drawLine(guideColor, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
        drawLine(guideColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)
        
        // Bottom Left
        drawLine(guideColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
        drawLine(guideColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)
        
        // Bottom Right
        drawLine(guideColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
        drawLine(guideColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)

        // Animated Scan Line
        if (!isPaused) {
            val currentLineY = top + (rectSize * linePosition)
            drawLine(
                color = Color.Red,
                start = Offset(left + 10.dp.toPx(), currentLineY),
                end = Offset(right - 10.dp.toPx(), currentLineY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
