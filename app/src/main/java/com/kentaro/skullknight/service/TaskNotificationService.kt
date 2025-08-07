package com.kentaro.skullknight.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kentaro.skullknight.MainActivity
import com.kentaro.skullknight.R
import com.kentaro.skullknight.di.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskNotificationService : Service() {
    
    companion object {
        private const val CHANNEL_ID = "task_notification_channel"
        private const val NOTIFICATION_ID = 1
        private const val UPDATE_INTERVAL = 60000L // 1 minute for more responsive updates
        
        fun startService(context: Context) {
            val intent = Intent(context, TaskNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, TaskNotificationService::class.java)
            context.stopService(intent)
        }
        
        fun updateNotification(context: Context) {
            val intent = Intent(context, TaskNotificationService::class.java).apply {
                action = "UPDATE_NOTIFICATION"
            }
            context.startService(intent)
        }
    }
    
    private val hybridRepository = NetworkModule.getHybridRepository(this)
    private lateinit var notificationManager: NotificationManager
    private var updateJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "UPDATE_NOTIFICATION" -> {
                // Manual update triggered
                CoroutineScope(Dispatchers.IO).launch {
                    updateNotification()
                }
            }
            else -> {
                // Normal service start
                startForeground(NOTIFICATION_ID, createNotification(0))
                startPeriodicUpdate()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows pending task count"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(pendingTaskCount: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (pendingTaskCount == 0) {
            "All tasks completed! 🎉"
        } else {
            "$pendingTaskCount pending task${if (pendingTaskCount == 1) "" else "s"}"
        }
        
        val content = if (pendingTaskCount == 0) {
            "Great job! All your tasks are done."
        } else {
            "Tap to view your tasks"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun startPeriodicUpdate() {
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                updateNotification()
                delay(UPDATE_INTERVAL)
            }
        }
    }
    
    private suspend fun updateNotification() {
        try {
            val pendingCount = getPendingTaskCount()
            val notification = createNotification(pendingCount)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Handle error silently to avoid crashing the service
        }
    }
    
    private suspend fun getPendingTaskCount(): Int {
        return try {
            // First try to sync with server to get latest data
            try {
                val syncResult = hybridRepository.sync()
                if (syncResult is com.kentaro.skullknight.data.sync.SyncResult.Success) {
                    // Sync successful, now get the updated count
                }
            } catch (e: Exception) {
                // If sync fails, continue with local data
            }
            
            // Get projects and count pending tasks
            val projects = hybridRepository.getProjects().first() // Get first emission from Flow
            projects.sumOf { project ->
                countPendingTasks(project.items)
            }
        } catch (e: Exception) {
            0
        }
    }
    
    private fun countPendingTasks(items: List<com.kentaro.skullknight.data.Item>): Int {
        var count = 0
        for (item in items) {
            if (item.status != "Completed") {
                count++
            }
            count += countPendingTasks(item.items)
        }
        return count
    }
} 