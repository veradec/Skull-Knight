package com.kentaro.skullknight.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kentaro.skullknight.data.Item
import com.kentaro.skullknight.ui.ProjectDetailUiState
import com.kentaro.skullknight.ui.components.ItemDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    uiState: ProjectDetailUiState,
    onBackClick: () -> Unit,
    onCreateItem: (String, String?) -> Unit,
    onUpdateItemStatus: (Item, String, String?) -> Unit,
    onDeleteItem: (Item) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateItemDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.project?.name ?: "Project",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateItemDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }
                uiState.project == null -> {
                    Text(
                        text = "Project not found",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.project.items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No items in this project",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to add your first item",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    ItemDisplay(
                        items = uiState.project.items,
                        onItemClick = { /* Could be used for editing or expanding */ },
                        onUpdateStatus = onUpdateItemStatus,
                        onDeleteItem = onDeleteItem,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // Create item dialog
    if (showCreateItemDialog) {
        CreateItemDialog(
            onItemCreated = { name, parentId ->
                onCreateItem(name, parentId)
                showCreateItemDialog = false
            },
            onDismiss = { showCreateItemDialog = false },
            availableItems = uiState.project?.items ?: emptyList()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemDialog(
    onItemCreated: (String, String?) -> Unit,
    onDismiss: () -> Unit,
    availableItems: List<Item>
) {
    var itemName by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf<String?>(null) }
    var showParentSelector by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (availableItems.isNotEmpty()) {
                    Text(
                        text = "Parent Item (optional):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val selectedParent = availableItems.find { it.id == selectedParentId }
                    OutlinedTextField(
                        value = selectedParent?.name ?: "None",
                        onValueChange = { },
                        label = { Text("Parent Item") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showParentSelector = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Parent"
                                )
                            }
                        }
                    )
                    
                    if (selectedParentId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { selectedParentId = null }
                        ) {
                            Text("Clear Parent")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onItemCreated(itemName, selectedParentId)
                    }
                },
                enabled = itemName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Parent item selector
    if (showParentSelector) {
        AlertDialog(
            onDismissRequest = { showParentSelector = false },
            title = { Text("Select Parent Item") },
            text = {
                Column {
                    availableItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedParentId == item.id,
                                onClick = { selectedParentId = item.id }
                            )
                            Text(item.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showParentSelector = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showParentSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 