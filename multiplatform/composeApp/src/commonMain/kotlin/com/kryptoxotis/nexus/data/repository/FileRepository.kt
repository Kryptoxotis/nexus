package com.kryptoxotis.nexus.data.repository

import com.kryptoxotis.nexus.data.remote.SupabaseClientProvider
import com.kryptoxotis.nexus.data.remote.dto.FileStorageLinkDto
import com.kryptoxotis.nexus.domain.model.Result
import com.kryptoxotis.nexus.platform.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FileRepository {
    companion object {
        private const val TAG = "Nexus:FileRepo"
        private const val BUCKET_NAME = "user-files"
    }

    private fun getCurrentUserId(): String? {
        return try {
            SupabaseClientProvider.getClient().auth.currentUserOrNull()?.id
        } catch (_: Exception) { null }
    }

    suspend fun uploadFile(
        fileBytes: ByteArray,
        originalFilename: String,
        mimeType: String? = null
    ): Result<String> {
        if (fileBytes.size > 10 * 1024 * 1024) return Result.Error("File must be under 10 MB")
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            val supabase = SupabaseClientProvider.getClient()
            val storagePath = "$userId/${Clock.System.now().toEpochMilliseconds()}_$originalFilename"

            supabase.storage.from(BUCKET_NAME).upload(storagePath, fileBytes)
            val publicUrl = supabase.storage.from(BUCKET_NAME).publicUrl(storagePath)

            supabase.postgrest["file_storage_links"].insert(FileStorageLinkDto(
                userId = userId, originalFilename = originalFilename,
                storagePath = storagePath, publicUrl = publicUrl,
                fileSize = fileBytes.size.toLong(), mimeType = mimeType
            ))
            Result.Success(publicUrl)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to upload file", e)
            Result.Error("Failed to upload file: ${e.message}", e)
        }
    }

    suspend fun getUserFiles(): Result<List<FileStorageLinkDto>> {
        return try {
            val userId = getCurrentUserId() ?: return Result.Error("Not authenticated")
            val files = SupabaseClientProvider.getClient().postgrest["file_storage_links"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<FileStorageLinkDto>()
            Result.Success(files)
        } catch (e: Exception) {
            Result.Error("Failed to load files: ${e.message}", e)
        }
    }

    suspend fun deleteFile(fileId: String, storagePath: String): Result<Unit> {
        return try {
            val supabase = SupabaseClientProvider.getClient()
            supabase.storage.from(BUCKET_NAME).delete(storagePath)
            supabase.postgrest["file_storage_links"].delete { filter { eq("id", fileId) } }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete file: ${e.message}", e)
        }
    }
}
