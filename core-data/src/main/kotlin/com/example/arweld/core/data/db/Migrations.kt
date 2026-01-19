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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_queue_new (
                id TEXT NOT NULL,
                type TEXT NOT NULL,
                eventType TEXT NOT NULL,
                workItemId TEXT,
                payloadJson TEXT NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO sync_queue_new (
                id,
                type,
                eventType,
                workItemId,
                payloadJson,
                status,
                createdAt
            )
            SELECT
                id,
                'EVENT' AS type,
                'UNKNOWN' AS eventType,
                NULL AS workItemId,
                payloadJson,
                status,
                createdAt
            FROM sync_queue
            """.trimIndent()
        )
        database.execSQL("DROP TABLE sync_queue")
        database.execSQL("ALTER TABLE sync_queue_new RENAME TO sync_queue")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_status ON sync_queue(status)")
    }
}
