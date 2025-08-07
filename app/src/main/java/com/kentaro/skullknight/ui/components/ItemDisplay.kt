package com.kentaro.skullknight.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kentaro.skullknight.data.Item
import com.kentaro.skullknight.data.ItemStatus

@Composable
fun ItemDisplay(
    items: List<Item>,
    onItemClick: (Item) -> Unit,
    onUpdateStatus: (Item, String, String?) -> Unit,
    onDeleteItem: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ItemCard(
                item = item,
                onItemClick = onItemClick,
                onUpdateStatus = onUpdateStatus,
                onDeleteItem = onDeleteItem
            )
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onItemClick: (Item) -> Unit,
    onUpdateStatus: (Item, String, String?) -> Unit,
    onDeleteItem: (Item) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Status chip
                    val statusColor = when (item.status) {
                        "Not Initiated" -> Color(0xFF808080)
                        "In Progress" -> Color(0xFF5CB85C)
                        "Completed" -> Color(0xFFF0AD4E)
                        "Near Complete" -> Color(0xFF5BC0DE)
                        else -> Color(0xFF808080)
                    }
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = item.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = statusColor
                        )
                    }
                    
                    if (!item.reason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reason: ${item.reason}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                // Action buttons
                Row {
                    IconButton(onClick = { showStatusDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Update Status"
                        )
                    }
                    
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item"
                        )
                    }
                }
            }
            
            // Display sub-items if any
            if (item.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    item.items.forEach { subItem ->
                        ItemCard(
                            item = subItem,
                            onItemClick = onItemClick,
                            onUpdateStatus = onUpdateStatus,
                            onDeleteItem = onDeleteItem,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Status update dialog
    if (showStatusDialog) {
        StatusUpdateDialog(
            currentStatus = item.status,
            onStatusSelected = { status, reason ->
                onUpdateStatus(item, status, reason)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete '${item.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteItem(item)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusUpdateDialog(
    currentStatus: String,
    onStatusSelected: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status") },
        text = {
            Column {
                Text("Select new status:")
                Spacer(modifier = Modifier.height(8.dp))
                
                ItemStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == status.displayName,
                            onClick = { selectedStatus = status.displayName }
                        )
                        Text(status.displayName)
                    }
                }
                
                if (selectedStatus != "Not Initiated") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalReason = if (reason.isBlank()) null else reason
                    onStatusSelected(selectedStatus, finalReason)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 