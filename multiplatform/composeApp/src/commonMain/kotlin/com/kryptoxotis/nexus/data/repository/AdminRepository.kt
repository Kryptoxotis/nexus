package com.kryptoxotis.nexus.data.repository

import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.AllowedEmailDto
import com.kryptoxotis.nexus.data.remote.dto.BusinessRequestDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.data.remote.dto.ProfileDto
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

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
        } catch (_: Exception) { null }
    }

    suspend fun loadPendingRequests(): Result<List<BusinessRequestDto>> {
        return try {
            val requests = SupabaseClientProvider.getClient().postgrest["business_requests"]
                .select { filter { eq("status", "pending") }; order("created_at", Order.ASCENDING); limit(REQUEST_PAGE_SIZE.toLong()) }
                .decodeList<BusinessRequestDto>()
            Result.Success(requests)
        } catch (e: Exception) {
            Result.Error("Failed to load requests. Please try again.", e)
        }
    }

    suspend fun approveRequest(request: BusinessRequestDto): Result<String> {
        if (request.userId.isBlank()) return Result.Error("User ID is missing")
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val adminId = getCurrentAdminId() ?: return Result.Error("Admin session expired.")
            val requestId = request.id ?: return Result.Error("Request ID is missing")

            var description: String? = null
            var enrollmentMode = "open"
            try {
                val msgJson = Json.parseToJsonElement(request.message ?: "").jsonObject
                description = msgJson["description"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }?.take(MAX_DESCRIPTION_LENGTH)
                val rawMode = msgJson["enrollmentMode"]?.jsonPrimitive?.contentOrNull?.ifBlank { "open" } ?: "open"
                enrollmentMode = if (rawMode in VALID_ENROLLMENT_MODES) rawMode else "open"
            } catch (_: Exception) {}

            val insertedOrg = supabase.postgrest["organizations"].insert(OrganizationDto(
                name = request.businessName, type = request.businessType,
                description = description, ownerId = request.userId,
                enrollmentMode = enrollmentMode, isActive = true
            )) { select() }.decodeSingle<OrganizationDto>()
            val insertedOrgId = insertedOrg.id

            val profileList = supabase.postgrest["profiles"]
                .select { filter { eq("id", request.userId) } }
                .decodeList<ProfileDto>()
            val originalProfile = profileList.firstOrNull()
            if (originalProfile == null) {
                try { if (!insertedOrgId.isNullOrBlank()) supabase.postgrest["organizations"].delete { filter { eq("id", insertedOrgId) } } } catch (_: Exception) {}
                return Result.Error("User profile not found.")
            }
            val originalAccountType = originalProfile.accountType

            try {
                supabase.postgrest["profiles"].update({ set("account_type", "business") }) { filter { eq("id", request.userId) } }

                val updated = supabase.postgrest["business_requests"].update({
                    set("status", "approved"); set("reviewed_by", adminId)
                }) { filter { eq("id", requestId); eq("status", "pending") }; select() }.decodeList<BusinessRequestDto>()

                if (updated.isEmpty()) {
                    try {
                        if (!insertedOrgId.isNullOrBlank()) supabase.postgrest["organizations"].delete { filter { eq("id", insertedOrgId) } }
                        supabase.postgrest["profiles"].update({ set("account_type", originalAccountType) }) { filter { eq("id", request.userId) } }
                    } catch (_: Exception) {}
                    return Result.Error("Request was already reviewed by another admin.")
                }
            } catch (e: Exception) {
                try {
                    if (!insertedOrgId.isNullOrBlank()) supabase.postgrest["organizations"].delete { filter { eq("id", insertedOrgId) } }
                    supabase.postgrest["profiles"].update({ set("account_type", originalAccountType) }) { filter { eq("id", request.userId) } }
                } catch (_: Exception) {}
                return Result.Error("Failed to approve request. Please try again.", e)
            }

            Result.Success("Request approved & organization created")
        } catch (e: Exception) {
            Result.Error("Failed to approve request. Please try again.", e)
        }
    }

    suspend fun rejectRequest(requestId: String): Result<String> {
        if (requestId.isBlank()) return Result.Error("Request ID is missing")
        return try {
            val adminId = getCurrentAdminId() ?: return Result.Error("Admin session expired.")
            val updated = SupabaseClientProvider.getClient().postgrest["business_requests"].update({
                set("status", "rejected"); set("reviewed_by", adminId)
            }) { filter { eq("id", requestId); eq("status", "pending") }; select() }.decodeList<BusinessRequestDto>()
            if (updated.isEmpty()) return Result.Error("Request was already reviewed by another admin.")
            Result.Success("Request rejected")
        } catch (e: Exception) {
            Result.Error("Failed to reject request. Please try again.", e)
        }
    }

    suspend fun loadUsers(): Result<List<ProfileDto>> {
        return try {
            val profiles = SupabaseClientProvider.getClient().postgrest["profiles"]
                .select { order("created_at", Order.DESCENDING); limit(USER_PAGE_SIZE.toLong()) }
                .decodeList<ProfileDto>()
            Result.Success(profiles)
        } catch (e: Exception) {
            Result.Error("Failed to load users. Please try again.", e)
        }
    }

    suspend fun updateUserStatus(userId: String, status: String): Result<String> {
        if (status !in VALID_USER_STATUSES) return Result.Error("Invalid status: $status")
        return try {
            SupabaseClientProvider.getClient().postgrest["profiles"].update({ set("status", status) }) { filter { eq("id", userId) } }
            Result.Success("User status updated")
        } catch (e: Exception) {
            Result.Error("Failed to update user. Please try again.", e)
        }
    }

    suspend fun changeAccountType(userId: String, newType: String): Result<String> {
        if (newType !in VALID_ACCOUNT_TYPES) return Result.Error("Invalid account type: $newType")
        return try {
            SupabaseClientProvider.getClient().postgrest["profiles"].update({ set("account_type", newType) }) { filter { eq("id", userId) } }
            Result.Success("Account type changed to $newType")
        } catch (e: Exception) {
            Result.Error("Failed to change account type. Please try again.", e)
        }
    }

    suspend fun loadAllowedEmails(): Result<List<AllowedEmailDto>> {
        return try {
            val emails = SupabaseClientProvider.getClient().postgrest["allowed_emails"]
                .select { order("created_at", Order.DESCENDING); limit(EMAIL_PAGE_SIZE.toLong()) }
                .decodeList<AllowedEmailDto>()
            Result.Success(emails)
        } catch (e: Exception) {
            Result.Error("Failed to load allowed emails. Please try again.", e)
        }
    }

    suspend fun createUser(email: String, fullName: String, accountType: String): Result<String> {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || !EMAIL_REGEX.matches(normalizedEmail)) return Result.Error("Invalid email address")
        if (fullName.trim().isBlank()) return Result.Error("Full name is required")
        if (accountType !in VALID_ACCOUNT_TYPES) return Result.Error("Invalid account type: $accountType")
        return try {
            SupabaseClientProvider.getClient().postgrest.rpc("admin_add_allowed_email", buildJsonObject {
                put("p_email", normalizedEmail); put("p_full_name", fullName.trim()); put("p_account_type", accountType)
            })
            Result.Success("User added successfully")
        } catch (e: Exception) {
            val msg = if (e.message?.contains("duplicate", ignoreCase = true) == true || e.message?.contains("23505") == true)
                "This email is already in the allowed list" else "Failed to add user. Please try again."
            Result.Error(msg, e)
        }
    }

    suspend fun deleteAllowedEmail(emailId: String): Result<String> {
        return try {
            SupabaseClientProvider.getClient().postgrest["allowed_emails"].delete { filter { eq("id", emailId) } }
            Result.Success("Invite removed")
        } catch (e: Exception) {
            Result.Error("Failed to remove invite. Please try again.", e)
        }
    }

    suspend fun deleteUser(userId: String): Result<String> {
        return try {
            SupabaseClientProvider.getClient().postgrest.rpc("admin_delete_user", buildJsonObject { put("p_user_id", userId) })
            Result.Success("User deleted")
        } catch (e: Exception) {
            Result.Error("Failed to delete user. Please try again.", e)
        }
    }

    suspend fun loadOrganizations(): Result<List<OrganizationDto>> {
        return try {
            val orgs = SupabaseClientProvider.getClient().postgrest["organizations"]
                .select { order("created_at", Order.DESCENDING); limit(ORG_PAGE_SIZE.toLong()) }
                .decodeList<OrganizationDto>()
            Result.Success(orgs)
        } catch (e: Exception) {
            Result.Error("Failed to load organizations. Please try again.", e)
        }
    }

    suspend fun toggleOrganizationActive(orgId: String, isActive: Boolean): Result<String> {
        return try {
            SupabaseClientProvider.getClient().postgrest["organizations"].update({ set("is_active", isActive) }) { filter { eq("id", orgId) } }
            Result.Success(if (isActive) "Organization activated" else "Organization deactivated")
        } catch (e: Exception) {
            Result.Error("Failed to update organization. Please try again.", e)
        }
    }

    suspend fun createOrganization(name: String, type: String?, description: String?, ownerId: String, enrollmentMode: String): Result<String> {
        if (name.isBlank()) return Result.Error("Organization name is required")
        if (enrollmentMode !in VALID_ENROLLMENT_MODES) return Result.Error("Invalid enrollment mode")
        return try {
            SupabaseClientProvider.getClient().postgrest["organizations"].insert(OrganizationDto(
                name = name, type = type, description = description?.take(MAX_DESCRIPTION_LENGTH),
                ownerId = ownerId, enrollmentMode = enrollmentMode, isActive = true
            ))
            Result.Success("Organization created")
        } catch (e: Exception) {
            Result.Error("Failed to create organization. Please try again.", e)
        }
    }

    suspend fun deleteOrganization(orgId: String): Result<String> {
        return try {
            SupabaseClientProvider.getClient().postgrest["organizations"].delete { filter { eq("id", orgId) } }
            Result.Success("Organization deleted")
        } catch (e: Exception) {
            Result.Error("Failed to delete organization. Please try again.", e)
        }
    }
}
