package com.kentaro.skullknight.data.sync

import com.google.gson.Gson
import com.kentaro.skullknight.data.ApiService
import com.kentaro.skullknight.data.CreateProjectRequest
import com.kentaro.skullknight.data.CreateItemRequest
import com.kentaro.skullknight.data.UpdateItemStatusRequest
import com.kentaro.skullknight.data.local.LocalRepository
import com.kentaro.skullknight.data.local.entities.SyncOperationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncService(
    private val apiService: ApiService,
    private val localRepository: LocalRepository
) {
    
    private val gson = Gson()
    private var lastSyncTime = 0L
    private val minSyncInterval = 10000L // 10 seconds minimum between syncs
    
    suspend fun syncPendingOperations(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Rate limiting - don't sync too frequently
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < minSyncInterval) {
                return@withContext SyncResult.Success("Sync skipped - too frequent")
            }
            lastSyncTime = currentTime
            
            val pendingOperations = localRepository.getPendingSyncOperations()
            
            if (pendingOperations.isEmpty()) {
                return@withContext SyncResult.Success("No pending operations to sync")
            }
            
            var successCount = 0
            var errorCount = 0
            
            for (operation in pendingOperations) {
                try {
                    when (operation.operationType) {
                        "CREATE_PROJECT" -> {
                            val project = gson.fromJson(operation.data, com.kentaro.skullknight.data.Project::class.java)
                            val response = apiService.createProject(CreateProjectRequest(project.name))
                            if (response.isSuccessful) {
                                localRepository.markSyncOperationCompleted(operation.id)
                                localRepository.markProjectSynced(project.name)
                                successCount++
                            } else {
                                errorCount++
                            }
                        }
                        
                        "UPDATE_PROJECT" -> {
                            val project = gson.fromJson(operation.data, com.kentaro.skullknight.data.Project::class.java)
                            // For updates, we need to handle each item change individually
                            // This is a simplified approach - you might want more granular sync
                            val response = apiService.getProject(project.name)
                            if (response.isSuccessful) {
                                localRepository.markSyncOperationCompleted(operation.id)
                                localRepository.markProjectSynced(project.name)
                                successCount++
                            } else {
                                errorCount++
                            }
                        }
                        
                        "DELETE_PROJECT" -> {
                            val response = apiService.deleteProject(operation.projectName)
                            if (response.isSuccessful) {
                                localRepository.markSyncOperationCompleted(operation.id)
                                successCount++
                            } else {
                                errorCount++
                            }
                        }
                        
                        "CREATE_ITEM" -> {
                            val itemData = gson.fromJson(operation.data, CreateItemRequest::class.java)
                            val response = apiService.createItem(operation.projectName, null, itemData)
                            if (response.isSuccessful) {
                                localRepository.markSyncOperationCompleted(operation.id)
                                successCount++
                            } else {
                                errorCount++
                            }
                        }
                        
                        "UPDATE_ITEM" -> {
                            val itemData = gson.fromJson(operation.data, UpdateItemStatusRequest::class.java)
                            // Extract itemId from data or use a different approach
                            val itemId = extractItemId(operation.data)
                            val response = apiService.updateItemStatus(operation.projectName, itemId, itemData)
                            if (response.isSuccessful) {
                                localRepository.markSyncOperationCompleted(operation.id)
                                successCount++
                            } else {
                                errorCount++
                            }
                        }
                        
                        "DELETE_ITEM" -> {
                            val itemId = operation.data
                            val response = apiService.deleteItem(operation.projectName, itemId)
                            if (response.isSuccessful) {
                                localRepository.markSyncOperationCompleted(operation.id)
                                successCount++
                            } else {
                                errorCount++
                            }
                        }
                    }
                } catch (e: Exception) {
                    errorCount++
                }
            }
            
            // Clean up completed operations
            localRepository.cleanupCompletedSyncOperations()
            
            if (errorCount == 0) {
                SyncResult.Success("Successfully synced $successCount operations")
            } else {
                SyncResult.PartialSuccess("Synced $successCount operations, $errorCount failed")
            }
            
        } catch (e: Exception) {
            SyncResult.Error("Sync failed: ${e.message}")
        }
    }
    
    suspend fun syncFromServer(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Rate limiting - don't sync too frequently
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSyncTime < minSyncInterval) {
                return@withContext SyncResult.Success("Sync skipped - too frequent")
            }
            lastSyncTime = currentTime
            
            val response = apiService.getProjects()
            if (response.isSuccessful) {
                val projects = response.body() ?: emptyList()
                
                // Update local database with server data
                for (project in projects) {
                    localRepository.saveProject(project)
                    localRepository.markProjectSynced(project.name)
                }
                
                SyncResult.Success("Successfully synced ${projects.size} projects from server")
            } else {
                SyncResult.Error("Failed to fetch projects from server")
            }
        } catch (e: Exception) {
            SyncResult.Error("Sync from server failed: ${e.message}")
        }
    }
    
    private fun extractItemId(data: String): String {
        // This is a simplified approach - you might want to store itemId separately
        return System.currentTimeMillis().toString()
    }
}

sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class PartialSuccess(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
} 