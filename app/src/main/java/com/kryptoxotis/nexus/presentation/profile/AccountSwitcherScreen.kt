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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold
import com.kryptoxotis.nexus.presentation.theme.NexusTeal
import com.kryptoxotis.nexus.presentation.theme.NexusTextSecondary
import com.kryptoxotis.nexus.presentation.theme.neuInset
import com.kryptoxotis.nexus.presentation.theme.neuRaised

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdmin: () -> Unit = {}
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

    NexusScaffold(
        title = "Account",
        onBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Dimens.screenPadding, vertical = Dimens.gap),
            verticalArrangement = Arrangement.spacedBy(Dimens.gapSmall)
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

            // Admin entry — admin is not a tab, it lives here
            if (accountType == AccountType.ADMIN) {
                item(key = "admin_row") {
                    Card(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Admin dashboard", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Requests, users and organizations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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

            // ── Settings ──
            item(key = "settings_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    color = NexusTextSecondary
                )
            }
            item(key = "settings_rows") {
                var appearance by remember { mutableStateOf(SettingsStore.appearance(context)) }
                var cardView by remember { mutableStateOf(SettingsStore.cardView(context)) }
                var nfcSharing by remember { mutableStateOf(SettingsStore.nfcSharing(context)) }
                var notifications by remember { mutableStateOf(SettingsStore.notifications(context)) }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsChoiceRow(
                        icon = Icons.Default.Palette,
                        label = "Appearance",
                        options = listOf("Dark" to SettingsStore.APPEARANCE_DARK, "Light" to SettingsStore.APPEARANCE_LIGHT),
                        selected = appearance,
                        onSelect = {
                            appearance = it
                            SettingsStore.setAppearance(context, it)
                        }
                    )
                    SettingsChoiceRow(
                        icon = Icons.Default.Style,
                        label = "Card view",
                        options = listOf("List" to SettingsStore.CARD_VIEW_LIST, "Deck" to SettingsStore.CARD_VIEW_DECK),
                        selected = cardView,
                        onSelect = {
                            cardView = it
                            SettingsStore.setCardView(context, it)
                        }
                    )
                    SettingsToggleRow(
                        icon = Icons.Default.Nfc,
                        label = "NFC & sharing",
                        checked = nfcSharing,
                        onToggle = {
                            nfcSharing = it
                            SettingsStore.setNfcSharing(context, it)
                        }
                    )
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        label = "Notifications",
                        checked = notifications,
                        onToggle = {
                            notifications = it
                            SettingsStore.setNotifications(context, it)
                        }
                    )
                    SettingsRow(Icons.Default.Lock, "Privacy", null)
                    SettingsRow(Icons.Default.HelpOutline, "Help", null)
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

/** Settings row with an inline two-option choice (e.g. Dark / Light). */
@Composable
private fun SettingsChoiceRow(
    icon: ImageVector,
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .neuRaised(cornerRadius = 14.dp, surfaceColor = Color(0xFF141414))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NexusTextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFD0D0D0),
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .neuInset(cornerRadius = 10.dp)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            options.forEach { (optionLabel, optionValue) ->
                val isSelected = optionValue == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF1E1E1E) else Color.Transparent)
                        .clickable { onSelect(optionValue) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = optionLabel,
                        fontSize = 12.sp,
                        color = if (isSelected) NexusTeal else Color(0xFF6F6F6F)
                    )
                }
            }
        }
    }
}

/** Settings row with a switch. */
@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .neuRaised(cornerRadius = 14.dp, surfaceColor = Color(0xFF141414))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NexusTextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFD0D0D0),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = NexusTeal,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF2A2A2A),
                uncheckedThumbColor = Color(0xFF6F6F6F),
                uncheckedBorderColor = Color(0xFF3A3A3A)
            )
        )
    }
}

/** Settings group row: neuRaised #141414 at 14dp radius, icon + label + optional value. */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .neuRaised(cornerRadius = 14.dp, surfaceColor = Color(0xFF141414))
            .clickable { /* placeholder — settings screens land in a later pass */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = NexusTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFD0D0D0),
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color(0xFF7D7D7D)
            )
        }
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
