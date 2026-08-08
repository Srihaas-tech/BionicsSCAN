package com.bionics.BionicsSCAN.data

enum class InventoryType(val displayName: String, val sheetName: String) {
    BELT_9MM("Belt 9mm", "'Belt Inventory 9mm'"),
    BELT_15MM("Belt 15mm", "'Belt Inventory 15mm'"),
    GEAR("Gear", "'Gear Inventory'"),
    SPROCKET("Sprocket", "'Sprocket Inventory'")
}
