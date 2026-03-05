package com.kryptoxotis.nexus.platform

expect class NfcManager {
    fun isSupported(): Boolean
    suspend fun readNdef(): String?
    fun writeNdefCache(content: String, isUri: Boolean)
}
