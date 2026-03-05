package com.kryptoxotis.nexus.platform

import com.kryptoxotis.nexus.domain.model.Result

expect class PlatformAuthManager {
    suspend fun signInWithGoogle(): Result<String>
}
