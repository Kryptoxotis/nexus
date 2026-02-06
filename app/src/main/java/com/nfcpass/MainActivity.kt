package com.nfcpass

import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfcpass.data.local.PassDatabase
import com.nfcpass.data.remote.AuthManager
import com.nfcpass.data.remote.SupabaseClientProvider
import com.nfcpass.data.repository.PassRepository
import com.nfcpass.presentation.auth.AuthState
import com.nfcpass.presentation.auth.AuthViewModel
import com.nfcpass.presentation.auth.LoginScreen
import com.nfcpass.presentation.passes.AddPassScreen
import com.nfcpass.presentation.passes.PassListScreen
import com.nfcpass.presentation.passes.PassViewModel
import com.nfcpass.presentation.profile.AccountSwitcherScreen
import com.nfcpass.presentation.profile.ProfileSetupScreen
import com.nfcpass.presentation.theme.NFCPassAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: PassRepository
    private lateinit var passViewModel: PassViewModel
    private lateinit var authManager: AuthManager
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNfcSupport()

        // Initialize Supabase (configure from BuildConfig or secrets)
        initSupabase()

        // Initialize database and repository
        val database = PassDatabase.getDatabase(applicationContext)
        repository = PassRepository(database.passDao())

        // Initialize auth
        authManager = AuthManager(applicationContext)
        authViewModel = AuthViewModel(authManager)

        // Initialize ViewModel
        passViewModel = PassViewModel(repository)

        // Check for existing session
        authViewModel.checkSession()

        setContent {
            NFCPassAppTheme {
                val navController = rememberNavController()
                val authState by authViewModel.authState.collectAsState()
                val coroutineScope = rememberCoroutineScope()

                // Determine start destination based on auth state
                val startDestination = when (authState) {
                    is AuthState.Loading -> "login"
                    is AuthState.NotAuthenticated -> "login"
                    is AuthState.NeedsProfileSetup -> "profile_setup"
                    is AuthState.Authenticated -> "pass_list"
                    is AuthState.Error -> "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onSignedIn = {
                                val state = authViewModel.authState.value
                                coroutineScope.launch {
                                    // Migrate any local passes to the real user
                                    val userId = authViewModel.getCurrentUserId()
                                    if (userId != null) {
                                        repository.migrateLocalPasses(userId)
                                        repository.syncFromSupabase()
                                    }
                                }
                                when (state) {
                                    is AuthState.NeedsProfileSetup -> {
                                        navController.navigate("profile_setup") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    is AuthState.Authenticated -> {
                                        navController.navigate("pass_list") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }

                    composable("profile_setup") {
                        ProfileSetupScreen(
                            authViewModel = authViewModel,
                            onSetupComplete = {
                                navController.navigate("pass_list") {
                                    popUpTo("profile_setup") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("pass_list") {
                        PassListScreen(
                            viewModel = passViewModel,
                            authViewModel = authViewModel,
                            onNavigateToAddPass = {
                                navController.navigate("add_pass")
                            },
                            onNavigateToAccounts = {
                                navController.navigate("accounts")
                            }
                        )
                    }

                    composable("add_pass") {
                        AddPassScreen(
                            viewModel = passViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("accounts") {
                        AccountSwitcherScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                // Handle sign out - navigate to login
                LaunchedEffect(authState) {
                    if (authState is AuthState.NotAuthenticated) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != null && currentRoute != "login") {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initSupabase() {
        // TODO: Load these from BuildConfig or secrets.properties
        // For now, set them manually or from a config file
        try {
            val properties = java.util.Properties()
            val secretsFile = java.io.File(applicationContext.filesDir.parentFile?.parentFile?.parentFile?.parentFile, "secrets.properties")
            if (secretsFile.exists()) {
                properties.load(secretsFile.inputStream())
                SupabaseClientProvider.supabaseUrl = properties.getProperty("SUPABASE_URL", "")
                SupabaseClientProvider.supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY", "")
            }

            // Also try to set Google Web Client ID
            AuthManager.googleWebClientId = properties.getProperty("GOOGLE_WEB_CLIENT_ID", "")
        } catch (e: Exception) {
            android.util.Log.w("NFCPass:MainActivity", "Could not load secrets.properties: ${e.message}")
        }
    }

    private fun checkNfcSupport() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        when {
            nfcAdapter == null -> {
                Toast.makeText(
                    this,
                    "NFC is not supported on this device.",
                    Toast.LENGTH_LONG
                ).show()
            }
            !nfcAdapter.isEnabled -> {
                Toast.makeText(
                    this,
                    "NFC is disabled. Please enable it in Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                android.util.Log.d("NFCPass:MainActivity", "NFC is supported and enabled")
            }
        }
    }
}
