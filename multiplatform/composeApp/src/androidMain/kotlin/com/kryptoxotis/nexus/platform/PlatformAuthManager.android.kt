package com.kryptoxotis.nexus.platform

import com.kryptoxotis.nexus.domain.model.Result

actual class PlatformAuthManager {
    actual suspend fun signInWithGoogle(): Result<String> {
        // Android auth is handled directly in AuthViewModel via CredentialManager
        // This expect/actual is primarily for iOS where we use Supabase OAuth
        return Result.Error("Use Android CredentialManager directly")
    }
}
