package com.kryptoxotis.nexus.presentation.profile

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.BuildConfig
import com.kryptoxotis.nexus.data.remote.AuthManager
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.presentation.auth.AuthState
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val savedAccounts by authViewModel.savedAccounts.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    val activeEmail = when (val state = authState) {
        is AuthState.Authenticated -> {
            val profile by authViewModel.profile.collectAsState()
            profile?.email
        }
        else -> null
    }

    val accountType = (authState as? AuthState.Authenticated)?.accountType
    val businessRequest = authViewModel.businessRequest.collectAsState().value
    var showBusinessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(savedAccounts, key = { it.email }) { account ->
                AccountItem(
                    account = account,
                    isActive = account.email == activeEmail,
                    onClick = {
                        if (account.email != activeEmail) {
                            authViewModel.switchAccount(account.email)
                        }
                    }
                )
            }

            // Upgrade to Business card
            if (accountType == AccountType.INDIVIDUAL && businessRequest?.status != "pending") {
                item(key = "upgrade_business") {
                    Spacer(modifier = Modifier.height(8.dp))
                    val isRejected = businessRequest?.status == "rejected"
                    Card(
                        onClick = { showBusinessDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRejected)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = if (isRejected)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isRejected) "Re-request Business Account" else "Upgrade to Business",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isRejected)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (isRejected)
                                        "Your previous request was rejected. Tap to try again."
                                    else
                                        "Create and manage passes for your organization",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isRejected)
                                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = if (isRejected)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        (context as? Activity)?.let { authViewModel.signInWithGoogle(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Account")
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { authViewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    // Business upgrade request dialog
    if (showBusinessDialog) {
        var businessName by remember { mutableStateOf("") }
        var businessType by remember { mutableStateOf("") }
        var contactEmail by remember { mutableStateOf("") }
        var userMessage by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var enrollmentMode by remember { mutableStateOf("open") }

        AlertDialog(
            onDismissRequest = { showBusinessDialog = false },
            title = { Text("Business Account Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tell us about your business. An admin will review your request and create your organization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = businessType,
                        onValueChange = { businessType = it },
                        label = { Text("Business Type") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g., Gym, Restaurant") }
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Brief description of your business") }
                    )
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Contact Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "Enrollment Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = enrollmentMode == "open",
                            onClick = { enrollmentMode = "open" },
                            label = { Text("Open") }
                        )
                        FilterChip(
                            selected = enrollmentMode == "pin",
                            onClick = { enrollmentMode = "pin" },
                            label = { Text("PIN") }
                        )
                        FilterChip(
                            selected = enrollmentMode == "closed",
                            onClick = { enrollmentMode = "closed" },
                            label = { Text("Closed") }
                        )
                    }
                    OutlinedTextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Why do you need a business account?") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Encode org details as JSON in the message field
                        val jsonMessage = org.json.JSONObject().apply {
                            put("userMessage", userMessage)
                            put("description", description)
                            put("enrollmentMode", enrollmentMode)
                        }.toString()
                        authViewModel.requestBusinessUpgrade(
                            businessName = businessName,
                            businessType = businessType.ifBlank { null },
                            contactEmail = contactEmail.ifBlank { null },
                            message = jsonMessage
                        )
                        showBusinessDialog = false
                    },
                    enabled = businessName.isNotBlank()
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBusinessDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountItem(
    account: AuthManager.SavedAccount,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isActive) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isActive) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
