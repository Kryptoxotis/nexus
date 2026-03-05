package com.kryptoxotis.nexus.platform

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.kryptoxotis.nexus.data.local.NexusDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(NexusDatabase.Schema, context, "nexus_database")
}
