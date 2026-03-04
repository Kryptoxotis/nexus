package com.kryptoxotis.nexus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.kryptoxotis.nexus.service.NdefCache

/**
 * Invisible activity that receives ACTION_SEND intents.
 * Writes the shared URL to NdefCache for instant NFC emulation.
 * Shows a toast and finishes — no visible UI.
 * When the user opens Nexus, the share screen appears automatically.
 */
class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (url != null) {
            NdefCache.writeUri(this, url)

            // Stash URL so MainActivity shows share screen when opened
            getSharedPreferences("nexus_share", MODE_PRIVATE)
                .edit().putString("pending_url", url).apply()

            Toast.makeText(this, "NFC ready — tap to share", Toast.LENGTH_LONG).show()
        }

        finish()
    }
}
