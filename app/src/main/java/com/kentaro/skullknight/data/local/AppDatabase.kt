package com.kentaro.skullknight.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.kentaro.skullknight.data.local.dao.ProjectDao
import com.kentaro.skullknight.data.local.entities.ProjectEntity
import com.kentaro.skullknight.data.local.entities.SyncOperationEntity

@Database(
    entities = [ProjectEntity::class, SyncOperationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skullknight_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 