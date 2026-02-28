package com.kryptoxotis.nexus.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivedCardDao {
    @Query("SELECT * FROM received_cards WHERE userId = :userId ORDER BY receivedAt DESC")
    fun observeByUser(userId: String): Flow<List<ReceivedCardEntity>>

    @Query("SELECT * FROM received_cards WHERE userId = :userId ORDER BY receivedAt DESC")
    suspend fun getByUser(userId: String): List<ReceivedCardEntity>

    @Query("SELECT * FROM received_cards WHERE id = :id")
    suspend fun getById(id: String): ReceivedCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: ReceivedCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<ReceivedCardEntity>)

    @Delete
    suspend fun delete(card: ReceivedCardEntity)

    @Query("DELETE FROM received_cards WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
