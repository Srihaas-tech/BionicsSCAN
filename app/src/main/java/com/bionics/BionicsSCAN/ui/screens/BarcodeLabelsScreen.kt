package com.bionics.BionicsSCAN.ui.screens

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.utils.BarcodeGenerator
import com.bionics.BionicsSCAN.viewmodel.BeltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeLabelsScreen(
    viewModel: BeltViewModel,
    onBackClick: () -> Unit
) {
    val belts by viewModel.belts.collectAsState()
    val selectedType by viewModel.selectedInventoryType.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Labels") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        printBarcodeLabels(context, belts, selectedType.displayName)
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print ${selectedType.displayName}")
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
                selectedTabIndex = com.bionics.BionicsSCAN.data.InventoryType.entries.indexOf(selectedType),
                modifier = Modifier.fillMaxWidth()
            ) {
                com.bionics.BionicsSCAN.data.InventoryType.entries.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { viewModel.setInventoryType(type) },
                        text = { Text(type.displayName, fontSize = MaterialTheme.typography.labelMedium.fontSize) }
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(belts) { belt ->
                    BarcodeLabelItem(belt = belt)
                }
                
                if (belts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items found in ${selectedType.displayName}")
                        }
                    }
                }
            }
        }
    }
}

private fun printBarcodeLabels(context: Context, belts: List<Belt>, typeName: String) {
    if (belts.isEmpty()) {
        Toast.makeText(context, "No labels to print", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                
                val pdfDocument = generatePdfDocument(belts)
                val info = android.print.PrintDocumentInfo.Builder("${typeName.replace(" ", "_").lowercase()}_labels.pdf")
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pdfDocument?.pages?.size ?: 0)
                    .build()
                
                callback?.onLayoutFinished(info, true)
            }
            
            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }
                
                try {
                    val pdfDocument = generatePdfDocument(belts)
                    val fileOutputStream = java.io.FileOutputStream(destination?.fileDescriptor)
                    pdfDocument?.writeTo(fileOutputStream)
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.toString())
                }
            }
        }
        
        printManager.print("$typeName Labels", printAdapter, null)
    } catch (e: Exception) {
        Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun generatePdfDocument(belts: List<Belt>): PdfDocument? {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    
    val paint = android.graphics.Paint()
    val textPaint = android.graphics.Paint()
    textPaint.textSize = 12f
    textPaint.color = android.graphics.Color.BLACK
    
    var xPos = 20f
    var yPos = 20f
    val labelWidth = 260f
    val labelHeight = 100f
    val labelsPerRow = 2
    val margin = 15f
    val barcodeHorizontalPadding = 20f
    val barcodeHeight = 50f
    val barcodeMaxWidth = labelWidth - (barcodeHorizontalPadding * 2f)
    val barcodePaint = android.graphics.Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }
    
    belts.forEachIndexed { index, belt ->
        if (index > 0 && index % labelsPerRow == 0) {
            xPos = 20f
            yPos += labelHeight + margin
        }
        
        if (yPos + labelHeight > 820f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPos = 20f
            xPos = 20f
        }
        
        // Draw label border
        paint.color = android.graphics.Color.BLACK
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(xPos, yPos, xPos + labelWidth, yPos + labelHeight, paint)
        
        // Draw belt name
        paint.style = android.graphics.Paint.Style.FILL
        textPaint.textSize = 12f
        val typeLabel = when (belt.inventoryType) {
            com.bionics.BionicsSCAN.data.InventoryType.BELT_9MM -> "Belt 9mm"
            com.bionics.BionicsSCAN.data.InventoryType.BELT_15MM -> "Belt 15mm"
            com.bionics.BionicsSCAN.data.InventoryType.GEAR -> "Gear"
            com.bionics.BionicsSCAN.data.InventoryType.SPROCKET -> "Sprocket"
        }
        val unit = when (belt.inventoryType) {
            com.bionics.BionicsSCAN.data.InventoryType.BELT_9MM,
            com.bionics.BionicsSCAN.data.InventoryType.BELT_15MM -> "mm"
            com.bionics.BionicsSCAN.data.InventoryType.GEAR,
            com.bionics.BionicsSCAN.data.InventoryType.SPROCKET -> "T"
        }
        canvas.drawText("$typeLabel: ${belt.length}$unit", xPos + 5f, yPos + 18f, textPaint)
        
        // Keep the barcode inside the cut border.
        val barcode = BarcodeGenerator.generateCode128Barcode(
            content = belt.barcode,
            width = barcodeMaxWidth.toInt(),
            height = barcodeHeight.toInt()
        )
        barcode?.let {
            val scale = minOf(1f, barcodeMaxWidth / it.width.toFloat())
            val renderedWidth = it.width * scale
            val barcodeLeft = xPos + (labelWidth - renderedWidth) / 2f
            val barcodeTop = yPos + 28f
            val source = android.graphics.Rect(0, 0, it.width, it.height)
            val destination = android.graphics.RectF(
                barcodeLeft,
                barcodeTop,
                barcodeLeft + renderedWidth,
                barcodeTop + barcodeHeight
            )
            canvas.drawBitmap(it, source, destination, barcodePaint)
        }
        
        // Draw the full barcode text below
        textPaint.textSize = 9f
        canvas.drawText(belt.barcode, xPos + 5f, yPos + 90f, textPaint)
        
        xPos += labelWidth + margin
    }
    
    pdfDocument.finishPage(page)
    return pdfDocument
}

@Composable
fun BarcodeLabelItem(belt: Belt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val unit = when (belt.inventoryType) {
                com.bionics.BionicsSCAN.data.InventoryType.BELT_9MM,
                com.bionics.BionicsSCAN.data.InventoryType.BELT_15MM -> "mm"
                com.bionics.BionicsSCAN.data.InventoryType.GEAR,
                com.bionics.BionicsSCAN.data.InventoryType.SPROCKET -> "T"
            }
            Text(
                text = "${belt.inventoryType.displayName}: ${belt.length}$unit",
                fontWeight = FontWeight.Bold
            )
            
            val barcode = BarcodeGenerator.generateCode128Barcode(belt.barcode)
            barcode?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Barcode for ${belt.barcode}",
                        modifier = Modifier.widthIn(max = 300.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Text(
                text = belt.barcode,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "Qty: ${belt.quantity}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
