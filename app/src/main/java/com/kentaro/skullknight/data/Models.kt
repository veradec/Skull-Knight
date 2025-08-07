package com.kentaro.skullknight.data

import com.google.gson.annotations.SerializedName

data class Project(
    val id: String? = null,
    val name: String,
    val items: List<Item> = emptyList()
)

data class Item(
    val id: String,
    val name: String,
    val status: String = "Not Initiated",
    val reason: String? = null,
    val items: List<Item> = emptyList()
)

data class CreateProjectRequest(
    val name: String,
    val items: List<Item> = emptyList()
)

data class CreateItemRequest(
    val name: String,
    val items: List<Item> = emptyList()
)

data class UpdateItemStatusRequest(
    val status: String,
    val reason: String? = null
)

enum class ItemStatus(val displayName: String, val color: Long) {
    NOT_INITIATED("Not Initiated", 0xFF808080), // Grey
    IN_PROGRESS("In Progress", 0xFF5CB85C),     // Green
    COMPLETED("Completed", 0xFFF0AD4E),         // Orange
    NEAR_COMPLETE("Near Complete", 0xFF5BC0DE)  // Light Blue
} 