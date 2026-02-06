package com.nfcpass.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.nfcpass.data.remote.dto.ProfileDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_sessions")

/**
 * Manages Google Sign-In, Supabase auth, and multi-account session switching.
 */
class AuthManager(private val context: Context) {

    companion object {
        private const val TAG = "NFCPass:Auth"
        private val ACCOUNTS_KEY = stringPreferencesKey("saved_accounts")
        private val ACTIVE_EMAIL_KEY = stringPreferencesKey("active_email")

        // User must set this from Google Cloud Console
        var googleWebClientId: String = ""
    }

    @Serializable
    data class SavedAccount(
        val email: String,
        val displayName: String,
        val avatarUrl: String?,
        val refreshToken: String
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Initiates Google Sign-In via Credential Manager and signs into Supabase.
     */
    suspend fun signInWithGoogle(context: android.app.Activity): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(googleWebClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken

            Log.d(TAG, "Google Sign-In successful, signing into Supabase...")

            // Sign into Supabase with the Google ID token
            val supabase = SupabaseClientProvider.getClient()
            supabase.auth.signInWith(IDToken) {
                provider = Google
                this.idToken = idToken
            }

            val session = supabase.auth.currentSessionOrNull()
            val user = supabase.auth.currentUserOrNull()

            if (session != null && user != null) {
                val email = user.email ?: googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email
                val avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()

                // Save account session for multi-account switching
                saveAccount(SavedAccount(
                    email = email,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    refreshToken = session.refreshToken
                ))

                setActiveEmail(email)
                Log.d(TAG, "Signed in as $email")
                Result.success(user.id)
            } else {
                Result.failure(Exception("No session after sign-in"))
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in error", e)
            Result.failure(e)
        }
    }

    /**
     * Restores a session from a saved refresh token.
     */
    suspend fun restoreSession(): Boolean {
        return try {
            val activeEmail = getActiveEmail() ?: return false
            val accounts = getSavedAccounts()
            val account = accounts.find { it.email == activeEmail } ?: return false

            val supabase = SupabaseClientProvider.getClient()
            supabase.auth.refreshSession(account.refreshToken)

            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                // Update saved refresh token
                saveAccount(account.copy(refreshToken = session.refreshToken))
                Log.d(TAG, "Session restored for $activeEmail")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore session", e)
            false
        }
    }

    /**
     * Switches to a different saved account.
     */
    suspend fun switchAccount(email: String): Boolean {
        return try {
            val accounts = getSavedAccounts()
            val account = accounts.find { it.email == email } ?: return false

            // Reset and re-initialize client
            SupabaseClientProvider.resetClient()
            val supabase = SupabaseClientProvider.getClient()
            supabase.auth.refreshSession(account.refreshToken)

            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                saveAccount(account.copy(refreshToken = session.refreshToken))
                setActiveEmail(email)
                Log.d(TAG, "Switched to account: $email")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch account", e)
            false
        }
    }

    /**
     * Signs out current account (removes from saved accounts).
     */
    suspend fun signOut() {
        try {
            val activeEmail = getActiveEmail()
            val supabase = SupabaseClientProvider.getClient()
            supabase.auth.signOut()

            if (activeEmail != null) {
                removeAccount(activeEmail)
            }

            // Switch to another saved account if available
            val remaining = getSavedAccounts()
            if (remaining.isNotEmpty()) {
                switchAccount(remaining.first().email)
            } else {
                setActiveEmail(null)
            }

            Log.d(TAG, "Signed out")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        }
    }

    /**
     * Gets the current user's profile from Supabase.
     */
    suspend fun getCurrentProfile(): ProfileDto? {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val userId = supabase.auth.currentUserOrNull()?.id ?: return null
            supabase.postgrest["profiles"]
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<ProfileDto>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get profile", e)
            null
        }
    }

    /**
     * Updates the current user's profile role.
     */
    suspend fun updateProfileRole(role: String) {
        try {
            val supabase = SupabaseClientProvider.getClient()
            val userId = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.postgrest["profiles"]
                .update({ set("current_role", role) }) {
                    filter { eq("user_id", userId) }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update profile role", e)
        }
    }

    fun getCurrentUserId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    fun observeSavedAccounts(): Flow<List<SavedAccount>> {
        return context.authDataStore.data.map { prefs ->
            val accountsJson = prefs[ACCOUNTS_KEY] ?: "[]"
            json.decodeFromString(accountsJson)
        }
    }

    suspend fun getSavedAccounts(): List<SavedAccount> {
        val prefs = context.authDataStore.data.first()
        val accountsJson = prefs[ACCOUNTS_KEY] ?: "[]"
        return json.decodeFromString(accountsJson)
    }

    suspend fun getActiveEmail(): String? {
        val prefs = context.authDataStore.data.first()
        return prefs[ACTIVE_EMAIL_KEY]
    }

    private suspend fun setActiveEmail(email: String?) {
        context.authDataStore.edit { prefs ->
            if (email != null) {
                prefs[ACTIVE_EMAIL_KEY] = email
            } else {
                prefs.remove(ACTIVE_EMAIL_KEY)
            }
        }
    }

    private suspend fun saveAccount(account: SavedAccount) {
        context.authDataStore.edit { prefs ->
            val accounts = getSavedAccountsFromPrefs(prefs).toMutableList()
            accounts.removeAll { it.email == account.email }
            accounts.add(account)
            prefs[ACCOUNTS_KEY] = json.encodeToString(accounts)
        }
    }

    private suspend fun removeAccount(email: String) {
        context.authDataStore.edit { prefs ->
            val accounts = getSavedAccountsFromPrefs(prefs).toMutableList()
            accounts.removeAll { it.email == email }
            prefs[ACCOUNTS_KEY] = json.encodeToString(accounts)
        }
    }

    private fun getSavedAccountsFromPrefs(prefs: Preferences): List<SavedAccount> {
        val accountsJson = prefs[ACCOUNTS_KEY] ?: "[]"
        return json.decodeFromString(accountsJson)
    }
}
