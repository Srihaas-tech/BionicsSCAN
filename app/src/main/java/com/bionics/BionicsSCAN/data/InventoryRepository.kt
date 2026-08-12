package com.bionics.BionicsSCAN.data

import android.content.Context
import com.bionics.BionicsSCAN.database.BionicsDatabase
import com.bionics.BionicsSCAN.database.InventoryEntity
import com.bionics.BionicsSCAN.database.PendingTransactionEntity
import com.bionics.BionicsSCAN.network.BionicInventoryApi
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class InventoryRepository(context: Context) {
    
    private val database = BionicsDatabase.getDatabase(context)
    private val inventoryDao = database.inventoryDao()
    private val pendingTransactionDao = database.pendingTransactionDao()
    private val api = BionicInventoryApi.create()
    
    // Get all inventory as Flow of Belt UI models
    fun getAllBelts(): Flow<List<Belt>> {
        return inventoryDao.getAllInventory().map { entities ->
            entities.map { it.toBelt() }
        }
    }
    
    // Get inventory by type as Flow of Belt UI models
    fun getBeltsByType(inventoryType: InventoryType): Flow<List<Belt>> {
        return inventoryDao.getInventoryByType(inventoryType.name).map { entities ->
            entities.map { it.toBelt() }
        }
    }
    
    // Get belt by ID
    suspend fun getBeltById(id: String): Belt? {
        return withContext(Dispatchers.IO) {
            inventoryDao.getInventoryById(id)?.toBelt()
        }
    }
    
    // Get belt by barcode
    suspend fun getBeltByBarcode(barcode: String): Belt? {
        return withContext(Dispatchers.IO) {
            inventoryDao.getInventoryByBarcode(barcode)?.toBelt()
        }
    }
    
    // Get pending transactions count
    fun getPendingCount(): Flow<Int> {
        return pendingTransactionDao.getCountFlow()
    }
    
    // Sync inventory from backend
    suspend fun syncInventory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.getInventory()
            val entities = response.inventory.map { it.toEntity() }
            inventoryDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Atomic checkout: update local quantity and queue transaction
    suspend fun checkoutBelt(beltId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if item exists and has stock
            val inventory = inventoryDao.getInventoryById(beltId)
                ?: return@withContext Result.failure(Exception("Item not found"))
            
            if (inventory.quantity <= 0) {
                return@withContext Result.failure(Exception("No items available"))
            }
            
            // Single Room transaction: update quantity and insert pending transaction
            database.withTransaction {
                inventoryDao.updateQuantity(beltId, -1)
                
                val transaction = PendingTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    inventoryId = beltId,
                    transactionType = "CHECKOUT",
                    quantityChange = -1,
                    timestamp = System.currentTimeMillis()
                )
                pendingTransactionDao.insert(transaction)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Atomic checkin: update local quantity and queue transaction
    suspend fun checkinBelt(beltId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if item exists
            val inventory = inventoryDao.getInventoryById(beltId)
                ?: return@withContext Result.failure(Exception("Item not found"))
            
            // Single Room transaction: update quantity and insert pending transaction
            database.withTransaction {
                inventoryDao.updateQuantity(beltId, 1)
                
                val transaction = PendingTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    inventoryId = beltId,
                    transactionType = "CHECKIN",
                    quantityChange = 1,
                    timestamp = System.currentTimeMillis()
                )
                pendingTransactionDao.insert(transaction)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get next pending transaction for sync
    suspend fun getNextPendingTransaction(): PendingTransactionEntity? {
        return withContext(Dispatchers.IO) {
            pendingTransactionDao.getNextPendingTransaction()
        }
    }
    
    // Delete pending transaction after successful sync
    suspend fun deletePendingTransaction(transactionId: String) {
        withContext(Dispatchers.IO) {
            pendingTransactionDao.deleteById(transactionId)
        }
    }
    
    // Post transaction to backend
    suspend fun postTransaction(transaction: PendingTransactionEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = transaction.toTransactionRequest()
                api.createTransaction(request)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Reconcile local inventory with backend after sync
    suspend fun reconcileInventory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Fetch latest from backend
            val response = api.getInventory()
            val entities = response.inventory.map { it.toEntity() }
            
            // Only update if no pending transactions exist
            val pendingCount = pendingTransactionDao.getCount()
            if (pendingCount == 0) {
                inventoryDao.deleteAll()
                inventoryDao.insertAll(entities)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Check if database is empty (first run)
    suspend fun isEmpty(): Boolean {
        return withContext(Dispatchers.IO) {
            inventoryDao.getCount() == 0
        }
    }
}
