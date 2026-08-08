package com.bionics.BionicsSCAN.data

import com.bionics.BionicsSCAN.service.SheetsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalBeltRepository {
    
    private val initialBelts = listOf(
        Belt("1", 180, 2, "BELT-180-001"),
        Belt("2", 225, 5, "BELT-225-001"),
        Belt("3", 230, 7, "BELT-230-001"),
        Belt("4", 235, 2, "BELT-235-001"),
        Belt("5", 240, 4, "BELT-240-001"),
        Belt("6", 245, 6, "BELT-245-001"),
        Belt("7", 250, 2, "BELT-250-001"),
        Belt("8", 255, 5, "BELT-255-001"),
        Belt("9", 275, 6, "BELT-275-001"),
        Belt("10", 285, 3, "BELT-285-001"),
        Belt("11", 300, 2, "BELT-300-001"),
        Belt("12", 320, 3, "BELT-320-001"),
        Belt("13", 325, 12, "BELT-325-001"),
        Belt("14", 350, 4, "BELT-350-001"),
        Belt("15", 355, 5, "BELT-355-001"),
        Belt("16", 375, 8, "BELT-375-001"),
        Belt("17", 400, 11, "BELT-400-001"),
        Belt("18", 425, 7, "BELT-425-001"),
        Belt("19", 450, 3, "BELT-450-001"),
        Belt("20", 475, 12, "BELT-475-001"),
        Belt("21", 525, 6, "BELT-525-001"),
        Belt("22", 540, 2, "BELT-540-001"),
        Belt("23", 550, 2, "BELT-550-001"),
        Belt("24", 575, 6, "BELT-575-001"),
        Belt("25", 625, 2, "BELT-625-001"),
        Belt("26", 645, 1, "BELT-645-001"),
        Belt("27", 700, 4, "BELT-700-001"),
        Belt("28", 720, 6, "BELT-720-001"),
        Belt("29", 750, 3, "BELT-750-001"),
        Belt("30", 1125, 2, "BELT-1125-001"),
        Belt("31", 1200, 3, "BELT-1200-001"),
        Belt("32", 1250, 3, "BELT-1250-001")
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
