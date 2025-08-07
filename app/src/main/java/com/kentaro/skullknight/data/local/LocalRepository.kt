package com.kentaro.skullknight.data.local

import com.google.gson.Gson
import com.kentaro.skullknight.data.Item
import com.kentaro.skullknight.data.Project
import com.kentaro.skullknight.data.local.dao.ProjectDao
import com.kentaro.skullknight.data.local.entities.ProjectEntity
import com.kentaro.skullknight.data.local.entities.SyncOperationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRepository(private val projectDao: ProjectDao) {
    
    private val gson = Gson()
    
    // Convert Project to ProjectEntity
    private fun projectToEntity(project: Project, isSynced: Boolean = false): ProjectEntity {
        return ProjectEntity(
            name = project.name,
            items = gson.toJson(project.items),
            isSynced = isSynced,
            lastModified = System.currentTimeMillis()
        )
    }
    
    // Convert ProjectEntity to Project
    private fun entityToProject(entity: ProjectEntity): Project {
        return Project(
            id = entity.name, // Use name as ID for now
            name = entity.name,
            items = gson.fromJson(entity.items, Array<Item>::class.java).toList()
        )
    }
    
    // Get all projects from local database
    fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { entityToProject(it) }
        }
    }
    
    // Get a specific project from local database
    suspend fun getProject(projectName: String): Project? {
        val entity = projectDao.getProject(projectName)
        return entity?.let { entityToProject(it) }
    }
    
    // Save project locally and queue for sync
    suspend fun saveProject(project: Project) {
        val entity = projectToEntity(project, isSynced = false)
        projectDao.insertProject(entity)
        
        // Queue sync operation
        val syncOperation = SyncOperationEntity(
            operationType = "UPDATE_PROJECT",
            projectName = project.name,
            data = gson.toJson(project)
        )
        projectDao.insertSyncOperation(syncOperation)
    }
    
    // Create new project locally and queue for sync
    suspend fun createProject(name: String): Project {
        val project = Project(
            id = name,
            name = name,
            items = emptyList()
        )
        val entity = projectToEntity(project, isSynced = false)
        projectDao.insertProject(entity)
        
        // Queue sync operation
        val syncOperation = SyncOperationEntity(
            operationType = "CREATE_PROJECT",
            projectName = name,
            data = gson.toJson(project)
        )
        projectDao.insertSyncOperation(syncOperation)
        
        return project
    }
    
    // Delete project locally and queue for sync
    suspend fun deleteProject(projectName: String) {
        projectDao.deleteProject(projectName)
        
        // Queue sync operation
        val syncOperation = SyncOperationEntity(
            operationType = "DELETE_PROJECT",
            projectName = projectName,
            data = ""
        )
        projectDao.insertSyncOperation(syncOperation)
    }
    
    // Create item locally and queue for sync
    suspend fun createItem(projectName: String, name: String, parentId: String? = null) {
        val project = getProject(projectName) ?: return
        val newItem = Item(
            id = System.currentTimeMillis().toString(), // Temporary ID
            name = name,
            status = "Not Initiated",
            reason = null,
            items = emptyList()
        )
        
        // Add item to project (simplified - you might want more complex logic for nested items)
        val updatedItems = project.items.toMutableList()
        updatedItems.add(newItem)
        val updatedProject = project.copy(items = updatedItems)
        
        saveProject(updatedProject)
    }
    
    // Update item status locally and queue for sync
    suspend fun updateItemStatus(projectName: String, itemId: String, status: String, reason: String? = null) {
        val project = getProject(projectName) ?: return
        
        // Update item in project (simplified - you might want more complex logic for nested items)
        val updatedItems = project.items.map { item ->
            if (item.id == itemId) {
                item.copy(status = status, reason = reason)
            } else {
                item
            }
        }
        val updatedProject = project.copy(items = updatedItems)
        
        saveProject(updatedProject)
    }
    
    // Delete item locally and queue for sync
    suspend fun deleteItem(projectName: String, itemId: String) {
        val project = getProject(projectName) ?: return
        
        // Remove item from project (simplified - you might want more complex logic for nested items)
        val updatedItems = project.items.filter { it.id != itemId }
        val updatedProject = project.copy(items = updatedItems)
        
        saveProject(updatedProject)
    }
    
    // Get pending sync operations
    suspend fun getPendingSyncOperations(): List<SyncOperationEntity> {
        return projectDao.getPendingSyncOperations()
    }
    
    // Mark sync operation as completed
    suspend fun markSyncOperationCompleted(operationId: Long) {
        projectDao.markSyncOperationCompleted(operationId)
    }
    
    // Clean up completed sync operations
    suspend fun cleanupCompletedSyncOperations() {
        projectDao.deleteCompletedSyncOperations()
    }
    
    // Mark project as synced
    suspend fun markProjectSynced(projectName: String) {
        projectDao.updateSyncStatus(projectName, true)
    }
} 