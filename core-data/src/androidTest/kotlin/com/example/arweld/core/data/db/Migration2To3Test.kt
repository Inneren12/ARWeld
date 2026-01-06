package com.example.arweld.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration2To3Test {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration2To3_createsIndexes() {
        helper.createDatabase(testDb, 2).close()

        val migratedDb = helper.runMigrationsAndValidate(testDb, 3, true, MIGRATION_2_3)

        migratedDb.use { database ->
            val workItemIndexes = database.indexNames("work_items")
            assertTrue("index_work_items_code" in workItemIndexes)

            val eventIndexes = database.indexNames("events")
            assertTrue("index_events_workItemId_timestamp" in eventIndexes)

            val eventIndexColumns = database.indexColumns("index_events_workItemId_timestamp")
            assertEquals(listOf("workItemId", "timestamp"), eventIndexColumns)
        }
    }

    private fun SupportSQLiteDatabase.indexNames(tableName: String): List<String> {
        return query("PRAGMA index_list('$tableName')").use { cursor ->
            buildList {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
    }

    private fun SupportSQLiteDatabase.indexColumns(indexName: String): List<String> {
        return query("PRAGMA index_info('$indexName')").use { cursor ->
            buildList {
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
    }
}
