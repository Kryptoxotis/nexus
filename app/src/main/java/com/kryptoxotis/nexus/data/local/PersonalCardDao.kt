package com.kryptoxotis.nexus.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalCardDao {
    @Query("SELECT * FROM personal_cards WHERE userId = :userId ORDER BY orderIndex ASC, createdAt DESC")
    fun observeCardsByUser(userId: String): Flow<List<PersonalCardEntity>>

    @Query("SELECT * FROM personal_cards WHERE userId = :userId ORDER BY orderIndex ASC, createdAt DESC")
    suspend fun getCardsByUser(userId: String): List<PersonalCardEntity>

    @Query("SELECT * FROM personal_cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): PersonalCardEntity?

    @Query("SELECT * FROM personal_cards WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveCard(userId: String): PersonalCardEntity?

    @Query("SELECT * FROM personal_cards WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun observeActiveCard(userId: String): Flow<PersonalCardEntity?>

    @Query("SELECT * FROM personal_cards ORDER BY orderIndex ASC, createdAt DESC")
    fun observeAllCards(): Flow<List<PersonalCardEntity>>

    @Query("SELECT * FROM personal_cards WHERE isActive = 1")
    fun observeAllActiveCards(): Flow<List<PersonalCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: PersonalCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<PersonalCardEntity>)

    @Update
    suspend fun updateCard(card: PersonalCardEntity)

    @Delete
    suspend fun deleteCard(card: PersonalCardEntity)

    @Query("UPDATE personal_cards SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllCards(userId: String)

    @Query("UPDATE personal_cards SET isActive = 0")
    suspend fun deactivateAll()

    @Transaction
    suspend fun activateCardAtomically(userId: String, cardId: String) {
        deactivateAllCards(userId)
        val card = getCardById(cardId)
        if (card != null) {
            updateCard(card.copy(isActive = true, updatedAt = java.time.Instant.now().toString()))
        }
    }

    @Query("DELETE FROM personal_cards WHERE userId = :userId")
    suspend fun deleteAllCardsByUser(userId: String)

    @Query("DELETE FROM personal_cards")
    suspend fun deleteAllCards()
}
