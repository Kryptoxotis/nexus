package com.kryptoxotis.nexus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PersonalCardEntity::class, BusinessPassEntity::class],
    version = 5,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun personalCardDao(): PersonalCardDao
    abstract fun businessPassDao(): BusinessPassDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_database"
                )
                    // TODO: Replace with proper migrations before production release.
                    // fallbackToDestructiveMigration() wipes ALL local data on schema changes.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
