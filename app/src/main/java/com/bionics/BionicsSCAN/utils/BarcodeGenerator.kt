package com.bionics.BionicsSCAN.utils

import android.graphics.Bitmap
import com.bionics.BionicsSCAN.data.InventoryType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.oned.Code128Writer
import java.util.EnumMap

object BarcodeGenerator {
    
    fun generateBarcodeCode(inventoryType: InventoryType, size: Int): String {
        val prefix = when (inventoryType) {
            InventoryType.BELT_9MM -> "B9"
            InventoryType.BELT_15MM -> "B15"
            InventoryType.GEAR -> "GR"
            InventoryType.SPROCKET -> "SP"
        }
        return "$prefix-$size"
    }
    
    fun generateCode128Barcode(
        content: String,
        width: Int = 400,
        height: Int = 100
    ): Bitmap? {
        try {
            val writer = Code128Writer()
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            
            // Use the requested width. ZXing expands it only when the data needs more modules.
            val bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, width, height, hints)
            
            val bitmap = Bitmap.createBitmap(
                bitMatrix.width,
                bitMatrix.height,
                Bitmap.Config.ARGB_8888
            )
            for (x in 0 until bitMatrix.width) {
                for (y in 0 until bitMatrix.height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }
    }
    
    fun generateQRCode(
        content: String,
        size: Int = 400
    ): Bitmap? {
        try {
            val writer = QRCodeWriter()
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 1
            
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }
    }
}
