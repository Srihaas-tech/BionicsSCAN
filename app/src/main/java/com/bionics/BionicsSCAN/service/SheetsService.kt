package com.bionics.BionicsSCAN.service

import com.bionics.BionicsSCAN.data.Belt
import com.bionics.BionicsSCAN.data.InventoryType
import com.bionics.BionicsSCAN.utils.BarcodeGenerator
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class SheetsService(credentialsStream: InputStream) {
    
    private val spreadsheetId: String = "1Wpepi5tsE-ykRRbyr1Pmk4KWreHNp-jAN8OoycMNLn8"
    
    // Read credentials into a byte array to prevent stream closure issues
    private val credentialsBytes: ByteArray = credentialsStream.readBytes()
    private var initializationError: String? = null
    
    private val sheets: Sheets? by lazy {
        try {
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            
            val credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential
                .fromStream(credentialsBytes.inputStream())
                .createScoped(listOf(SheetsScopes.SPREADSHEETS))
            
            Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("BionicsSCAN")
                .build()
        } catch (e: Exception) {
            initializationError = "Invalid credentials: ${e.message}"
            null
        }
    }
    
    suspend fun getAllBeltsByType(inventoryType: InventoryType): Result<List<Belt>> = withContext(Dispatchers.IO) {
        if (sheets == null) {
            return@withContext Result.failure(Exception(initializationError ?: "Google Sheets service not initialized"))
        }
        
        try {
            val response = sheets!!.spreadsheets().values()
                .get(spreadsheetId, "${inventoryType.sheetName}!A2:C")
                .execute()
            
            val values = response.getValues()
            val items = values?.mapIndexed { index, row ->
                val size = row[0].toString().toInt()
                Belt(
                    id = "${index + 1}",
                    length = size,
                    quantity = row[1].toString().toInt(),
                    barcode = if (row.size > 2 && row[2].toString().isNotEmpty()) {
                        row[2].toString()
                    } else {
                        BarcodeGenerator.generateBarcodeCode(inventoryType, size)
                    },
                    inventoryType = inventoryType
                )
            } ?: emptyList()
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllBelts(): Result<List<Belt>> = withContext(Dispatchers.IO) {
        // For backward compatibility, fetch from Belt 9mm
        return@withContext getAllBeltsByType(InventoryType.BELT_9MM)
    }
    
    suspend fun updateBeltQuantity(beltId: String, newQuantity: Int, inventoryType: InventoryType): Result<Unit> = withContext(Dispatchers.IO) {
        if (sheets == null) {
            return@withContext Result.failure(Exception(initializationError ?: "Google Sheets service not initialized"))
        }
        
        try {
            // Extract the index from beltId (e.g., "5" -> index 4)
            val index = beltId.toIntOrNull()?.minus(1)
                ?: return@withContext Result.failure(Exception("Invalid belt ID"))
            
            // The row number is index + 2 (since data starts at row 2, and 0-based indexing)
            val rowNumber = index + 2
            
            // Update the quantity (column B)
            val range = "${inventoryType.sheetName}!B${rowNumber}"
            val valueRange = ValueRange()
                .setValues(listOf(listOf(newQuantity.toString())))
            
            sheets!!.spreadsheets().values()
                .update(spreadsheetId, range, valueRange)
                .setValueInputOption("RAW")
                .execute()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBeltQuantity(beltId: String, newQuantity: Int): Result<Unit> {
        // For backward compatibility
        return updateBeltQuantity(beltId, newQuantity, InventoryType.BELT_9MM)
    }
    
    suspend fun checkoutBelt(beltId: String, inventoryType: InventoryType): Result<Unit> {
        val belt = getAllBeltsByType(inventoryType).getOrNull()?.find { it.id == beltId }
            ?: return Result.failure(Exception("Belt not found"))
        
        if (belt.quantity > 0) {
            return updateBeltQuantity(beltId, belt.quantity - 1, inventoryType)
        } else {
            return Result.failure(Exception("No items available"))
        }
    }
    
    suspend fun checkinBelt(beltId: String, inventoryType: InventoryType): Result<Unit> {
        val belt = getAllBeltsByType(inventoryType).getOrNull()?.find { it.id == beltId }
            ?: return Result.failure(Exception("Belt not found"))
        
        return updateBeltQuantity(beltId, belt.quantity + 1, inventoryType)
    }
    
    suspend fun checkoutBelt(beltId: String): Result<Unit> {
        return checkoutBelt(beltId, InventoryType.BELT_9MM)
    }
    
    suspend fun checkinBelt(beltId: String): Result<Unit> {
        return checkinBelt(beltId, InventoryType.BELT_9MM)
    }
}

