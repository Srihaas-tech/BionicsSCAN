package com.bionics.BionicsSCAN.data

import com.bionics.BionicsSCAN.database.InventoryEntity
import com.bionics.BionicsSCAN.database.PendingTransactionEntity
import com.bionics.BionicsSCAN.network.*

// Mapping from DTO to Entity
fun InventoryPartDto.toEntity(): InventoryEntity {
    return InventoryEntity(
        id = id,
        length = metadata.size,
        quantity = quantity,
        barcode = mfgPartNumber,
        inventoryType = metadata.inventoryType,
        lastUpdated = System.currentTimeMillis()
    )
}

// Mapping from Entity to UI Model
fun InventoryEntity.toBelt(): Belt {
    val inventoryTypeEnum = try {
        InventoryType.valueOf(inventoryType)
    } catch (_: Exception) {
        // Fallback or log error
        InventoryType.BELT_9MM 
    }
    
    return Belt(
        id = id,
        length = length,
        quantity = quantity,
        barcode = barcode,
        inventoryType = inventoryTypeEnum,
        lastUpdated = lastUpdated
    )
}

// Mapping from PendingTransactionEntity to TransactionRequestDto
fun PendingTransactionEntity.toTransactionRequest(): TransactionRequestDto {
    return TransactionRequestDto(
        actor = "bionicsscan-android",
        note = null,
        lines = listOf(
            TransactionLineDto(
                partId = inventoryId,
                quantityDelta = quantityChange,
                usedIn = null
            )
        )
    )
}
