package com.kryptoxotis.nexus.platform

import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.domain.model.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google

actual class PlatformAuthManager {
    actual suspend fun signInWithGoogle(): Result<String> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.auth.signInWith(Google)
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                Result.Success(user.id)
            } else {
                Result.Error("Sign in completed but no user found")
            }
        } catch (e: Exception) {
            Result.Error("Google sign-in failed: ${e.message}", e)
        }
    }
}
