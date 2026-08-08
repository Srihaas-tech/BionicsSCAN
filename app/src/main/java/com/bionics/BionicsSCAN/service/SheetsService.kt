package com.bionics.BionicsSCAN.service

import com.bionics.BionicsSCAN.data.Belt
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
    private val sheetName: String = "Sheet1"
    
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
    
    suspend fun getAllBelts(): Result<List<Belt>> = withContext(Dispatchers.IO) {
        if (sheets == null) {
            return@withContext Result.failure(Exception(initializationError ?: "Google Sheets service not initialized"))
        }
        
        try {
            val response = sheets!!.spreadsheets().values()
                .get(spreadsheetId, "$sheetName!A2:D")
                .execute()
            
            val values = response.getValues()
            val belts = values?.map { row ->
                Belt(
                    id = row[0].toString(),
                    length = row[1].toString().toInt(),
                    quantity = row[2].toString().toInt(),
                    barcode = row[3].toString()
                )
            } ?: emptyList()
            
            Result.success(belts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBeltQuantity(beltId: String, newQuantity: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (sheets == null) {
            return@withContext Result.failure(Exception(initializationError ?: "Google Sheets service not initialized"))
        }
        
        try {
            // First, find the row index for this belt
            val response = sheets!!.spreadsheets().values()
                .get(spreadsheetId, "$sheetName!A2:D")
                .execute()
            
            val values = response.getValues()
            val rowIndex = values?.indexOfFirst { it[0].toString() == beltId }
            
            if (rowIndex != null && rowIndex >= 0) {
                // Update the quantity (column C, which is index 2)
                val range = "$sheetName!C${rowIndex + 2}"
                val valueRange = ValueRange()
                    .setValues(listOf(listOf(newQuantity.toString())))
                
                sheets!!.spreadsheets().values()
                    .update(spreadsheetId, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Belt not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkoutBelt(beltId: String): Result<Unit> {
        val belt = getAllBelts().getOrNull()?.find { it.id == beltId }
            ?: return Result.failure(Exception("Belt not found"))
        
        if (belt.quantity > 0) {
            return updateBeltQuantity(beltId, belt.quantity - 1)
        } else {
            return Result.failure(Exception("No belts available"))
        }
    }
    
    suspend fun checkinBelt(beltId: String): Result<Unit> {
        val belt = getAllBelts().getOrNull()?.find { it.id == beltId }
            ?: return Result.failure(Exception("Belt not found"))
        
        return updateBeltQuantity(beltId, belt.quantity + 1)
    }
}
