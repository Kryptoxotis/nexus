package com.kryptoxotis.nexus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PersonalCardEntity::class, BusinessPassEntity::class],
    version = 6,
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
                    // Only wipe data when migrating from old pre-release schemas (1-5).
                    // For version 6+, add explicit Migration objects instead.
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
