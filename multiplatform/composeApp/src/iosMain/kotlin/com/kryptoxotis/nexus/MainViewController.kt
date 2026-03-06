package com.kryptoxotis.nexus

import androidx.compose.ui.window.ComposeUIViewController
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory
import com.kryptoxotis.nexus.platform.PlatformAuthManager
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.Foundation.NSURL

private val platformAuth = PlatformAuthManager()

fun MainViewController() = ComposeUIViewController {
    App(onSignInClick = {
        GlobalScope.launch {
            platformAuth.signInWithGoogle()
        }
    })
}

fun initApp() {
    SupabaseClientProvider.supabaseUrl = IosConfig.SUPABASE_URL
    SupabaseClientProvider.supabaseAnonKey = IosConfig.SUPABASE_ANON_KEY
    AppModule.init(DatabaseDriverFactory())
}

fun handleDeepLink(url: String) {
    val nsUrl = NSURL(string = url) ?: return
    SupabaseClientProvider.getClient().handleDeeplinks(
        url = nsUrl,
        onSessionSuccess = {
            // Session imported — refresh auth state
            AppModule.authViewModel.checkSession()
        }
    )
}
