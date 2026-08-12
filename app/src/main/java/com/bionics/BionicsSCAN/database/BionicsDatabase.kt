package com.bionics.BionicsSCAN.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InventoryEntity::class, PendingTransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BionicsDatabase : RoomDatabase() {
    
    abstract fun inventoryDao(): InventoryDao
    abstract fun pendingTransactionDao(): PendingTransactionDao
    
    companion object {
        @Volatile
        private var INSTANCE: BionicsDatabase? = null
        
        fun getDatabase(context: Context): BionicsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BionicsDatabase::class.java,
                    "bionics_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
