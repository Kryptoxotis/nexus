package com.kryptoxotis.nexus.platform

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kryptoxotis.nexus.BuildConfig
import com.kryptoxotis.nexus.domain.model.Result

actual class PlatformAuthManager {

    actual suspend fun signInWithGoogle(): Result<String> {
        // Needs Activity context — use signInWithGoogle(activity) instead
        return Result.Error("Use signInWithGoogle(activity) on Android")
    }

    suspend fun signInWithGoogle(activity: Activity): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(activity)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
            Result.Success(googleIdTokenCredential.idToken)
        } catch (e: Exception) {
            Logger.e("Nexus:Auth", "Google Sign-In failed", e)
            Result.Error("Sign-in failed: ${e.message}", e)
        }
    }
}
