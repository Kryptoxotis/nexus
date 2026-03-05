package com.kryptoxotis.nexus.platform

actual class NfcManager {
    actual fun isSupported(): Boolean {
        // Core NFC is available on iPhone 7+ with iOS 11+
        // NFCNDEFReaderSession.readingAvailable would be checked here
        return true
    }

    actual suspend fun readNdef(): String? {
        // Core NFC NFCNDEFReaderSession implementation would go here
        // Requires Swift interop for the delegate pattern
        return null
    }

    actual fun writeNdefCache(content: String, isUri: Boolean) {
        // iOS cannot write NFC tags via HCE — no-op
    }
}
