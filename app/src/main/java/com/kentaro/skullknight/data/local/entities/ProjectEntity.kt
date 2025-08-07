package com.kentaro.skullknight.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val name: String,
    val items: String, // JSON string of items
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) 