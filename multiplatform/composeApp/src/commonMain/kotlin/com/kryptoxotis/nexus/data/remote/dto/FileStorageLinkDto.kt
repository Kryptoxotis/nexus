package com.kryptoxotis.nexus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileStorageLinkDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("original_filename")
    val originalFilename: String,
    @SerialName("storage_path")
    val storagePath: String,
    @SerialName("public_url")
    val publicUrl: String? = null,
    @SerialName("file_size")
    val fileSize: Long? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
