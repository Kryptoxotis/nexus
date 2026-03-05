package com.kryptoxotis.nexus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.navigation.NexusNavHost
import com.kryptoxotis.nexus.presentation.theme.NexusTheme

@Composable
fun App(onSignInClick: () -> Unit = {}) {
    LaunchedEffect(Unit) { AppModule.onAppStart() }

    NexusTheme {
        val navController = rememberNavController()

        NexusNavHost(
            navController = navController,
            authViewModel = AppModule.authViewModel,
            cardViewModel = AppModule.cardViewModel,
            receivedCardViewModel = AppModule.receivedCardViewModel,
            businessViewModel = AppModule.businessViewModel,
            adminViewModel = AppModule.adminViewModel,
            onSignInClick = onSignInClick
        )
    }
}
