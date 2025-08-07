package com.kentaro.skullknight.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kentaro.skullknight.ui.ProjectDetailViewModel
import com.kentaro.skullknight.ui.ProjectListViewModel
import com.kentaro.skullknight.ui.screens.ProjectDetailScreen
import com.kentaro.skullknight.ui.screens.ProjectListScreen

sealed class Screen(val route: String) {
    object ProjectList : Screen("projectList")
    object ProjectDetail : Screen("projectDetail/{projectName}") {
        fun createRoute(projectName: String) = "projectDetail/$projectName"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = Screen.ProjectList.route
    ) {
        composable(Screen.ProjectList.route) {
            val viewModel: ProjectListViewModel = viewModel {
                ProjectListViewModel(context)
            }
            val uiState by viewModel.uiState.collectAsState()
            
            ProjectListScreen(
                uiState = uiState,
                onProjectClick = { project ->
                    // Use project name as identifier since API doesn't return project IDs
                    navController.navigate(Screen.ProjectDetail.createRoute(project.name))
                },
                onCreateProject = { name ->
                    viewModel.createProject(name)
                },
                onDeleteProject = { projectName ->
                    viewModel.deleteProject(projectName)
                },
                onRefresh = {
                    viewModel.loadProjects()
                },
                onSync = {
                    viewModel.sync()
                }
            )
        }
        
        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectName = backStackEntry.arguments?.getString("projectName") ?: return@composable
            
            val viewModel: ProjectDetailViewModel = viewModel {
                ProjectDetailViewModel(context, projectName)
            }
            val uiState by viewModel.uiState.collectAsState()
            
            ProjectDetailScreen(
                uiState = uiState,
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateItem = { name, parentId ->
                    viewModel.createItem(name, parentId)
                },
                onUpdateItemStatus = { item, status, reason ->
                    viewModel.updateItemStatus(item.id, status, reason)
                },
                onDeleteItem = { item ->
                    viewModel.deleteItem(item.id)
                },
                onRefresh = {
                    viewModel.loadProject()
                }
            )
        }
    }
} 