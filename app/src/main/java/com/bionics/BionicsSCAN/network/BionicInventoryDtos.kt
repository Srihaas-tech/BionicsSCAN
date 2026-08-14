package com.bionics.BionicsSCAN.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryResponseDto(
    @SerialName("inventory")
    val inventory: List<InventoryPartDto>
)

@Serializable
data class InventoryPartDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("mfgPartNumber")
    val mfgPartNumber: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("metadata")
    val metadata: InventoryMetadataDto,
    
    @SerialName("quantity")
    val quantity: Int
)

@Serializable
data class InventoryMetadataDto(
    @SerialName("inventoryType")
    val inventoryType: String,
    
    @SerialName("size")
    val size: Int
)

@Serializable
data class TransactionRequestDto(
    @SerialName("actor")
    val actor: String,
    
    @SerialName("note")
    val note: String? = null,
    
    @SerialName("lines")
    val lines: List<TransactionLineDto>
)

@Serializable
data class TransactionLineDto(
    @SerialName("partId")
    val partId: String,
    
    @SerialName("quantityDelta")
    val quantityDelta: Int,
    
    @SerialName("usedIn")
    val usedIn: String? = null
)

@Serializable
data class TransactionResponseDto(
    @SerialName("transaction")
    val transaction: TransactionResultDto
)

@Serializable
data class TransactionResultDto(
    @SerialName("transactionId")
    val transactionId: String,
    
    @SerialName("recordedAt")
    val recordedAt: String,
    
    @SerialName("lineCount")
    val lineCount: Int
)
