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
import com.kryptoxotis.nexus.presentation.cards.CardPreview
import com.kryptoxotis.nexus.presentation.theme.Dimens
import com.kryptoxotis.nexus.presentation.theme.NexusEmptyState
import com.kryptoxotis.nexus.presentation.theme.NexusScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessPassListScreen(
    viewModel: BusinessViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEnrollment: () -> Unit
) {
    val passes by viewModel.userPasses.collectAsState()

    NexusScaffold(
        title = "Passes",
        subtitle = "Your memberships",
        actions = listOf<Pair<androidx.compose.ui.graphics.vector.ImageVector, () -> Unit>>(
            Icons.Default.Add to onNavigateToEnrollment
        )
    ) {
        if (passes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NexusEmptyState(
                    icon = Icons.Default.Badge,
                    title = "No passes yet",
                    body = "Enroll in an organization to get your first pass.",
                    actionLabel = "Enroll",
                    onAction = onNavigateToEnrollment
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Dimens.screenPadding, vertical = Dimens.gap),
                verticalArrangement = Arrangement.spacedBy(Dimens.gap)
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
    // Palette-mapped status colors; expired passes render in dark mode
    val storedColor = when (pass.status) {
        PassStatus.ACTIVE -> "#0A7968"
        PassStatus.SUSPENDED -> "#F95B1A"
        PassStatus.REVOKED -> "#FF1744"
        PassStatus.EXPIRED -> "#0A7968:dark"
    }
    val subtitle = buildString {
        append(pass.status.name.lowercase().replaceFirstChar { it.uppercase() })
        if (pass.expiresAt != null) append(" · expires ${pass.expiresAt}")
    }
    CardPreview(
        title = pass.organizationName ?: "Organization",
        subtitle = subtitle,
        cardShape = "card",
        storedColor = storedColor,
        tag = "Business"
    )
}
