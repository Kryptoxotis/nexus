package com.kryptoxotis.nexus

import androidx.compose.ui.window.ComposeUIViewController
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initApp() {
    SupabaseClientProvider.supabaseUrl = IosConfig.SUPABASE_URL
    SupabaseClientProvider.supabaseAnonKey = IosConfig.SUPABASE_ANON_KEY
    AppModule.init(DatabaseDriverFactory())
}

fun handleDeepLink(url: String) {
    val nsUrl = NSURL(string = url) ?: return
    SupabaseClientProvider.getClient().handleDeeplinks(nsUrl)
}
