package com.kryptoxotis.nexus.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessPassDao {
    @Query("SELECT * FROM business_passes WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePassesByUser(userId: String): Flow<List<BusinessPassEntity>>

    @Query("SELECT * FROM business_passes WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPassesByUser(userId: String): List<BusinessPassEntity>

    @Query("SELECT * FROM business_passes WHERE id = :passId")
    suspend fun getPassById(passId: String): BusinessPassEntity?

    @Query("SELECT * FROM business_passes WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun observePassesByOrganization(orgId: String): Flow<List<BusinessPassEntity>>

    @Query("SELECT * FROM business_passes WHERE organizationId = :orgId ORDER BY createdAt DESC")
    suspend fun getPassesByOrganization(orgId: String): List<BusinessPassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: BusinessPassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasses(passes: List<BusinessPassEntity>)

    @Update
    suspend fun updatePass(pass: BusinessPassEntity)

    @Delete
    suspend fun deletePass(pass: BusinessPassEntity)

    @Query("DELETE FROM business_passes WHERE userId = :userId")
    suspend fun deleteAllPassesByUser(userId: String)
}
