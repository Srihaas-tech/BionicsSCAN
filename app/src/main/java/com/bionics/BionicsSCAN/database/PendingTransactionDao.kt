package com.bionics.BionicsSCAN.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    
    @Query("SELECT * FROM pending_transactions ORDER BY timestamp ASC")
    fun getAllPendingTransactions(): Flow<List<PendingTransactionEntity>>
    
    @Query("SELECT * FROM pending_transactions ORDER BY timestamp ASC LIMIT 1")
    suspend fun getNextPendingTransaction(): PendingTransactionEntity?
    
    @Query("SELECT * FROM pending_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): PendingTransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: PendingTransactionEntity)
    
    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM pending_transactions")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM pending_transactions")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM pending_transactions")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT * FROM pending_transactions WHERE inventoryId = :partId ORDER BY timestamp DESC")
    fun getPendingTransactionsForPart(partId: String): Flow<List<PendingTransactionEntity>>
}
