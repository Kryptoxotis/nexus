package com.kryptoxotis.nexus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "received_cards")
data class ReceivedCardEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val linkedin: String = "",
    val instagram: String = "",
    val twitter: String = "",
    val github: String = "",
    val notes: String = "",
    val receivedAt: String
)
