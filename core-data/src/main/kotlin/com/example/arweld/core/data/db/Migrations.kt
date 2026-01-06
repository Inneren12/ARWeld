package com.example.arweld.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE evidence ADD COLUMN workItemId TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE evidence ADD COLUMN sizeBytes INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_evidence_workItemId ON evidence(workItemId)")
        database.execSQL(
            "UPDATE evidence SET workItemId = (SELECT workItemId FROM events WHERE events.id = evidence.eventId LIMIT 1) " +
                "WHERE workItemId = ''"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX IF NOT EXISTS index_work_items_code ON work_items(code)")
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_events_workItemId_timestamp ON events(workItemId, timestamp)"
        )
    }
}
