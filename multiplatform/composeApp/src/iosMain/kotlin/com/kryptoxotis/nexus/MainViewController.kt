package com.kryptoxotis.nexus

import androidx.compose.ui.window.ComposeUIViewController
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initApp(supabaseUrl: String, supabaseAnonKey: String) {
    SupabaseClientProvider.supabaseUrl = supabaseUrl
    SupabaseClientProvider.supabaseAnonKey = supabaseAnonKey
    AppModule.init(DatabaseDriverFactory())
}
