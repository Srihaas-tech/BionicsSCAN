package com.bionics.BionicsSCAN.data

import com.bionics.BionicsSCAN.database.InventoryEntity
import com.bionics.BionicsSCAN.database.PendingTransactionEntity
import com.bionics.BionicsSCAN.network.*

/**
 * Generates an engineering specification description if one is not provided by the backend.
 */
fun formatBeltDescription(type: InventoryType, size: Int): String {
    return when (type) {
        InventoryType.BELT_9MM -> {
            val teeth = if (size % 5 == 0) " (${size / 5}T)" else ""
            "${size}mm Pitch Length$teeth • 5mm HTD Pitch • 9mm Width"
        }
        InventoryType.BELT_15MM -> {
            val teeth = if (size % 5 == 0) " (${size / 5}T)" else ""
            "${size}mm Pitch Length$teeth • 5mm HTD Pitch • 15mm Width"
        }
        InventoryType.GEAR -> "${size}T Metal Spur Gear • 20 DP • 1/2\" Hex Bore"
        InventoryType.SPROCKET -> "${size}T Plate Sprocket • #25 Chain Pitch • 1/2\" Hex Bore"
    }
}

// Mapping from DTO to Entity
fun InventoryPartDto.toEntity(): InventoryEntity {
    val typeEnum = try {
        InventoryType.valueOf(metadata.inventoryType)
    } catch (_: Exception) {
        InventoryType.BELT_9MM
    }

    val finalDesc = if (description.isNotBlank()) {
        description
    } else {
        formatBeltDescription(typeEnum, metadata.size)
    }

    return InventoryEntity(
        id = id,
        length = metadata.size,
        quantity = quantity,
        barcode = mfgPartNumber,
        inventoryType = metadata.inventoryType,
        description = finalDesc,
        lastUpdated = System.currentTimeMillis()
    )
}

// Mapping from Entity to UI Model
fun InventoryEntity.toBelt(): Belt {
    val inventoryTypeEnum = try {
        InventoryType.valueOf(inventoryType)
    } catch (_: Exception) {
        InventoryType.BELT_9MM 
    }
    
    val finalDesc = if (description.isNotBlank()) {
        description
    } else {
        formatBeltDescription(inventoryTypeEnum, length)
    }

    return Belt(
        id = id,
        length = length,
        quantity = quantity,
        barcode = barcode,
        inventoryType = inventoryTypeEnum,
        description = finalDesc,
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
