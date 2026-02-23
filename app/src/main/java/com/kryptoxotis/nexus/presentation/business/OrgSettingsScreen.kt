package com.kryptoxotis.nexus.presentation.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.EnrollmentMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgSettingsScreen(
    viewModel: BusinessViewModel,
    onNavigateBack: () -> Unit
) {
    val myOrg by viewModel.myOrganization.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var name by remember(myOrg) { mutableStateOf(myOrg?.name ?: "") }
    var description by remember(myOrg) { mutableStateOf(myOrg?.description ?: "") }
    var enrollmentMode by remember(myOrg) { mutableStateOf(myOrg?.enrollmentMode ?: EnrollmentMode.OPEN) }
    var staticPin by remember(myOrg) { mutableStateOf(myOrg?.staticPin ?: "") }
    var allowSelfEnrollment by remember(myOrg) { mutableStateOf(myOrg?.allowSelfEnrollment ?: true) }
    var expanded by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is BusinessUiState.Success) {
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organization Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (myOrg == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No organization found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Organization Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()

            Text(
                text = "Enrollment",
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = enrollmentMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Enrollment Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    EnrollmentMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                enrollmentMode = mode
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Allow Self-Enrollment")
                Switch(
                    checked = allowSelfEnrollment,
                    onCheckedChange = { allowSelfEnrollment = it }
                )
            }

            if (enrollmentMode == EnrollmentMode.PIN) {
                OutlinedTextField(
                    value = staticPin,
                    onValueChange = { staticPin = it },
                    label = { Text("Static PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    viewModel.updateOrganization(
                        orgId = myOrg!!.id,
                        name = name.ifBlank { null },
                        description = description.ifBlank { null },
                        enrollmentMode = enrollmentMode,
                        staticPin = staticPin.ifBlank { null },
                        allowSelfEnrollment = allowSelfEnrollment
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is BusinessUiState.Loading
            ) {
                if (uiState is BusinessUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Changes")
                }
            }

            HorizontalDivider()

            Text(
                text = "Enrollment PINs",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("New PIN Code") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        viewModel.createEnrollmentPin(myOrg!!.id, newPin)
                        newPin = ""
                    },
                    enabled = newPin.isNotBlank()
                ) {
                    Text("Create")
                }
            }

            if (uiState is BusinessUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as BusinessUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (uiState is BusinessUiState.Success) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = (uiState as BusinessUiState.Success).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
