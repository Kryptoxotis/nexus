package com.kryptoxotis.nexus.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val organizations by viewModel.organizations.collectAsState()
    val users by viewModel.users.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadOrganizations()
        viewModel.loadUsers()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.Success -> {
                snackbarHostState.showSnackbar((uiState as AdminUiState.Success).message)
                viewModel.resetState()
            }
            is AdminUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as AdminUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organizations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Organization")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (organizations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No organizations yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "${organizations.size} organization(s)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(organizations) { org ->
                    OrgItem(
                        org = org,
                        onToggleActive = {
                            viewModel.toggleOrganizationActive(
                                org.id ?: "",
                                !org.isActive
                            )
                        },
                        onDelete = {
                            viewModel.deleteOrganization(org.id ?: "")
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateOrgDialog(
            users = users,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type, description, ownerId, enrollmentMode ->
                viewModel.createOrganization(name, type, description, ownerId, enrollmentMode)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun OrgItem(
    org: OrganizationDto,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = org.isActive
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = org.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (org.type != null) {
                    Text(
                        text = org.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (isActive) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isActive)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                (org.enrollmentMode ?: "open").replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isActive) "Deactivate" else "Activate") },
                        onClick = {
                            onToggleActive()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (isActive) Icons.Default.ToggleOff else Icons.Default.ToggleOn,
                                contentDescription = null
                            )
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            showDeleteConfirm = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Organization") },
            text = { Text("Delete \"${org.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrgDialog(
    users: List<ProfileDto>,
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String?, description: String?, ownerId: String, enrollmentMode: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedOwnerId by remember { mutableStateOf("") }
    var enrollmentMode by remember { mutableStateOf("open") }
    var ownerExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    val selectedOwner = users.find { it.id == selectedOwnerId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Organization") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type") },
                    placeholder = { Text("e.g. Technology") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = ownerExpanded,
                    onExpandedChange = { ownerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOwner?.fullName ?: selectedOwner?.email ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Owner *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = ownerExpanded,
                        onDismissRequest = { ownerExpanded = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.fullName ?: user.email ?: user.id) },
                                onClick = {
                                    selectedOwnerId = user.id
                                    ownerExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = enrollmentMode.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Enrollment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        listOf("open", "pin", "invite", "closed").forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    enrollmentMode = mode
                                    modeExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        name,
                        type.ifBlank { null },
                        description.ifBlank { null },
                        selectedOwnerId,
                        enrollmentMode
                    )
                },
                enabled = name.isNotBlank() && selectedOwnerId.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
