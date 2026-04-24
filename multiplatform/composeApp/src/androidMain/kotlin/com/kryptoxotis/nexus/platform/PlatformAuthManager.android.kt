package com.kryptoxotis.nexus.platform

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kryptoxotis.nexus.BuildConfig
import com.kryptoxotis.nexus.domain.model.Result

actual class PlatformAuthManager {

    actual suspend fun signInWithGoogle(): Result<String> {
        return Result.Error("Use signInWithGoogle(activity) on Android")
    }

    suspend fun signInWithGoogle(activity: Activity): Result<String> {
        val credentialManager = CredentialManager.create(activity)

        // Try One Tap first, fall back to full Sign In With Google if no credentials cached
        val idTokenOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        val signInOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        for (request in listOf(
            GetCredentialRequest.Builder().addCredentialOption(idTokenOption).build(),
            GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
        )) {
            try {
                val response = credentialManager.getCredential(request = request, context = activity)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
                return Result.Success(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                Logger.e("Nexus:Auth", "Credential attempt failed: ${e.type}", e)
                continue
            } catch (e: Exception) {
                Logger.e("Nexus:Auth", "Google Sign-In failed", e)
                return Result.Error("Sign-in failed: ${e.message}", e)
            }
        }

        return Result.Error("Sign-in failed: No credentials available")
    }
}
