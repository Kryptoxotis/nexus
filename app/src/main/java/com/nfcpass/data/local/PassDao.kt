package com.nfcpass.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Pass entities.
 * Provides methods to interact with the local Room database.
 */
@Dao
interface PassDao {
    /**
     * Observes all passes for a specific user as a Flow.
     * Automatically updates when data changes in the database.
     */
    @Query("SELECT * FROM passes WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePassesByUser(userId: String): Flow<List<PassEntity>>

    /**
     * Gets all passes for a specific user (one-time query)
     */
    @Query("SELECT * FROM passes WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPassesByUser(userId: String): List<PassEntity>

    /**
     * Gets a single pass by its ID
     */
    @Query("SELECT * FROM passes WHERE id = :passId")
    suspend fun getPassById(passId: String): PassEntity?

    /**
     * Gets the currently active pass for a user (used for NFC emulation)
     * Only one pass can be active at a time.
     */
    @Query("SELECT * FROM passes WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActivePass(userId: String): PassEntity?

    /**
     * Observes the active pass as a Flow
     */
    @Query("SELECT * FROM passes WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun observeActivePass(userId: String): Flow<PassEntity?>

    /**
     * Inserts a new pass. If a pass with the same ID exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: PassEntity)

    /**
     * Inserts multiple passes at once
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasses(passes: List<PassEntity>)

    /**
     * Updates an existing pass
     */
    @Update
    suspend fun updatePass(pass: PassEntity)

    /**
     * Deletes a pass
     */
    @Delete
    suspend fun deletePass(pass: PassEntity)

    /**
     * Deactivates all passes for a user.
     * Used before activating a new pass to ensure only one is active.
     */
    @Query("UPDATE passes SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllPasses(userId: String)

    /**
     * Deletes all passes for a user (used on logout)
     */
    @Query("DELETE FROM passes WHERE userId = :userId")
    suspend fun deleteAllPassesByUser(userId: String)

    /**
     * Deletes all passes (used for testing/debugging)
     */
    @Query("DELETE FROM passes")
    suspend fun deleteAllPasses()
}
