package com.kryptoxotis.nexus.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel
import com.kryptoxotis.nexus.presentation.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onNavigateToRequests: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToOrgs: () -> Unit,
    onNavigateToCardWallet: () -> Unit,
    onNavigateToAccounts: () -> Unit
) {
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val users by viewModel.users.collectAsState()
    val organizations by viewModel.organizations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPendingRequests()
        viewModel.loadUsers()
        viewModel.loadOrganizations()
    }

    NexusScaffold(
        title = "Admin",
        subtitle = "Nexus control",
        actions = listOf<Pair<ImageVector, () -> Unit>>(
            Icons.Default.CreditCard to onNavigateToCardWallet,
            Icons.Default.AccountCircle to onNavigateToAccounts
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Default.ChatBubble,
                label = "Requests",
                value = "${pendingRequests.size}",
                color = NexusOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.People,
                label = "Users",
                value = "${users.size}",
                color = NexusTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.GridView,
                label = "Orgs",
                value = "${organizations.size}",
                color = NexusOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Menu items
        val adminCount = users.count { it.accountType.equals("admin", ignoreCase = true) }
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuItem(
                icon = Icons.Default.ChatBubble,
                title = "Business requests",
                subtitle = "${pendingRequests.size} pending",
                iconColor = NexusOrange,
                onClick = onNavigateToRequests
            )
            MenuItem(
                icon = Icons.Default.People,
                title = "User management",
                subtitle = "${users.size} users · $adminCount admin",
                iconColor = NexusTeal,
                onClick = onNavigateToUsers
            )
            MenuItem(
                icon = Icons.Default.GridView,
                title = "Organizations",
                subtitle = "${organizations.size} registered",
                iconColor = NexusOrange,
                onClick = onNavigateToOrgs
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .neuRaised(cornerRadius = 16.dp, elevation = 8.dp, surfaceColor = NexusSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Inset icon circle — color lives on the icon only
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .neuInset(cornerRadius = 21.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NexusTextPrimary,
                lineHeight = 40.sp
            )
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = NexusTextSecondary
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuRaised(cornerRadius = 20.dp, elevation = 10.dp, surfaceColor = NexusSurface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(22.dp, 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inset icon circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .neuInset(cornerRadius = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NexusTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = NexusTextSecondary,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NexusOrange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
