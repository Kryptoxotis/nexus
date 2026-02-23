package com.kryptoxotis.nexus.presentation.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuePassScreen(
    viewModel: BusinessViewModel,
    onNavigateBack: () -> Unit
) {
    val myOrg by viewModel.myOrganization.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is BusinessUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Pass") },
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
                Text("No organization found. Create one first.")
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
                text = "Issue a pass for ${myOrg!!.name}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Enter the user's email to issue them a business pass. They must have a Nexus account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("User Email *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = { Text("user@example.com") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // For now, enroll with the org ID - in a real app you'd look up the user by email
                    viewModel.enrollInOrganization(myOrg!!.id, myOrg!!.name)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && uiState !is BusinessUiState.Loading
            ) {
                if (uiState is BusinessUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Issue Pass")
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
        }
    }
}
