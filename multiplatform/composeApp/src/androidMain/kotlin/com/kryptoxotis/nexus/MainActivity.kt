package com.kryptoxotis.nexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory
import com.kryptoxotis.nexus.platform.PlatformAuthManager
import com.kryptoxotis.nexus.platform.UrlLauncherContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val platformAuth = PlatformAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set Supabase credentials from BuildConfig
        SupabaseClientProvider.supabaseUrl = BuildConfig.SUPABASE_URL
        SupabaseClientProvider.supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

        // Initialize platform dependencies
        UrlLauncherContext.appContext = applicationContext
        AppModule.init(DatabaseDriverFactory(applicationContext))

        setContent {
            App(onSignInClick = { triggerGoogleSignIn() })
        }
    }

    private fun triggerGoogleSignIn() {
        lifecycleScope.launch {
            when (val result = platformAuth.signInWithGoogle(this@MainActivity)) {
                is Result.Success -> {
                    AppModule.authViewModel.signInWithIdToken(result.data)
                }
                is Result.Error -> {
                    AppModule.authViewModel.setError(result.message)
                }
            }
        }
    }
}
