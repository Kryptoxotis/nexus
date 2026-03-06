package com.kryptoxotis.nexus.platform

import android.content.Context
import android.nfc.NfcAdapter
import com.kryptoxotis.nexus.service.NdefCache

actual class NfcManager(private val context: Context) {
    actual fun isSupported(): Boolean =
        NfcAdapter.getDefaultAdapter(context) != null

    actual suspend fun readNdef(): String? {
        // NFC reading is handled by NfcReader in service/ — this is a stub for the expect/actual contract
        return null
    }

    actual fun writeNdefCache(content: String, isUri: Boolean) {
        if (isUri) {
            NdefCache.writeUri(context, content)
        } else {
            NdefCache.writeVCard(context, content)
        }
    }
}
