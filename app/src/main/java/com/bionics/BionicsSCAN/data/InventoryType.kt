package com.bionics.BionicsSCAN.data

enum class InventoryType(val displayName: String, val sheetName: String) {
    BELT_9MM("9mm Belts", "'Belt Inventory 9mm'"),
    BELT_15MM("15mm Belts", "'Belt Inventory 15mm'"),
    GEAR("Gears", "'Gear Inventory'"),
    SPROCKET("Sprockets", "'Sprocket Inventory'")
}
