package com.kentaro.skullknight

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.kentaro.skullknight.service.TaskNotificationService
import com.kentaro.skullknight.ui.navigation.AppNavigation
import com.kentaro.skullknight.ui.theme.SkullknightTheme

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result will be handled in LaunchedEffect
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, service will be started in LaunchedEffect
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // For older Android versions, service will be started in LaunchedEffect
        
        setContent {
            SkullknightTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                    
                    // Start the notification service after the UI is loaded
                    LaunchedEffect(Unit) {
                        delay(1000) // Wait 1 second for the app to fully load
                        
                        // Check if we have notification permission
                        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true // No permission needed for older Android versions
                        }
                        
                        if (hasPermission) {
                            startTaskNotificationService()
                        }
                    }
                }
            }
        }
    }
    
    private fun startTaskNotificationService() {
        TaskNotificationService.startService(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Keep the service running even when the app is closed
        // TaskNotificationService.stopService(this)
    }
}