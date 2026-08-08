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

class SheetsService(private val credentialsStream: InputStream) {
    
    private val spreadsheetId: String = "1Wr_n0T0EPrmFgwfx3-Yh7oRMalMUUE5Lya4uo5zjN6A"
    private val sheetName: String = "Sheet1"
    
    private val sheets: Sheets by lazy {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        
        // Note: For production, you should use proper OAuth2 flow
        // This is a simplified version using service account credentials
        val credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential
            .fromStream(credentialsStream)
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))
        
        Sheets.Builder(transport, jsonFactory, credential)
            .setApplicationName("BionicsSCAN")
            .build()
    }
    
    suspend fun getAllBelts(): Result<List<Belt>> = withContext(Dispatchers.IO) {
        try {
            val response = sheets.spreadsheets().values()
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
        try {
            // First, find the row index for this belt
            val response = sheets.spreadsheets().values()
                .get(spreadsheetId, "$sheetName!A2:D")
                .execute()
            
            val values = response.getValues()
            val rowIndex = values?.indexOfFirst { it[0].toString() == beltId }
            
            if (rowIndex != null && rowIndex >= 0) {
                // Update the quantity (column C, which is index 2)
                val range = "$sheetName!C${rowIndex + 2}"
                val valueRange = ValueRange()
                    .setValues(listOf(listOf(newQuantity.toString())))
                
                sheets.spreadsheets().values()
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
