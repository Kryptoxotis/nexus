package com.kryptoxotis.nexus.presentation.cards

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: String,
    viewModel: ReceivedCardViewModel,
    onNavigateBack: () -> Unit
) {
    val contact by viewModel.getContact(contactId).collectAsState(initial = null)
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        val c = contact
        if (c == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Contact not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = c.name.ifBlank { "Unknown" },
                style = MaterialTheme.typography.headlineMedium
            )
            val subtitle = listOfNotNull(
                c.jobTitle.ifBlank { null },
                c.company.ifBlank { null }
            ).joinToString(" at ")
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Contact fields
            if (c.phone.isNotBlank()) {
                ContactField(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = c.phone,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}"))
                        context.startActivity(intent)
                    }
                )
            }
            if (c.email.isNotBlank()) {
                ContactField(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = c.email,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${c.email}"))
                        context.startActivity(intent)
                    }
                )
            }
            if (c.website.isNotBlank()) {
                ContactField(
                    icon = Icons.Default.Language,
                    label = "Website",
                    value = c.website,
                    onClick = {
                        val url = if (c.website.startsWith("http")) c.website else "https://${c.website}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
            if (c.linkedin.isNotBlank()) {
                ContactField(icon = Icons.Default.Work, label = "LinkedIn", value = c.linkedin, onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(c.linkedin))
                    context.startActivity(intent)
                })
            }
            if (c.instagram.isNotBlank()) {
                ContactField(icon = Icons.Default.CameraAlt, label = "Instagram", value = c.instagram, onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(c.instagram))
                    context.startActivity(intent)
                })
            }
            if (c.twitter.isNotBlank()) {
                ContactField(icon = Icons.Default.Tag, label = "Twitter / X", value = c.twitter, onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(c.twitter))
                    context.startActivity(intent)
                })
            }
            if (c.github.isNotBlank()) {
                ContactField(icon = Icons.Default.Code, label = "GitHub", value = c.github, onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(c.github))
                    context.startActivity(intent)
                })
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact") },
            text = { Text("Remove this contact from your collection?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteContact(contactId)
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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
private fun ContactField(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
