package com.bionics.BionicsSCAN.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class InventoryEntity(
    @PrimaryKey
    val id: String,
    
    val length: Int,
    
    val quantity: Int,
    
    val barcode: String,
    
    val inventoryType: String,
    
    val lastUpdated: Long = System.currentTimeMillis()
)
