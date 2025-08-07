package com.kentaro.skullknight.data

import com.kentaro.skullknight.data.local.LocalRepository
import com.kentaro.skullknight.data.sync.SyncService
import com.kentaro.skullknight.data.sync.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class HybridRepository(
    private val localRepository: LocalRepository,
    private val syncService: SyncService,
    private val isOnline: () -> Boolean
) {
    
    // Get all projects - prioritize local data, sync in background
    fun getProjects(): Flow<List<Project>> {
        return localRepository.getAllProjects().map { localProjects ->
            // Return local data immediately, no aggressive background syncing
            localProjects
        }.catch { e ->
            // If local database fails, return empty list
            emit(emptyList<Project>())
        }
    }
    
    // Get a specific project
    suspend fun getProject(projectName: String): Project? {
        return localRepository.getProject(projectName)
    }
    
    // Create project - save locally first, then sync
    suspend fun createProject(name: String): Result<Project> {
        return try {
            val project = localRepository.createProject(name)
            
            // Don't sync immediately to prevent excessive requests
            // Sync will happen manually or through periodic sync
            
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete project - delete locally first, then sync
    suspend fun deleteProject(projectName: String): Result<Unit> {
        return try {
            localRepository.deleteProject(projectName)
            
            // Don't sync immediately to prevent excessive requests
            // Sync will happen manually or through periodic sync
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Create item - save locally first, then sync
    suspend fun createItem(projectName: String, name: String, parentId: String? = null): Result<Unit> {
        return try {
            localRepository.createItem(projectName, name, parentId)
            
            // Don't sync immediately to prevent excessive requests
            // Sync will happen manually or through periodic sync
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update item status - save locally first, then sync
    suspend fun updateItemStatus(projectName: String, itemId: String, status: String, reason: String? = null): Result<Unit> {
        return try {
            localRepository.updateItemStatus(projectName, itemId, status, reason)
            
            // Don't sync immediately to prevent excessive requests
            // Sync will happen manually or through periodic sync
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete item - delete locally first, then sync
    suspend fun deleteItem(projectName: String, itemId: String): Result<Unit> {
        return try {
            localRepository.deleteItem(projectName, itemId)
            
            // Don't sync immediately to prevent excessive requests
            // Sync will happen manually or through periodic sync
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Manual sync - can be called by user or periodically
    suspend fun sync(): SyncResult {
        return if (isOnline()) {
            try {
                // First sync from server to get latest data
                val serverSyncResult = syncService.syncFromServer()
                
                // Then sync pending operations to server
                val pendingSyncResult = syncService.syncPendingOperations()
                
                when {
                    serverSyncResult is SyncResult.Success && pendingSyncResult is SyncResult.Success -> {
                        SyncResult.Success("Sync completed successfully")
                    }
                    serverSyncResult is SyncResult.Error -> {
                        serverSyncResult
                    }
                    pendingSyncResult is SyncResult.Error -> {
                        pendingSyncResult
                    }
                    else -> {
                        SyncResult.PartialSuccess("Sync completed with some issues")
                    }
                }
            } catch (e: Exception) {
                SyncResult.Error("Sync failed: ${e.message}")
            }
        } else {
            SyncResult.Error("No internet connection")
        }
    }
    
    // Get sync status
    suspend fun getPendingSyncCount(): Int {
        return localRepository.getPendingSyncOperations().size
    }
} 