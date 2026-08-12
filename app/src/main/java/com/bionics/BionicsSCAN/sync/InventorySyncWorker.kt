package com.bionics.BionicsSCAN.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log
import com.bionics.BionicsSCAN.data.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventorySyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val repository = InventoryRepository(context)
    
    override suspend fun doWork(): Result {
        Log.d("InventorySyncWorker", "Work started")
        return withContext(Dispatchers.IO) {
            try {
                // 1. Process all pending transactions first
                var nextTransaction = repository.getNextPendingTransaction()
                while (nextTransaction != null) {
                    Log.d("InventorySyncWorker", "Processing transaction: ${nextTransaction.id}")
                    val postResult = repository.postTransaction(nextTransaction)
                    if (postResult.isSuccess) {
                        Log.d("InventorySyncWorker", "Transaction success, deleting")
                        repository.deletePendingTransaction(nextTransaction.id)
                        nextTransaction = repository.getNextPendingTransaction()
                    } else {
                        Log.w("InventorySyncWorker", "Transaction failed, will retry")
                        return@withContext Result.retry()
                    }
                }
                
                // 2. Once all pending transactions are cleared, refresh full inventory
                Log.d("InventorySyncWorker", "All transactions processed, reconciling...")
                val syncResult = repository.reconcileInventory()
                if (syncResult.isSuccess) {
                    Log.d("InventorySyncWorker", "Sync complete")
                    Result.success()
                } else {
                    Log.w("InventorySyncWorker", "Reconciliation failed")
                    Result.retry()
                }
            } catch (e: Exception) {
                Log.e("InventorySyncWorker", "Fatal error", e)
                Result.retry()
            }
        }
    }
    
    companion object {
        const val WORK_NAME = "inventory_sync_worker"
    }
}
