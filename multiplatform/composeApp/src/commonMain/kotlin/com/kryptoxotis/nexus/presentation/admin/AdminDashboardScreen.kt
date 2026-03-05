package com.kryptoxotis.nexus.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kryptoxotis.nexus.presentation.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Back button placeholder (left)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuCircleButton(
                    icon = Icons.Default.Close,
                    onClick = onNavigateToCardWallet
                )
            }
            // Right icons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeuCircleButton(
                    icon = Icons.Default.CreditCard,
                    onClick = onNavigateToCardWallet
                )
                NeuCircleButton(
                    icon = Icons.Default.AccountCircle,
                    onClick = onNavigateToAccounts
                )
            }
        }

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Default.ChatBubble,
                value = "${pendingRequests.size}",
                color = NexusOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.People,
                value = "${users.size}",
                color = NexusTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.GridView,
                value = "${organizations.size}",
                color = NexusOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Menu items
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuItem(
                icon = Icons.Default.ChatBubble,
                title = "Business\nRequests",
                iconColor = NexusOrange,
                onClick = onNavigateToRequests
            )
            MenuItem(
                icon = Icons.Default.People,
                title = "User\nManagement",
                iconColor = NexusTeal,
                onClick = onNavigateToUsers
            )
            MenuItem(
                icon = Icons.Default.GridView,
                title = "Organizations",
                iconColor = NexusOrange,
                onClick = onNavigateToOrgs
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NeuCircleButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .neuCircle(elevation = 4.dp, surfaceColor = NexusSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF888888),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
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
            // Inset icon circle
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
                color = color,
                lineHeight = 40.sp
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
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
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666),
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NexusOrange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
