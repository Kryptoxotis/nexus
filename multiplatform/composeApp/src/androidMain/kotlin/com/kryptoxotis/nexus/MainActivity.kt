package com.kryptoxotis.nexus

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.di.AppModule
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.DatabaseDriverFactory
import com.kryptoxotis.nexus.platform.PlatformAuthManager
import com.kryptoxotis.nexus.platform.UrlLauncherContext
import com.kryptoxotis.nexus.service.NdefCache
import com.kryptoxotis.nexus.service.NFCPassService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val platformAuth = PlatformAuthManager()
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set Supabase credentials from BuildConfig
        SupabaseClientProvider.supabaseUrl = BuildConfig.SUPABASE_URL
        SupabaseClientProvider.supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY

        // Initialize platform dependencies
        UrlLauncherContext.appContext = applicationContext
        AppModule.init(DatabaseDriverFactory(applicationContext))

        // NFC setup
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Pre-cache NDEF bytes whenever the active card changes
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppModule.cardViewModel.activeCard.collect { card ->
                    try {
                        NdefCache.write(applicationContext, card)
                    } catch (_: Exception) { }
                }
            }
        }

        setContent {
            App(onSignInClick = { triggerGoogleSignIn() })
        }
    }

    @Suppress("NewApi")
    override fun onResume() {
        super.onResume()
        val adapter = nfcAdapter ?: return

        // Clean up any stale reader mode from ScanCardScreen
        try { adapter.disableReaderMode(this) } catch (_: Exception) {}

        // Set our HCE service as preferred so the system routes NFC to us
        try {
            val cardEmulation = CardEmulation.getInstance(adapter)
            cardEmulation.setPreferredService(
                this,
                ComponentName(this, NFCPassService::class.java)
            )
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= 35) {
            try {
                // API 35+: Disable polling (reading), keep HCE listen mode
                // This makes the phone act like a passive NFC tag — iPhones read it
                // without triggering wallet, Android doesn't try peer-to-peer
                adapter.setDiscoveryTechnology(
                    this,
                    NfcAdapter.FLAG_READER_DISABLE,
                    NfcAdapter.FLAG_LISTEN_KEEP
                )
            } catch (_: Exception) {}
        } else {
            try {
                // Pre-35 fallback: intercept tag reads silently to suppress the
                // "New tag scanned" / peer-to-peer dialog
                val pendingIntent = PendingIntent.getActivity(
                    this, 0,
                    Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE
                )
                adapter.enableForegroundDispatch(this, pendingIntent, null, null)
            } catch (_: Exception) {}
        }
    }

    @Suppress("NewApi")
    override fun onPause() {
        super.onPause()
        val adapter = nfcAdapter ?: return

        if (Build.VERSION.SDK_INT >= 35) {
            try { adapter.resetDiscoveryTechnology(this) } catch (_: Exception) {}
        } else {
            try { adapter.disableForegroundDispatch(this) } catch (_: Exception) {}
        }

        try {
            val cardEmulation = CardEmulation.getInstance(adapter)
            cardEmulation.unsetPreferredService(this)
        } catch (_: Exception) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // NFC tag discovered — ignore it to release RF field
        // This prevents the phone from trying to read its own emulated tag
        @Suppress("DEPRECATION")
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            try { nfcAdapter?.ignore(tag, 500, null, null) } catch (_: Exception) {}
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
