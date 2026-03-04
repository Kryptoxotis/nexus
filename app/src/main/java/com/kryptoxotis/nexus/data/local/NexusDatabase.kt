package com.kryptoxotis.nexus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PersonalCardEntity::class, BusinessPassEntity::class, ReceivedCardEntity::class],
    version = 9,
    exportSchema = true
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun personalCardDao(): PersonalCardDao
    abstract fun businessPassDao(): BusinessPassDao
    abstract fun receivedCardDao(): ReceivedCardDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE personal_cards ADD COLUMN stackId TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE received_cards ADD COLUMN facebook TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE received_cards ADD COLUMN youtube TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE received_cards ADD COLUMN tiktok TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE received_cards ADD COLUMN discord TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE received_cards ADD COLUMN twitch TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE received_cards ADD COLUMN whatsapp TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_database"
                )
                    // Only wipe data when migrating from old pre-release schemas (1-5).
                    // For version 6+, add explicit Migration objects instead.
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
