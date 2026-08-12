package com.bionics.BionicsSCAN.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey
    val id: String,
    
    val inventoryId: String,
    
    val transactionType: String, // "CHECKOUT" or "CHECKIN"
    
    val quantityChange: Int, // -1 for checkout, +1 for checkin
    
    val timestamp: Long = System.currentTimeMillis(),
    
    val deviceId: String? = null
)
