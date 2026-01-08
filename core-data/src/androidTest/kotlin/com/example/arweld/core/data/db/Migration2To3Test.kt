package com.example.arweld.core.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2To3Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val dbName = "migration-test"

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(dbName)
    }

    @Test
    fun migratesFrom2To3_andCreatesIndexes() {
        helper.createDatabase(dbName, 2).close()

        helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_2_3).use { db ->
            val workItemIndexes = db.indexNames("work_items")
            val eventIndexes = db.indexNames("events")

            assertTrue(workItemIndexes.contains("index_work_items_code"))
            assertTrue(eventIndexes.contains("index_events_workItemId_timestamp"))
        }
    }

    private fun SupportSQLiteDatabase.indexNames(table: String): List<String> {
        val names = mutableListOf<String>()
        query("PRAGMA index_list('$table')").use { cursor ->
            val nameColumn = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                names.add(cursor.getString(nameColumn))
            }
        }
        return names
    }
}
