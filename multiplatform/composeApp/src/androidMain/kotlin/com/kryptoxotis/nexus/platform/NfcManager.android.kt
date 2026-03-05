package com.kryptoxotis.nexus.platform

import android.content.Context
import android.nfc.NfcAdapter

actual class NfcManager(private val context: Context) {
    actual fun isSupported(): Boolean =
        NfcAdapter.getDefaultAdapter(context) != null

    actual suspend fun readNdef(): String? {
        // NFC reading is handled by NfcReader in service/ — this is a stub for the expect/actual contract
        return null
    }

    actual fun writeNdefCache(content: String, isUri: Boolean) {
        // Delegated to NdefCache in service/
    }
}
