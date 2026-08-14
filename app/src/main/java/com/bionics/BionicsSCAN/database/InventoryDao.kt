package com.bionics.BionicsSCAN.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    
    @Query("SELECT * FROM inventory")
    fun getAllInventory(): Flow<List<InventoryEntity>>
    
    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getInventoryById(id: String): InventoryEntity?
    
    @Query("SELECT * FROM inventory WHERE barcode = :barcode")
    suspend fun getInventoryByBarcode(barcode: String): InventoryEntity?

    @Query("SELECT * FROM inventory WHERE UPPER(REPLACE(REPLACE(barcode, '-', ''), ' ', '')) = UPPER(REPLACE(REPLACE(:normalizedBarcode, '-', ''), ' ', '')) LIMIT 1")
    suspend fun getInventoryByNormalizedBarcode(normalizedBarcode: String): InventoryEntity?
    
    @Query("SELECT * FROM inventory WHERE inventoryType = :type")
    fun getInventoryByType(type: String): Flow<List<InventoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inventory: List<InventoryEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: InventoryEntity)
    
    @Update
    suspend fun update(inventory: InventoryEntity)
    
    @Query("UPDATE inventory SET quantity = quantity + :delta WHERE id = :id")
    suspend fun updateQuantity(id: String, delta: Int)
    
    @Query("DELETE FROM inventory")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun getCount(): Int
}
