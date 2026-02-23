package com.kryptoxotis.nexus.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel

@Composable
fun ProfileSetupScreen(
    authViewModel: AuthViewModel,
    onSetupComplete: () -> Unit
) {
    var showBusinessForm by remember { mutableStateOf(false) }
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var requestSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!showBusinessForm && !requestSubmitted) {
            Text(
                text = "How will you use Nexus?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Individual option (immediate)
            Card(
                onClick = {
                    authViewModel.setAccountType(AccountType.INDIVIDUAL)
                    onSetupComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Individual",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Personal card wallet with NFC and QR codes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Business option (shows form)
            Card(
                onClick = { showBusinessForm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Business",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Create and manage passes for your organization",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Requires admin approval",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        } else if (showBusinessForm && !requestSubmitted) {
            // Business request form
            Text(
                text = "Business Account Request",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us about your business. An admin will review your request.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = businessType,
                onValueChange = { businessType = it },
                label = { Text("Business Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Gym, Restaurant, Office") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("Contact Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Why do you need a business account?") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.requestBusinessUpgrade(
                        businessName = businessName,
                        businessType = businessType.ifBlank { null },
                        contactEmail = contactEmail.ifBlank { null },
                        message = message.ifBlank { null }
                    )
                    // Set as individual for now, will upgrade when approved
                    authViewModel.setAccountType(AccountType.INDIVIDUAL)
                    requestSubmitted = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = businessName.isNotBlank()
            ) {
                Text("Submit Request")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { showBusinessForm = false }) {
                Text("Back")
            }
        } else if (requestSubmitted) {
            // Confirmation
            Text(
                text = "Request Submitted!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your business request is pending approval. You can use Nexus as an individual in the meantime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSetupComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue as Individual")
            }
        }
    }
}
