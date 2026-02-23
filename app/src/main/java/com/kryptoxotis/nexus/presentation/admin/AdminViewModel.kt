package com.kryptoxotis.nexus.presentation.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.AllowedEmailDto
import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        // Auto-dismiss success/error states after 3 seconds
        viewModelScope.launch {
            _uiState.collect { state ->
                if (state is AdminUiState.Success || state is AdminUiState.Error) {
                    delay(3000)
                    if (_uiState.value == state) {
                        _uiState.value = AdminUiState.Idle
                    }
                }
            }
        }
    }

    private val _pendingRequests = MutableStateFlow<List<BusinessRequestDto>>(emptyList())
    val pendingRequests: StateFlow<List<BusinessRequestDto>> = _pendingRequests.asStateFlow()

    private val _users = MutableStateFlow<List<ProfileDto>>(emptyList())
    val users: StateFlow<List<ProfileDto>> = _users.asStateFlow()

    private val _organizations = MutableStateFlow<List<OrganizationDto>>(emptyList())
    val organizations: StateFlow<List<OrganizationDto>> = _organizations.asStateFlow()

    private val _allowedEmails = MutableStateFlow<List<AllowedEmailDto>>(emptyList())
    val allowedEmails: StateFlow<List<AllowedEmailDto>> = _allowedEmails.asStateFlow()

    companion object {
        private const val TAG = "Nexus:AdminVM"
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                val requests = supabase.postgrest["business_requests"]
                    .select { filter { eq("status", "pending") } }
                    .decodeList<BusinessRequestDto>()
                _pendingRequests.value = requests
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load pending requests", e)
                _uiState.value = AdminUiState.Error("Failed to load requests: ${e.message}")
            }
        }
    }

    fun approveRequest(requestId: String, userId: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                val adminId = supabase.auth.currentUserOrNull()?.id

                // Update request status
                supabase.postgrest["business_requests"].update({
                    set("status", "approved")
                    set("reviewed_by", adminId)
                    set("reviewed_at", java.time.Instant.now().toString())
                }) {
                    filter { eq("id", requestId) }
                }

                // Upgrade user to business account
                supabase.postgrest["profiles"].update({
                    set("account_type", "business")
                }) {
                    filter { eq("id", userId) }
                }

                _uiState.value = AdminUiState.Success("Request approved")
                loadPendingRequests()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to approve request", e)
                _uiState.value = AdminUiState.Error("Failed to approve: ${e.message}")
            }
        }
    }

    fun rejectRequest(requestId: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                val adminId = supabase.auth.currentUserOrNull()?.id

                supabase.postgrest["business_requests"].update({
                    set("status", "rejected")
                    set("reviewed_by", adminId)
                    set("reviewed_at", java.time.Instant.now().toString())
                }) {
                    filter { eq("id", requestId) }
                }

                _uiState.value = AdminUiState.Success("Request rejected")
                loadPendingRequests()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reject request", e)
                _uiState.value = AdminUiState.Error("Failed to reject: ${e.message}")
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                val profiles = supabase.postgrest["profiles"]
                    .select()
                    .decodeList<ProfileDto>()
                _users.value = profiles
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load users", e)
                _uiState.value = AdminUiState.Error("Failed to load users: ${e.message}")
            }
        }
    }

    fun updateUserStatus(userId: String, status: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest["profiles"].update({
                    set("status", status)
                }) {
                    filter { eq("id", userId) }
                }
                _uiState.value = AdminUiState.Success("User status updated")
                loadUsers()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update user status", e)
                _uiState.value = AdminUiState.Error("Failed to update user: ${e.message}")
            }
        }
    }

    fun changeAccountType(userId: String, newType: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest["profiles"].update({
                    set("account_type", newType)
                }) {
                    filter { eq("id", userId) }
                }
                _uiState.value = AdminUiState.Success("Account type changed to $newType")
                loadUsers()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to change account type", e)
                _uiState.value = AdminUiState.Error("Failed to change type: ${e.message}")
            }
        }
    }

    fun loadAllowedEmails() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                val emails = supabase.postgrest["allowed_emails"]
                    .select()
                    .decodeList<AllowedEmailDto>()
                _allowedEmails.value = emails
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load allowed emails", e)
            }
        }
    }

    fun createUser(email: String, fullName: String, accountType: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest.rpc("admin_add_allowed_email", buildJsonObject {
                    put("p_email", email)
                    put("p_full_name", fullName)
                    put("p_account_type", accountType)
                })
                _uiState.value = AdminUiState.Success("\"$fullName\" added")
                loadUsers()
                loadAllowedEmails()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add user", e)
                _uiState.value = AdminUiState.Error("Failed to add user: ${e.message}")
            }
        }
    }

    fun deleteAllowedEmail(emailId: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest["allowed_emails"].delete {
                    filter { eq("id", emailId) }
                }
                _uiState.value = AdminUiState.Success("Invite removed")
                loadAllowedEmails()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete allowed email", e)
                _uiState.value = AdminUiState.Error("Failed to remove invite: ${e.message}")
            }
        }
    }

    fun deleteUser(userId: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest.rpc("admin_delete_user", buildJsonObject {
                    put("p_user_id", userId)
                })
                _uiState.value = AdminUiState.Success("User deleted")
                loadUsers()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete user", e)
                _uiState.value = AdminUiState.Error("Failed to delete user: ${e.message}")
            }
        }
    }

    fun loadOrganizations() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                val orgs = supabase.postgrest["organizations"]
                    .select()
                    .decodeList<OrganizationDto>()
                _organizations.value = orgs
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load organizations", e)
                _uiState.value = AdminUiState.Error("Failed to load organizations: ${e.message}")
            }
        }
    }

    fun toggleOrganizationActive(orgId: String, isActive: Boolean) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest["organizations"].update({
                    set("is_active", isActive)
                }) {
                    filter { eq("id", orgId) }
                }
                _uiState.value = AdminUiState.Success(
                    if (isActive) "Organization activated" else "Organization deactivated"
                )
                loadOrganizations()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle organization", e)
                _uiState.value = AdminUiState.Error("Failed to update organization: ${e.message}")
            }
        }
    }

    fun createOrganization(
        name: String,
        type: String?,
        description: String?,
        ownerId: String,
        enrollmentMode: String
    ) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest["organizations"].insert(OrganizationDto(
                    name = name,
                    type = type,
                    description = description,
                    ownerId = ownerId,
                    enrollmentMode = enrollmentMode,
                    isActive = true
                ))
                _uiState.value = AdminUiState.Success("\"$name\" created")
                loadOrganizations()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create organization", e)
                _uiState.value = AdminUiState.Error("Failed to create organization: ${e.message}")
            }
        }
    }

    fun deleteOrganization(orgId: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.getClient()
                supabase.postgrest["organizations"].delete {
                    filter { eq("id", orgId) }
                }
                _uiState.value = AdminUiState.Success("Organization deleted")
                loadOrganizations()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete organization", e)
                _uiState.value = AdminUiState.Error("Failed to delete organization: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }
}

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}
