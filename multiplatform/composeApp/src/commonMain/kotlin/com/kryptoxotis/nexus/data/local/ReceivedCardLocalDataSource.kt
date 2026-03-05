package com.kryptoxotis.nexus.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ReceivedCardLocalDataSource(private val db: NexusDatabase) {

    private val queries get() = db.receivedCardQueries

    fun observeByUser(userId: String): Flow<List<ReceivedCard>> =
        queries.selectAll(userId).asFlow().mapToList(Dispatchers.Default)

    fun getByUser(userId: String): List<ReceivedCard> =
        queries.selectAll(userId).executeAsList()

    fun getById(id: String): ReceivedCard? =
        queries.selectById(id).executeAsOneOrNull()

    fun insert(card: ReceivedCard) {
        queries.insert(
            id = card.id,
            userId = card.userId,
            name = card.name,
            jobTitle = card.jobTitle,
            company = card.company,
            phone = card.phone,
            email = card.email,
            website = card.website,
            linkedin = card.linkedin,
            instagram = card.instagram,
            twitter = card.twitter,
            github = card.github,
            facebook = card.facebook,
            youtube = card.youtube,
            tiktok = card.tiktok,
            discord = card.discord,
            twitch = card.twitch,
            whatsapp = card.whatsapp,
            notes = card.notes,
            receivedAt = card.receivedAt
        )
    }

    fun deleteById(id: String) {
        queries.deleteById(id)
    }
}
