package com.example.arweld.core.data.seed

import com.example.arweld.core.data.db.dao.WorkItemDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbSeedInitializer @Inject constructor(
    private val workItemDao: WorkItemDao,
) {

    suspend fun seedIfEmpty() {
        if (workItemDao.countAll() == 0) {
            workItemDao.insertAll(SeedWorkItems.items)
        }
    }
}
