package com.kryptoxotis.nexus.presentation.profile

import androidx.compose.runtime.*
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.presentation.auth.AuthViewModel

@Composable
fun ProfileSetupScreen(
    authViewModel: AuthViewModel,
    onSetupComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        authViewModel.setAccountType(AccountType.INDIVIDUAL)
        onSetupComplete()
    }
}
