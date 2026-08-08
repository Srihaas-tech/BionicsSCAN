package com.bionics.BionicsSCAN.data

data class Belt(
    val id: String,
    val length: Int,
    val quantity: Int,
    val barcode: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
