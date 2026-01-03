package com.example.arweld.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE evidence ADD COLUMN workItemId TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE evidence ADD COLUMN sizeBytes INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_evidence_workItemId ON evidence(workItemId)")
    }
}
