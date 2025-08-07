package com.kentaro.skullknight.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String, // CREATE_PROJECT, UPDATE_PROJECT, DELETE_PROJECT, CREATE_ITEM, UPDATE_ITEM, DELETE_ITEM
    val projectName: String,
    val data: String, // JSON string of the data
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) 