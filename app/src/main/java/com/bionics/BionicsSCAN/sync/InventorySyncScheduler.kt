package com.bionics.BionicsSCAN.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class InventorySyncScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun schedulePeriodicSync() {
        // Schedule periodic sync every 15 minutes when network is available
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val periodicWorkRequest = PeriodicWorkRequestBuilder<InventorySyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            InventorySyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
    
    fun scheduleImmediateSync() {
        // Schedule immediate sync with network constraint
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<InventorySyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "${InventorySyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
    }
    
    fun cancelAllSyncs() {
        workManager.cancelUniqueWork(InventorySyncWorker.WORK_NAME)
        workManager.cancelUniqueWork("${InventorySyncWorker.WORK_NAME}_immediate")
    }
}
