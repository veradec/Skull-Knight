package com.kentaro.skullknight.data.local.dao

import androidx.room.*
import com.kentaro.skullknight.data.local.entities.ProjectEntity
import com.kentaro.skullknight.data.local.entities.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    
    // Project operations
    @Query("SELECT * FROM projects WHERE isDeleted = 0")
    fun getAllProjects(): Flow<List<ProjectEntity>>
    
    @Query("SELECT * FROM projects WHERE name = :projectName AND isDeleted = 0")
    suspend fun getProject(projectName: String): ProjectEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)
    
    @Query("UPDATE projects SET isDeleted = 1 WHERE name = :projectName")
    suspend fun deleteProject(projectName: String)
    
    @Query("UPDATE projects SET isSynced = :isSynced WHERE name = :projectName")
    suspend fun updateSyncStatus(projectName: String, isSynced: Boolean)
    
    // Sync operations
    @Query("SELECT * FROM sync_operations WHERE isCompleted = 0 ORDER BY timestamp ASC")
    suspend fun getPendingSyncOperations(): List<SyncOperationEntity>
    
    @Insert
    suspend fun insertSyncOperation(operation: SyncOperationEntity)
    
    @Query("UPDATE sync_operations SET isCompleted = 1 WHERE id = :operationId")
    suspend fun markSyncOperationCompleted(operationId: Long)
    
    @Query("DELETE FROM sync_operations WHERE isCompleted = 1")
    suspend fun deleteCompletedSyncOperations()
} 