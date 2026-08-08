package com.bionics.BionicsSCAN.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.oned.Code128Writer
import java.util.EnumMap

object BarcodeGenerator {
    
    fun generateCode128Barcode(
        content: String,
        width: Int = 400,
        height: Int = 100
    ): Bitmap? {
        try {
            val writer = Code128Writer()
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 0
            
            // Calculate minimum width needed for the content to prevent squishing
            // Code128 needs approximately 11 modules per character, each module should be at least 3 pixels for readability
            val minWidth = (content.length + 3) * 11 * 4 // +3 for start/stop/check characters, 4 pixels per module
            val actualWidth = maxOf(width, minWidth)
            
            val bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, actualWidth, height, hints)
            
            val bitmap = Bitmap.createBitmap(actualWidth, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until actualWidth) {
                for (y in 0 until height) {
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
