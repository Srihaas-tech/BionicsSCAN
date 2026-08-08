package com.bionics.BionicsSCAN.data

import com.bionics.BionicsSCAN.service.SheetsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalBeltRepository {
    
    private val initialBelts = listOf(
        // 9mm Belts
        Belt("1", 180, 2, "B9-180", InventoryType.BELT_9MM),
        Belt("2", 225, 5, "B9-225", InventoryType.BELT_9MM),
        Belt("3", 230, 7, "B9-230", InventoryType.BELT_9MM),
        Belt("4", 235, 2, "B9-235", InventoryType.BELT_9MM),
        Belt("5", 240, 4, "B9-240", InventoryType.BELT_9MM),
        Belt("6", 245, 6, "B9-245", InventoryType.BELT_9MM),
        Belt("7", 250, 2, "B9-250", InventoryType.BELT_9MM),
        Belt("8", 255, 5, "B9-255", InventoryType.BELT_9MM),
        Belt("9", 275, 6, "B9-275", InventoryType.BELT_9MM),
        Belt("10", 285, 3, "B9-285", InventoryType.BELT_9MM),
        Belt("11", 300, 2, "B9-300", InventoryType.BELT_9MM),
        Belt("12", 320, 3, "B9-320", InventoryType.BELT_9MM),
        Belt("13", 325, 12, "B9-325", InventoryType.BELT_9MM),
        Belt("14", 350, 4, "B9-350", InventoryType.BELT_9MM),
        Belt("15", 355, 5, "B9-355", InventoryType.BELT_9MM),
        Belt("16", 375, 8, "B9-375", InventoryType.BELT_9MM),
        Belt("17", 400, 11, "B9-400", InventoryType.BELT_9MM),
        Belt("18", 425, 7, "B9-425", InventoryType.BELT_9MM),
        Belt("19", 450, 3, "B9-450", InventoryType.BELT_9MM),
        Belt("20", 475, 12, "B9-475", InventoryType.BELT_9MM),
        Belt("21", 525, 6, "B9-525", InventoryType.BELT_9MM),
        Belt("22", 540, 2, "B9-540", InventoryType.BELT_9MM),
        Belt("23", 550, 2, "B9-550", InventoryType.BELT_9MM),
        Belt("24", 575, 6, "B9-575", InventoryType.BELT_9MM),
        Belt("25", 625, 2, "B9-625", InventoryType.BELT_9MM),
        Belt("26", 645, 1, "B9-645", InventoryType.BELT_9MM),
        Belt("27", 700, 4, "B9-700", InventoryType.BELT_9MM),
        Belt("28", 720, 6, "B9-720", InventoryType.BELT_9MM),
        Belt("29", 750, 3, "B9-750", InventoryType.BELT_9MM),
        Belt("30", 1125, 2, "B9-1125", InventoryType.BELT_9MM),
        Belt("31", 1200, 3, "B9-1200", InventoryType.BELT_9MM),
        Belt("32", 1250, 4, "B9-1250", InventoryType.BELT_9MM),
        
        // 15mm Belts
        Belt("101", 250, 3, "b15-250", InventoryType.BELT_15MM),
        Belt("102", 320, 12, "b15-320", InventoryType.BELT_15MM),
        Belt("103", 345, 4, "b15-345", InventoryType.BELT_15MM),
        Belt("104", 350, 2, "b15-350", InventoryType.BELT_15MM),
        Belt("105", 355, 2, "b15-355", InventoryType.BELT_15MM),
        Belt("106", 360, 1, "b15-360", InventoryType.BELT_15MM),
        Belt("107", 365, 2, "b15-365", InventoryType.BELT_15MM),
        Belt("108", 370, 1, "b15-370", InventoryType.BELT_15MM),
        Belt("109", 400, 5, "b15-400", InventoryType.BELT_15MM),
        Belt("110", 425, 1, "b15-425", InventoryType.BELT_15MM),
        Belt("111", 450, 1, "b15-450", InventoryType.BELT_15MM),
        Belt("112", 520, 4, "b15-520", InventoryType.BELT_15MM),
        Belt("113", 585, 4, "b15-585", InventoryType.BELT_15MM),
        Belt("114", 590, 2, "b15-590", InventoryType.BELT_15MM),
        Belt("115", 600, 8, "b15-600", InventoryType.BELT_15MM),
        Belt("116", 625, 4, "b15-625", InventoryType.BELT_15MM),
        Belt("117", 655, 29, "b15-655", InventoryType.BELT_15MM),
        Belt("118", 695, 1, "b15-695", InventoryType.BELT_15MM),
        Belt("119", 700, 2, "b15-700", InventoryType.BELT_15MM),
        Belt("120", 750, 4, "b15-750", InventoryType.BELT_15MM),
        Belt("121", 755, 10, "b15-755", InventoryType.BELT_15MM),
        Belt("122", 800, 8, "b15-800", InventoryType.BELT_15MM),
        Belt("123", 850, 7, "b15-850", InventoryType.BELT_15MM),
        Belt("124", 1125, 3, "b15-1125", InventoryType.BELT_15MM),
        Belt("125", 1200, 3, "b15-1200", InventoryType.BELT_15MM),
        Belt("126", 1250, 3, "b15-1250", InventoryType.BELT_15MM),
        Belt("127", 1295, 2, "b15-1295", InventoryType.BELT_15MM),
        Belt("128", 1870, 2, "b15-1870", InventoryType.BELT_15MM),
        Belt("129", 3120, 5, "b15-3120", InventoryType.BELT_15MM),
        
        // Gears
        Belt("201", 84, 3, "GR-84", InventoryType.GEAR),
        Belt("202", 80, 2, "GR-80", InventoryType.GEAR),
        Belt("203", 76, 2, "GR-76", InventoryType.GEAR),
        Belt("204", 72, 10, "GR-72", InventoryType.GEAR),
        Belt("205", 64, 1, "GR-64", InventoryType.GEAR),
        Belt("206", 60, 1, "GR-60", InventoryType.GEAR),
        Belt("207", 56, 1, "GR-56", InventoryType.GEAR),
        Belt("208", 54, 2, "GR-54", InventoryType.GEAR),
        Belt("209", 52, 3, "GR-52", InventoryType.GEAR),
        Belt("210", 50, 3, "GR-50", InventoryType.GEAR),
        Belt("211", 48, 14, "GR-48", InventoryType.GEAR),
        Belt("212", 45, 2, "GR-45", InventoryType.GEAR),
        Belt("213", 44, 4, "GR-44", InventoryType.GEAR),
        Belt("214", 42, 2, "GR-42", InventoryType.GEAR),
        Belt("215", 30, 4, "GR-30", InventoryType.GEAR),
        Belt("216", 26, 2, "GR-26", InventoryType.GEAR),
        Belt("217", 24, 6, "GR-24", InventoryType.GEAR),
        Belt("218", 22, 6, "GR-22", InventoryType.GEAR),
        
        // Sprockets
        Belt("301", 16, 12, "SP-16", InventoryType.SPROCKET),
        Belt("302", 24, 4, "SP-24", InventoryType.SPROCKET),
        Belt("303", 32, 7, "SP-32", InventoryType.SPROCKET)
    )
    
    private val belts = mutableMapOf<String, Belt>()
    
    init {
        initialBelts.forEach { belt ->
            belts[belt.id] = belt
        }
    }
    
    fun getAllBelts(): Flow<List<Belt>> = flow {
        emit(belts.values.toList())
    }
    
    fun getAllBeltsList(): List<Belt> = belts.values.toList()
    
    fun getBeltById(id: String): Belt? = belts[id]
    
    fun getBeltByBarcode(barcode: String): Belt? {
        return belts.values.find { it.barcode == barcode }
    }
    
    fun updateBeltQuantity(beltId: String, newQuantity: Int) {
        belts[beltId]?.let { belt ->
            belts[beltId] = belt.copy(quantity = newQuantity)
        }
    }
    
    fun checkoutBelt(beltId: String): Boolean {
        val belt = belts[beltId] ?: return false
        if (belt.quantity > 0) {
            belts[beltId] = belt.copy(quantity = belt.quantity - 1)
            return true
        }
        return false
    }
    
    fun checkinBelt(beltId: String): Boolean {
        val belt = belts[beltId] ?: return false
        belts[beltId] = belt.copy(quantity = belt.quantity + 1)
        return true
    }
}
