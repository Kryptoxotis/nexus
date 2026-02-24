package com.kryptoxotis.nexus.presentation.business

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
import com.kryptoxotis.nexus.domain.model.EnrollmentMode
import com.kryptoxotis.nexus.domain.model.Organization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    viewModel: BusinessViewModel,
    onNavigateBack: () -> Unit
) {
    val organizations by viewModel.organizations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var selectedOrg by remember { mutableStateOf<Organization?>(null) }
    var pinInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadOrganizations()
    }

    LaunchedEffect(uiState) {
        if (uiState is BusinessUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enroll in Organization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No organizations available",
                        style = MaterialTheme.typography.titleMedium,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Available Organizations",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(organizations, key = { it.id }) { org ->
                    OrgEnrollmentCard(
                        org = org,
                        isLoading = uiState is BusinessUiState.Loading,
                        onEnroll = {
                            when (org.enrollmentMode) {
                                EnrollmentMode.OPEN -> {
                                    viewModel.enrollInOrganization(org.id, org.name)
                                }
                                EnrollmentMode.PIN -> {
                                    selectedOrg = org
                                    showPinDialog = true
                                }
                                EnrollmentMode.INVITE, EnrollmentMode.CLOSED -> { /* Cannot self-enroll */ }
                            }
                        }
                    )
                }
            }
        }

        // Error is shown inline in the PIN dialog above
    }

    if (showPinDialog && selectedOrg != null) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
            },
            title = { Text("Enter PIN") },
            text = {
                Column {
                    Text("Enter the enrollment PIN for ${selectedOrg!!.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState is BusinessUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as BusinessUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.enrollWithPin(selectedOrg!!.id, selectedOrg!!.name, pinInput)
                        showPinDialog = false
                        pinInput = ""
                    },
                    enabled = pinInput.isNotBlank()
                ) {
                    Text("Enroll")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    pinInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OrgEnrollmentCard(
    org: Organization,
    isLoading: Boolean,
    onEnroll: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
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
                }
            }

            if (org.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = org.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val canEnroll = org.enrollmentMode == EnrollmentMode.OPEN || org.enrollmentMode == EnrollmentMode.PIN

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (org.enrollmentMode) {
                        EnrollmentMode.OPEN -> "Open enrollment"
                        EnrollmentMode.PIN -> "PIN required"
                        EnrollmentMode.INVITE -> "Invite only"
                        EnrollmentMode.CLOSED -> "Closed"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                Button(
                    onClick = onEnroll,
                    enabled = canEnroll && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (canEnroll) "Enroll" else "Unavailable")
                    }
                }
            }
        }
    }
}
