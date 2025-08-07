package com.kentaro.skullknight.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kentaro.skullknight.data.Project
import com.kentaro.skullknight.di.NetworkModule
import com.kentaro.skullknight.service.TaskNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectListViewModel(context: Context) : ViewModel() {
    
    private val hybridRepository = NetworkModule.getHybridRepository(context)
    private val appContext = context.applicationContext
    
    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()
    
    init {
        loadProjects()
        loadInitialDataFromServer()
    }
    
    private fun loadInitialDataFromServer() {
        viewModelScope.launch {
            // Only sync from server once when app starts
            try {
                hybridRepository.sync()
            } catch (e: Exception) {
                // Ignore errors, continue with local data
            }
        }
    }
    
    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Only load from local database, no automatic syncing
                hybridRepository.getProjects().collect { projects ->
                    _uiState.value = _uiState.value.copy(
                        projects = projects,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun createProject(name: String) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hybridRepository.createProject(name)
                .onSuccess {
                    // Projects will be updated automatically through Flow
                    // Update notification
                    TaskNotificationService.updateNotification(appContext)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create project"
                    )
                }
        }
    }
    
    fun deleteProject(projectName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hybridRepository.deleteProject(projectName)
                .onSuccess {
                    // Projects will be updated automatically through Flow
                    // Update notification
                    TaskNotificationService.updateNotification(appContext)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete project"
                    )
                }
        }
    }
    
    fun sync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = hybridRepository.sync()
            when (result) {
                is com.kentaro.skullknight.data.sync.SyncResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                is com.kentaro.skullknight.data.sync.SyncResult.PartialSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is com.kentaro.skullknight.data.sync.SyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ProjectListUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 