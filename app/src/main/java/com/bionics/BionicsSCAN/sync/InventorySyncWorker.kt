package com.bionics.BionicsSCAN.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bionics.BionicsSCAN.data.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventorySyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val repository = InventoryRepository(context)
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Process all pending transactions first
                var nextTransaction = repository.getNextPendingTransaction()
                while (nextTransaction != null) {
                    val postResult = repository.postTransaction(nextTransaction)
                    if (postResult.isSuccess) {
                        repository.deletePendingTransaction(nextTransaction.id)
                        nextTransaction = repository.getNextPendingTransaction()
                    } else {
                        // If one fails, we retry later. 
                        // We shouldn't proceed with full refresh if some deltas are unsent.
                        return@withContext Result.retry()
                    }
                }
                
                // 2. Once all pending transactions are cleared, refresh full inventory
                val syncResult = repository.reconcileInventory()
                if (syncResult.isSuccess) {
                    Result.success()
                } else {
                    Result.retry()
                }
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
    
    companion object {
        const val WORK_NAME = "inventory_sync_worker"
    }
}
