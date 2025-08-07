package com.kentaro.skullknight.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class Repository(private val apiService: ApiService) {
    
    suspend fun getProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProjects()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch projects: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProject(projectName: String): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProject(projectName)
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Project not found"))
            } else {
                Result.failure(Exception("Failed to fetch project: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createProject(name: String): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createProject(CreateProjectRequest(name))
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Project not found"))
            } else {
                Result.failure(Exception("Failed to create project: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProject(projectName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteProject(projectName)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete project: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createItem(projectName: String, name: String, parentId: String? = null): Result<Item> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createItem(projectName, parentId, CreateItemRequest(name))
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Item not found"))
            } else {
                Result.failure(Exception("Failed to create item: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateItemStatus(projectName: String, itemId: String, status: String, reason: String? = null): Result<Item> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateItemStatus(projectName, itemId, UpdateItemStatusRequest(status, reason))
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Item not found"))
            } else {
                Result.failure(Exception("Failed to update item status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteItem(projectName: String, itemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteItem(projectName, itemId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete item: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 