package com.kryptoxotis.nexus.data.repository

import android.util.Log
import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.EnrollmentPinDto
import com.kryptoxotis.nexus.data.remote.dto.OrganizationDto
import com.kryptoxotis.nexus.domain.model.EnrollmentMode
import com.kryptoxotis.nexus.domain.model.Organization
import com.kryptoxotis.nexus.domain.model.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class OrganizationRepository {
    companion object {
        private const val TAG = "Nexus:OrgRepo"
    }

    private fun getCurrentUserId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrganizations(): Result<List<Organization>> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val orgs = supabase.postgrest["organizations"]
                .select { filter { eq("is_active", true) } }
                .decodeList<OrganizationDto>()

            val result = orgs.filter { it.id != null }.map { it.toDomain() }
            Result.Success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch organizations", e)
            Result.Error("Failed to load organizations: ${e.message}", e)
        }
    }

    suspend fun getMyOrganization(): Result<Organization?> {
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            val supabase = SupabaseClientProvider.getClient()
            val org = supabase.postgrest["organizations"]
                .select { filter { eq("owner_id", userId) } }
                .decodeSingleOrNull<OrganizationDto>()

            Result.Success(org?.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch my organization", e)
            Result.Error("Failed to load organization: ${e.message}", e)
        }
    }

    suspend fun createOrganization(
        name: String,
        type: String? = null,
        description: String? = null,
        enrollmentMode: EnrollmentMode = EnrollmentMode.OPEN
    ): Result<Organization> {
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            val supabase = SupabaseClientProvider.getClient()

            val dto = OrganizationDto(
                name = name,
                type = type,
                description = description,
                ownerId = userId,
                enrollmentMode = enrollmentMode.toDbString()
            )

            val created = supabase.postgrest["organizations"]
                .insert(dto) { select() }
                .decodeSingle<OrganizationDto>()

            Result.Success(created.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create organization", e)
            Result.Error("Failed to create organization: ${e.message}", e)
        }
    }

    suspend fun updateOrganization(
        orgId: String,
        name: String? = null,
        description: String? = null,
        enrollmentMode: EnrollmentMode? = null,
        staticPin: String? = null,
        allowSelfEnrollment: Boolean? = null
    ): Result<Unit> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["organizations"].update({
                name?.let { set("name", it) }
                description?.let { set("description", it) }
                enrollmentMode?.let { set("enrollment_mode", it.toDbString()) }
                staticPin?.let { set("static_pin", it) }
                allowSelfEnrollment?.let { set("allow_self_enrollment", it) }
            }) {
                filter { eq("id", orgId) }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update organization", e)
            Result.Error("Failed to update organization: ${e.message}", e)
        }
    }

    suspend fun createEnrollmentPin(orgId: String, pinCode: String, expiresAt: String? = null): Result<Unit> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.postgrest["enrollment_pins"].insert(EnrollmentPinDto(
                organizationId = orgId,
                pinCode = pinCode,
                expiresAt = expiresAt
            ))
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create enrollment pin", e)
            Result.Error("Failed to create pin: ${e.message}", e)
        }
    }

    suspend fun validateEnrollmentPin(orgId: String, pinCode: String): Result<Boolean> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            val pin = supabase.postgrest["enrollment_pins"]
                .select {
                    filter {
                        eq("organization_id", orgId)
                        eq("pin_code", pinCode)
                        eq("is_used", false)
                    }
                }
                .decodeSingleOrNull<EnrollmentPinDto>()

            Result.Success(pin != null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to validate pin", e)
            Result.Error("Failed to validate pin: ${e.message}", e)
        }
    }

    private fun OrganizationDto.toDomain(): Organization = Organization(
        id = id ?: throw IllegalStateException("Organization ID is null"),
        name = name,
        type = type,
        description = description,
        logoUrl = logoUrl,
        ownerId = ownerId,
        enrollmentMode = EnrollmentMode.fromString(enrollmentMode),
        staticPin = staticPin,
        allowSelfEnrollment = allowSelfEnrollment,
        isActive = isActive,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )
}
