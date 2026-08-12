package com.bionics.BionicsSCAN

import android.app.Application
import com.bionics.BionicsSCAN.data.InventoryRepository
import com.bionics.BionicsSCAN.sync.InventorySyncScheduler

class BionicsScanApplication : Application() {
    
    lateinit var inventoryRepository: InventoryRepository
        private set
        
    lateinit var syncScheduler: InventorySyncScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        
        inventoryRepository = InventoryRepository(this)
        syncScheduler = InventorySyncScheduler(this)
    }
}
