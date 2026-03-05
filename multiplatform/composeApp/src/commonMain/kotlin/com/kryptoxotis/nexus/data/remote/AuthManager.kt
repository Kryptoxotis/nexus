package com.kryptoxotis.nexus.data.remote

import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import com.kryptoxotis.nexus.domain.model.AccountType
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import com.russhwolf.settings.Settings
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthManager {
    companion object {
        private const val TAG = "Nexus:Auth"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts"
        private const val KEY_ACTIVE_EMAIL = "active_email"
    }

    @Serializable
    data class SavedAccount(
        val email: String,
        val refreshToken: String,
        val displayName: String? = null,
        val avatarUrl: String? = null
    )

    private val settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    private val _savedAccounts = MutableStateFlow<List<SavedAccount>>(emptyList())

    init {
        _savedAccounts.value = loadSavedAccounts()
    }

    fun observeSavedAccounts(): Flow<List<SavedAccount>> = _savedAccounts.asStateFlow()
    fun getSavedAccounts(): List<SavedAccount> = _savedAccounts.value

    fun getCurrentUserId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (_: Exception) { null }
    }

    suspend fun restoreSession(): Boolean {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val activeEmail = settings.getStringOrNull(KEY_ACTIVE_EMAIL)
            val accounts = loadSavedAccounts()

            val account = if (activeEmail != null) {
                accounts.find { it.email == activeEmail }
            } else {
                accounts.firstOrNull()
            }

            if (account != null) {
                supabase.auth.refreshSession(account.refreshToken)
                saveCurrentSession(account.email)
                true
            } else {
                supabase.auth.currentUserOrNull() != null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to restore session", e)
            false
        }
    }

    suspend fun signInWithIdToken(idToken: String): kotlin.Result<Unit> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
            val email = supabase.auth.currentUserOrNull()?.email ?: ""
            saveCurrentSession(email)
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Sign in failed", e)
            kotlin.Result.failure(e)
        }
    }

    suspend fun getCurrentProfile(): ProfileDto? {
        return try {
            val userId = getCurrentUserId() ?: return null
            SupabaseClientProvider.getClient().postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<ProfileDto>()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to get profile", e)
            null
        }
    }

    suspend fun createProfileIfMissing(): ProfileDto? {
        return try {
            val user = SupabaseClientProvider.getClient().auth.currentUserOrNull() ?: return null
            val existing = getCurrentProfile()
            if (existing != null) return existing

            val dto = ProfileDto(
                id = user.id,
                email = user.email,
                fullName = user.userMetadata?.get("full_name")?.toString()?.trim('"'),
                avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.trim('"')
            )
            SupabaseClientProvider.getClient().postgrest["profiles"].insert(dto)
            getCurrentProfile()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create profile", e)
            null
        }
    }

    suspend fun getBusinessRequestStatus(): BusinessRequestDto? {
        return try {
            val userId = getCurrentUserId() ?: return null
            SupabaseClientProvider.getClient().postgrest["business_requests"]
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<BusinessRequestDto>()
        } catch (_: Exception) { null }
    }

    suspend fun submitBusinessRequest(
        businessName: String, businessType: String?,
        contactEmail: String?, message: String?
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            SupabaseClientProvider.getClient().postgrest["business_requests"].insert(
                BusinessRequestDto(
                    userId = userId, businessName = businessName,
                    businessType = businessType, contactEmail = contactEmail, message = message
                )
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to submit request: ${e.message}", e)
        }
    }

    suspend fun updateAccountType(type: AccountType) {
        try {
            val userId = getCurrentUserId() ?: return
            SupabaseClientProvider.getClient().postgrest["profiles"]
                .update({ set("account_type", type.toDbString()) }) {
                    filter { eq("id", userId) }
                }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to update account type", e)
        }
    }

    suspend fun switchAccount(email: String): Boolean {
        return try {
            val accounts = loadSavedAccounts()
            val account = accounts.find { it.email == email } ?: return false
            SupabaseClientProvider.resetClient()
            SupabaseClientProvider.getClient().auth.refreshSession(account.refreshToken)
            settings.putString(KEY_ACTIVE_EMAIL, email)
            saveCurrentSession(email)
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to switch account", e)
            false
        }
    }

    suspend fun signOut() {
        try {
            val email = SupabaseClientProvider.getClient().auth.currentUserOrNull()?.email
            SupabaseClientProvider.getClient().auth.signOut()
            if (email != null) {
                val accounts = loadSavedAccounts().filter { it.email != email }
                saveSavedAccounts(accounts)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to sign out", e)
        }
    }

    private fun saveCurrentSession(email: String) {
        try {
            val session = SupabaseClientProvider.getClient().auth.currentSessionOrNull()
            val refreshToken = session?.refreshToken ?: return
            val user = SupabaseClientProvider.getClient().auth.currentUserOrNull()

            val accounts = loadSavedAccounts().toMutableList()
            accounts.removeAll { it.email == email }
            accounts.add(0, SavedAccount(
                email = email,
                refreshToken = refreshToken,
                displayName = user?.userMetadata?.get("full_name")?.toString()?.trim('"'),
                avatarUrl = user?.userMetadata?.get("avatar_url")?.toString()?.trim('"')
            ))
            saveSavedAccounts(accounts)
            settings.putString(KEY_ACTIVE_EMAIL, email)
            _savedAccounts.value = accounts
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to save session", e)
        }
    }

    private fun loadSavedAccounts(): List<SavedAccount> {
        return try {
            val raw = settings.getStringOrNull(KEY_SAVED_ACCOUNTS) ?: return emptyList()
            json.decodeFromString<List<SavedAccount>>(raw)
        } catch (_: Exception) { emptyList() }
    }

    private fun saveSavedAccounts(accounts: List<SavedAccount>) {
        settings.putString(KEY_SAVED_ACCOUNTS, json.encodeToString(accounts))
        _savedAccounts.value = accounts
    }
}
