package com.nfcpass.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the NFC Pass app.
 * Stores passes locally for offline access and caching.
 *
 * Database version: 1
 * Entities: PassEntity
 */
@Database(
    entities = [PassEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PassDatabase : RoomDatabase() {
    /**
     * Provides access to Pass DAO
     */
    abstract fun passDao(): PassDao

    companion object {
        @Volatile
        private var INSTANCE: PassDatabase? = null

        /**
         * Gets the singleton instance of the database.
         * Creates it if it doesn't exist.
         *
         * @param context Application context
         * @return The database instance
         */
        fun getDatabase(context: Context): PassDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PassDatabase::class.java,
                    "nfc_pass_database"
                )
                    .fallbackToDestructiveMigration() // For development; in production, provide migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
