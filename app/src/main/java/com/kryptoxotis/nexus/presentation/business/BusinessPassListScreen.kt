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
import com.kryptoxotis.nexus.domain.model.BusinessPass
import com.kryptoxotis.nexus.domain.model.PassStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessPassListScreen(
    viewModel: BusinessViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEnrollment: () -> Unit
) {
    val passes by viewModel.userPasses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Passes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToEnrollment) {
                Icon(Icons.Default.Add, contentDescription = "Enroll")
            }
        }
    ) { paddingValues ->
        if (passes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No business passes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Tap + to enroll in an organization",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(passes, key = { it.id }) { pass ->
                    BusinessPassItem(pass = pass)
                }
            }
        }
    }
}

@Composable
private fun BusinessPassItem(pass: BusinessPass) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Badge,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (pass.status) {
                    PassStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                    PassStatus.EXPIRED -> MaterialTheme.colorScheme.outline
                    PassStatus.REVOKED -> MaterialTheme.colorScheme.error
                    PassStatus.SUSPENDED -> MaterialTheme.colorScheme.tertiary
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pass.organizationName ?: "Organization",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Status: ${pass.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (pass.status) {
                        PassStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        PassStatus.EXPIRED -> MaterialTheme.colorScheme.outline
                        PassStatus.REVOKED -> MaterialTheme.colorScheme.error
                        PassStatus.SUSPENDED -> MaterialTheme.colorScheme.tertiary
                    }
                )
                if (pass.expiresAt != null) {
                    Text(
                        text = "Expires: ${pass.expiresAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
