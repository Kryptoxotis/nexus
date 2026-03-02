package com.kryptoxotis.nexus.data.repository

import android.util.Log
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.AllowedEmailDto
import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import com.kryptoxotis.nexus.domain.model.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * Repository for admin-only Supabase operations (user management, org lifecycle, business request review).
 * All suspend functions are main-safe and return [Result].
 */
class AdminRepository {

    companion object {
        private const val TAG = "Nexus:AdminRepo"
        internal val VALID_ACCOUNT_TYPES = setOf("individual", "business")
        internal val VALID_USER_STATUSES = setOf("active", "suspended")
        internal val VALID_ENROLLMENT_MODES = setOf("open", "pin", "invite", "closed")
        private const val REQUEST_PAGE_SIZE = 50
        private const val USER_PAGE_SIZE = 100
        private const val EMAIL_PAGE_SIZE = 200
        private const val ORG_PAGE_SIZE = 100
        private const val MAX_DESCRIPTION_LENGTH = 500
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }

    private fun getCurrentAdminId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get current admin ID", e)
            null
        }
    }

    /** Fetches up to [REQUEST_PAGE_SIZE] business requests with status "pending", oldest first. */
    suspend fun loadPendingRequests(): Result<List<BusinessRequestDto>> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val requests = supabase.postgrest["business_requests"]
                .select {
                    filter { eq("status", "pending") }
                    order("created_at", Order.ASCENDING)
                    limit(REQUEST_PAGE_SIZE.toLong())
                }
                .decodeList<BusinessRequestDto>()
            if (requests.size >= REQUEST_PAGE_SIZE) {
                Log.w(TAG, "Pending requests list truncated at $REQUEST_PAGE_SIZE")
            }
            Result.Success(requests)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load pending requests", e)
            Result.Error("Failed to load requests. Please try again.", e)
        }
    }

    /**
     * Approves a business request in three steps:
     * (1) create organization, (2) upgrade user profile to "business", (3) mark request approved.
     * Steps 2/3 are rolled back on failure.
     */
    suspend fun approveRequest(request: BusinessRequestDto): Result<String> {
        if (request.userId.isBlank()) return Result.Error("User ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val adminId = getCurrentAdminId()
                ?: return Result.Error("Admin session expired. Please sign in again.")
            val requestId = request.id
                ?: return Result.Error("Request ID is missing")

            // Parse org details from message JSON using kotlinx.serialization
            var description: String? = null
            var enrollmentMode = "open"
            try {
                val msgJson = Json.parseToJsonElement(request.message ?: "").jsonObject
                description = msgJson["description"]?.jsonPrimitive?.contentOrNull
                    ?.ifBlank { null }?.take(MAX_DESCRIPTION_LENGTH)
                val rawMode = msgJson["enrollmentMode"]?.jsonPrimitive?.contentOrNull
                    ?.ifBlank { "open" } ?: "open"
                enrollmentMode = if (rawMode in VALID_ENROLLMENT_MODES) rawMode else "open"
            } catch (_: Exception) {
                /* Legacy plain-text message or malformed JSON — fall back to defaults */
            }

            // Step 1: Create organization first, capture ID for precise rollback
            val insertedOrg = supabase.postgrest["organizations"].insert(OrganizationDto(
                name = request.businessName,
                type = request.businessType,
                description = description,
                ownerId = request.userId,
                enrollmentMode = enrollmentMode,
                isActive = true
            )) { select() }.decodeSingle<OrganizationDto>()
            val insertedOrgId = insertedOrg.id

            // Capture original account type — use decodeList to handle missing profile safely
            val profileList = supabase.postgrest["profiles"]
                .select { filter { eq("id", request.userId) } }
                .decodeList<ProfileDto>()
            val originalProfile = profileList.firstOrNull()
            if (originalProfile == null) {
                // Profile missing — clean up the org we just created
                try {
                    if (!insertedOrgId.isNullOrBlank()) {
                        supabase.postgrest["organizations"].delete {
                            filter { eq("id", insertedOrgId) }
                        }
                    }
                } catch (rollbackEx: Exception) {
                    Log.e(TAG, "Rollback failed after missing profile", rollbackEx)
                }
                return Result.Error("User profile not found. Request cannot be approved.")
            }
            val originalAccountType = originalProfile.accountType ?: "individual"

            try {
                // Step 2: Upgrade user to business account
                supabase.postgrest["profiles"].update({
                    set("account_type", "business")
                }) {
                    filter { eq("id", request.userId) }
                }

                // Step 3: Mark request as approved — only if still pending (concurrent admin guard)
                val updated = supabase.postgrest["business_requests"].update({
                    set("status", "approved")
                    set("reviewed_by", adminId)
                }) {
                    filter {
                        eq("id", requestId)
                        eq("status", "pending")
                    }
                    select()
                }.decodeList<BusinessRequestDto>()

                // Verify the update actually matched a row — detect concurrent admin race
                if (updated.isEmpty()) {
                    // Another admin already processed this request — roll back
                    try {
                        if (!insertedOrgId.isNullOrBlank()) {
                            supabase.postgrest["organizations"].delete {
                                filter { eq("id", insertedOrgId) }
                            }
                        }
                        supabase.postgrest["profiles"].update({
                            set("account_type", originalAccountType)
                        }) {
                            filter { eq("id", request.userId) }
                        }
                    } catch (rollbackEx: Exception) {
                        Log.e(TAG, "Rollback after stale approval failed", rollbackEx)
                    }
                    return Result.Error("Request was already reviewed by another admin.")
                }
            } catch (e: Exception) {
                // Rollback: delete the org created in step 1 and revert profile to original type
                Log.e(TAG, "Approval step 2/3 failed, rolling back", e)
                var rollbackFailed = false
                try {
                    if (!insertedOrgId.isNullOrBlank()) {
                        supabase.postgrest["organizations"].delete {
                            filter { eq("id", insertedOrgId) }
                        }
                    }
                    supabase.postgrest["profiles"].update({
                        set("account_type", originalAccountType)
                    }) {
                        filter { eq("id", request.userId) }
                    }
                } catch (rollbackEx: Exception) {
                    Log.e(TAG, "Rollback failed — orphan data may exist, check dashboard", rollbackEx)
                    rollbackFailed = true
                }
                return if (rollbackFailed) {
                    Result.Error("Approval failed and rollback incomplete. Check dashboard for orphaned data.", e)
                } else {
                    Result.Error("Failed to approve request. Please try again.", e)
                }
            }

            Result.Success("Request approved & organization created")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to approve request", e)
            Result.Error("Failed to approve request. Please try again.", e)
        }
    }

    /** Rejects a pending business request and records the current admin as reviewer. */
    suspend fun rejectRequest(requestId: String): Result<String> {
        if (requestId.isBlank()) return Result.Error("Request ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val adminId = getCurrentAdminId()
                ?: return Result.Error("Admin session expired. Please sign in again.")

            val updated = supabase.postgrest["business_requests"].update({
                set("status", "rejected")
                set("reviewed_by", adminId)
            }) {
                filter {
                    eq("id", requestId)
                    eq("status", "pending")
                }
                select()
            }.decodeList<BusinessRequestDto>()

            if (updated.isEmpty()) {
                return Result.Error("Request was already reviewed by another admin.")
            }

            Result.Success("Request rejected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject request", e)
            Result.Error("Failed to reject request. Please try again.", e)
        }
    }

    suspend fun loadUsers(): Result<List<ProfileDto>> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val profiles = supabase.postgrest["profiles"]
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(USER_PAGE_SIZE.toLong())
                }
                .decodeList<ProfileDto>()
            if (profiles.size >= USER_PAGE_SIZE) {
                Log.w(TAG, "User list truncated at $USER_PAGE_SIZE")
            }
            Result.Success(profiles)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load users", e)
            Result.Error("Failed to load users. Please try again.", e)
        }
    }

    suspend fun updateUserStatus(userId: String, status: String): Result<String> {
        if (userId.isBlank()) return Result.Error("User ID is missing")
        if (status !in VALID_USER_STATUSES) return Result.Error("Invalid status: $status")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["profiles"].update({
                set("status", status)
            }) {
                filter { eq("id", userId) }
            }
            Result.Success("User status updated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user status", e)
            Result.Error("Failed to update user. Please try again.", e)
        }
    }

    suspend fun changeAccountType(userId: String, newType: String): Result<String> {
        if (userId.isBlank()) return Result.Error("User ID is missing")
        if (newType !in VALID_ACCOUNT_TYPES) return Result.Error("Invalid account type: $newType")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["profiles"].update({
                set("account_type", newType)
            }) {
                filter { eq("id", userId) }
            }
            Result.Success("Account type changed to $newType")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change account type", e)
            Result.Error("Failed to change account type. Please try again.", e)
        }
    }

    suspend fun loadAllowedEmails(): Result<List<AllowedEmailDto>> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val emails = supabase.postgrest["allowed_emails"]
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(EMAIL_PAGE_SIZE.toLong())
                }
                .decodeList<AllowedEmailDto>()
            if (emails.size >= EMAIL_PAGE_SIZE) {
                Log.w(TAG, "Allowed emails list truncated at $EMAIL_PAGE_SIZE")
            }
            Result.Success(emails)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load allowed emails", e)
            Result.Error("Failed to load allowed emails. Please try again.", e)
        }
    }

    /**
     * Adds an email to the allowed-emails list via the [admin_add_allowed_email] RPC.
     * Does not create a user account directly; the user must still sign up.
     */
    suspend fun createUser(email: String, fullName: String, accountType: String): Result<String> {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || normalizedEmail.length > 254 || !EMAIL_REGEX.matches(normalizedEmail)) return Result.Error("Invalid email address")
        val trimmedName = fullName.trim()
        if (trimmedName.isBlank()) return Result.Error("Full name is required")
        if (accountType !in VALID_ACCOUNT_TYPES) return Result.Error("Invalid account type: $accountType")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest.rpc("admin_add_allowed_email", buildJsonObject {
                put("p_email", normalizedEmail)
                put("p_full_name", trimmedName)
                put("p_account_type", accountType)
            })
            Result.Success("User added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add user", e)
            val msg = if (e.message?.contains("duplicate", ignoreCase = true) == true ||
                          e.message?.contains("unique", ignoreCase = true) == true ||
                          e.message?.contains("23505") == true) {
                "This email is already in the allowed list"
            } else {
                "Failed to add user. Please try again."
            }
            Result.Error(msg, e)
        }
    }

    suspend fun deleteAllowedEmail(emailId: String): Result<String> {
        if (emailId.isBlank()) return Result.Error("Email ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["allowed_emails"].delete {
                filter { eq("id", emailId) }
            }
            Result.Success("Invite removed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete allowed email", e)
            Result.Error("Failed to remove invite. Please try again.", e)
        }
    }

    suspend fun deleteUser(userId: String): Result<String> {
        if (userId.isBlank()) return Result.Error("User ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest.rpc("admin_delete_user", buildJsonObject {
                put("p_user_id", userId)
            })
            Result.Success("User deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user", e)
            Result.Error("Failed to delete user. Please try again.", e)
        }
    }

    suspend fun loadOrganizations(): Result<List<OrganizationDto>> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val orgs = supabase.postgrest["organizations"]
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(ORG_PAGE_SIZE.toLong())
                }
                .decodeList<OrganizationDto>()
            if (orgs.size >= ORG_PAGE_SIZE) {
                Log.w(TAG, "Organizations list truncated at $ORG_PAGE_SIZE")
            }
            Result.Success(orgs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load organizations", e)
            Result.Error("Failed to load organizations. Please try again.", e)
        }
    }

    suspend fun toggleOrganizationActive(orgId: String, isActive: Boolean): Result<String> {
        if (orgId.isBlank()) return Result.Error("Organization ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["organizations"].update({
                set("is_active", isActive)
            }) {
                filter { eq("id", orgId) }
            }
            Result.Success(if (isActive) "Organization activated" else "Organization deactivated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle organization", e)
            Result.Error("Failed to update organization. Please try again.", e)
        }
    }

    suspend fun createOrganization(
        name: String,
        type: String?,
        description: String?,
        ownerId: String,
        enrollmentMode: String
    ): Result<String> {
        if (name.isBlank()) return Result.Error("Organization name is required")
        if (name.length > 200) return Result.Error("Organization name is too long (max 200 characters)")
        if (ownerId.isBlank()) return Result.Error("Owner ID is required")
        if (enrollmentMode !in VALID_ENROLLMENT_MODES) return Result.Error("Invalid enrollment mode: $enrollmentMode")
        val safeDesc = description?.take(MAX_DESCRIPTION_LENGTH)
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["organizations"].insert(OrganizationDto(
                name = name,
                type = type,
                description = safeDesc,
                ownerId = ownerId,
                enrollmentMode = enrollmentMode,
                isActive = true
            ))
            Result.Success("Organization created")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create organization", e)
            Result.Error("Failed to create organization. Please try again.", e)
        }
    }

    suspend fun deleteOrganization(orgId: String): Result<String> {
        if (orgId.isBlank()) return Result.Error("Organization ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["organizations"].delete {
                filter { eq("id", orgId) }
            }
            Result.Success("Organization deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete organization", e)
            Result.Error("Failed to delete organization. Please try again.", e)
        }
    }
}
