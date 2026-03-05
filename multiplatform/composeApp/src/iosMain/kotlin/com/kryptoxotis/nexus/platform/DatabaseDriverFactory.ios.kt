package com.kryptoxotis.nexus.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.kryptoxotis.nexus.data.local.NexusDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(NexusDatabase.Schema, "nexus_database")
}
