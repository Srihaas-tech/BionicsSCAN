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
                        printBarcodeLabels(context, belts)
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print All")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(belts) { belt ->
                BarcodeLabelItem(belt = belt)
            }
        }
    }
}

private fun printBarcodeLabels(context: Context, belts: List<Belt>) {
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
                val info = android.print.PrintDocumentInfo.Builder("barcode_labels.pdf")
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
        
        printManager.print("Barcode Labels", printAdapter, null)
    } catch (e: Exception) {
        Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun generatePdfDocument(belts: List<Belt>): PdfDocument? {
    val pdfDocument = PdfDocument()
    // Use landscape orientation for more width
    val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 landscape
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    
    val paint = android.graphics.Paint()
    val textPaint = android.graphics.Paint()
    textPaint.textSize = 12f
    textPaint.color = android.graphics.Color.BLACK
    
    var xPos = 20f
    var yPos = 20f
    val labelWidth = 400f
    val labelHeight = 80f
    val labelsPerRow = 2
    val margin = 15f
    
    belts.forEachIndexed { index, belt ->
        if (index > 0 && index % labelsPerRow == 0) {
            xPos = 20f
            yPos += labelHeight + margin
        }
        
        if (yPos + labelHeight > 570f) {
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
        canvas.drawText("Belt: ${belt.length}mm", xPos + 5f, yPos + 18f, textPaint)
        
        // Draw barcode (wider to prevent squishing)
        val barcode = BarcodeGenerator.generateCode128Barcode(belt.barcode, 350, 40)
        barcode?.let {
            canvas.drawBitmap(it, xPos + 25f, yPos + 25f, paint)
        }
        
        // Draw barcode text (larger font and more space)
        textPaint.textSize = 10f
        canvas.drawText(belt.barcode, xPos + 5f, yPos + 65f, textPaint)
        
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
            Text(
                text = "Belt Length: ${belt.length}mm",
                fontWeight = FontWeight.Bold
            )
            
            val barcode = BarcodeGenerator.generateCode128Barcode(belt.barcode)
            barcode?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Barcode for ${belt.barcode}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
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
