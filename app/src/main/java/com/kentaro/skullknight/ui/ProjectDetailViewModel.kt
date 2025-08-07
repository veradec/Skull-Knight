package com.kentaro.skullknight.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kentaro.skullknight.data.Item
import com.kentaro.skullknight.data.Project
import com.kentaro.skullknight.di.NetworkModule
import com.kentaro.skullknight.service.TaskNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    context: Context,
    private val projectName: String
) : ViewModel() {
    
    private val hybridRepository = NetworkModule.getHybridRepository(context)
    private val appContext = context.applicationContext
    
    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadProject()
    }
    
    fun loadProject() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val project = hybridRepository.getProject(projectName)
                if (project != null) {
                    _uiState.value = _uiState.value.copy(
                        project = project,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Project not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load project"
                )
            }
        }
    }
    
    fun createItem(name: String, parentId: String? = null) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hybridRepository.createItem(projectName, name, parentId)
                .onSuccess {
                    loadProject() // Reload the project
                    // Update notification
                    TaskNotificationService.updateNotification(appContext)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create item"
                    )
                }
        }
    }
    
    fun updateItemStatus(itemId: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hybridRepository.updateItemStatus(projectName, itemId, status, reason)
                .onSuccess {
                    loadProject() // Reload the project
                    // Update notification
                    TaskNotificationService.updateNotification(appContext)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update item status"
                    )
                }
        }
    }
    
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hybridRepository.deleteItem(projectName, itemId)
                .onSuccess {
                    loadProject() // Reload the project
                    // Update notification
                    TaskNotificationService.updateNotification(appContext)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete item"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ProjectDetailUiState(
    val project: Project? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 