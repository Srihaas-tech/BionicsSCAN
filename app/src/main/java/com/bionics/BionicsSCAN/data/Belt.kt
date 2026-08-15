package com.bionics.BionicsSCAN.data

data class Belt(
    val id: String,
    val length: Int,
    val quantity: Int,
    val barcode: String,
    val inventoryType: InventoryType = InventoryType.BELT_9MM,
    val description: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
