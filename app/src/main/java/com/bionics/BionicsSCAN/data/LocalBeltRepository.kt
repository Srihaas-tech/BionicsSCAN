package com.bionics.BionicsSCAN.data

import com.bionics.BionicsSCAN.service.SheetsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalBeltRepository {
    
    private val initialBelts = listOf(
        Belt("1", 180, 2, "180"),
        Belt("2", 225, 5, "225"),
        Belt("3", 230, 7, "230"),
        Belt("4", 235, 2, "235"),
        Belt("5", 240, 4, "240"),
        Belt("6", 245, 6, "245"),
        Belt("7", 250, 2, "250"),
        Belt("8", 255, 5, "255"),
        Belt("9", 275, 6, "275"),
        Belt("10", 285, 3, "285"),
        Belt("11", 300, 2, "300"),
        Belt("12", 320, 3, "320"),
        Belt("13", 325, 12, "325"),
        Belt("14", 350, 4, "350"),
        Belt("15", 355, 5, "355"),
        Belt("16", 375, 8, "375"),
        Belt("17", 400, 11, "400"),
        Belt("18", 425, 7, "425"),
        Belt("19", 450, 3, "450"),
        Belt("20", 475, 12, "475"),
        Belt("21", 525, 6, "525"),
        Belt("22", 540, 2, "540"),
        Belt("23", 550, 2, "550"),
        Belt("24", 575, 6, "575"),
        Belt("25", 625, 2, "625"),
        Belt("26", 645, 1, "645"),
        Belt("27", 700, 4, "700"),
        Belt("28", 720, 6, "720"),
        Belt("29", 750, 3, "750"),
        Belt("30", 1125, 2, "1125"),
        Belt("31", 1200, 3, "1200"),
        Belt("32", 1250, 3, "1250")
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
