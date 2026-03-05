package com.kryptoxotis.nexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory
import com.kryptoxotis.nexus.platform.UrlLauncherContext

class MainActivity : ComponentActivity() {
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
            App()
        }
    }
}
