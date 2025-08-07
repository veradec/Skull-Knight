package com.kentaro.skullknight.di

import android.content.Context
import com.kentaro.skullknight.data.ApiService
import com.kentaro.skullknight.data.Repository
import com.kentaro.skullknight.data.HybridRepository
import com.kentaro.skullknight.data.local.AppDatabase
import com.kentaro.skullknight.data.local.LocalRepository
import com.kentaro.skullknight.data.sync.SyncService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private const val BASE_URL = "http://192.168.0.147:8000"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    val repository: Repository = Repository(apiService)
    
    // Database and local storage
    fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    fun getLocalRepository(context: Context): LocalRepository {
        val database = getDatabase(context)
        return LocalRepository(database.projectDao())
    }
    
    fun getSyncService(context: Context): SyncService {
        val localRepository = getLocalRepository(context)
        return SyncService(apiService, localRepository)
    }
    
    fun getHybridRepository(context: Context): HybridRepository {
        val localRepository = getLocalRepository(context)
        val syncService = getSyncService(context)
        
        // Simple online check - you might want to implement a more sophisticated network check
        val isOnline: () -> Boolean = {
            try {
                val runtime = Runtime.getRuntime()
                val process = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
                val exitCode = process.waitFor()
                exitCode == 0
            } catch (e: Exception) {
                false
            }
        }
        
        return HybridRepository(localRepository, syncService, isOnline)
    }
} 