package com.nfcpass.presentation.passes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassScreen(
    viewModel: PassViewModel,
    onNavigateBack: () -> Unit
) {
    var passId by remember { mutableStateOf("") }
    var passName by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on success
    LaunchedEffect(uiState) {
        if (uiState is PassUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pass") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Enter pass details",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = passId,
                onValueChange = { passId = it },
                label = { Text("Pass ID *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., ABC123456") }
            )

            OutlinedTextField(
                value = passName,
                onValueChange = { passName = it },
                label = { Text("Pass Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Gym Membership") }
            )

            OutlinedTextField(
                value = organization,
                onValueChange = { organization = it },
                label = { Text("Organization *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Gold's Gym") }
            )

            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Link (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://example.com") },
                supportingText = { Text("When set, tapping phones opens this link on the other phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.addPass(
                        passId = passId,
                        passName = passName,
                        organization = organization,
                        expiryDate = expiryDate.ifBlank { null },
                        link = link.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is PassUiState.Loading
            ) {
                if (uiState is PassUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Pass")
                }
            }

            // Show error message
            if (uiState is PassUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as PassUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
