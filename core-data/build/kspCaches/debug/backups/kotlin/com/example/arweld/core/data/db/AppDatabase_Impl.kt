package com.example.arweld.core.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.arweld.core.`data`.db.dao.EventDao
import com.example.arweld.core.`data`.db.dao.EventDao_Impl
import com.example.arweld.core.`data`.db.dao.EvidenceDao
import com.example.arweld.core.`data`.db.dao.EvidenceDao_Impl
import com.example.arweld.core.`data`.db.dao.SyncQueueDao
import com.example.arweld.core.`data`.db.dao.SyncQueueDao_Impl
import com.example.arweld.core.`data`.db.dao.UserDao
import com.example.arweld.core.`data`.db.dao.UserDao_Impl
import com.example.arweld.core.`data`.db.dao.WorkItemDao
import com.example.arweld.core.`data`.db.dao.WorkItemDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _workItemDao: Lazy<WorkItemDao> = lazy {
    WorkItemDao_Impl(this)
  }

  private val _eventDao: Lazy<EventDao> = lazy {
    EventDao_Impl(this)
  }

  private val _evidenceDao: Lazy<EvidenceDao> = lazy {
    EvidenceDao_Impl(this)
  }

  private val _userDao: Lazy<UserDao> = lazy {
    UserDao_Impl(this)
  }

  private val _syncQueueDao: Lazy<SyncQueueDao> = lazy {
    SyncQueueDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3,
        "8ab5cd169cd240300682d1412b7f0b85", "424b44e1442115ccd773b705df061232") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `work_items` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `zoneId` TEXT, `type` TEXT NOT NULL, `code` TEXT, `description` TEXT, `nodeId` TEXT, `createdAt` INTEGER, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_work_items_code` ON `work_items` (`code`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `events` (`id` TEXT NOT NULL, `workItemId` TEXT NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `actorId` TEXT NOT NULL, `actorRole` TEXT NOT NULL, `deviceId` TEXT NOT NULL, `payloadJson` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_events_workItemId` ON `events` (`workItemId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_events_actorId` ON `events` (`actorId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_events_workItemId_timestamp` ON `events` (`workItemId`, `timestamp`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `evidence` (`id` TEXT NOT NULL, `workItemId` TEXT NOT NULL, `eventId` TEXT NOT NULL, `kind` TEXT NOT NULL, `uri` TEXT NOT NULL, `sha256` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `metaJson` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_evidence_eventId` ON `evidence` (`eventId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_evidence_workItemId` ON `evidence` (`workItemId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `name` TEXT, `role` TEXT NOT NULL, `lastSeenAt` INTEGER, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`id` TEXT NOT NULL, `payloadJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `retryCount` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_queue_status` ON `sync_queue` (`status`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8ab5cd169cd240300682d1412b7f0b85')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `work_items`")
        connection.execSQL("DROP TABLE IF EXISTS `events`")
        connection.execSQL("DROP TABLE IF EXISTS `evidence`")
        connection.execSQL("DROP TABLE IF EXISTS `users`")
        connection.execSQL("DROP TABLE IF EXISTS `sync_queue`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsWorkItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkItems.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("projectId", TableInfo.Column("projectId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("zoneId", TableInfo.Column("zoneId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("code", TableInfo.Column("code", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("description", TableInfo.Column("description", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("nodeId", TableInfo.Column("nodeId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkItems.put("createdAt", TableInfo.Column("createdAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkItems: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWorkItems.add(TableInfo.Index("index_work_items_code", false, listOf("code"),
            listOf("ASC")))
        val _infoWorkItems: TableInfo = TableInfo("work_items", _columnsWorkItems,
            _foreignKeysWorkItems, _indicesWorkItems)
        val _existingWorkItems: TableInfo = read(connection, "work_items")
        if (!_infoWorkItems.equals(_existingWorkItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |work_items(com.example.arweld.core.data.db.entity.WorkItemEntity).
              | Expected:
              |""".trimMargin() + _infoWorkItems + """
              |
              | Found:
              |""".trimMargin() + _existingWorkItems)
        }
        val _columnsEvents: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEvents.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("workItemId", TableInfo.Column("workItemId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("actorId", TableInfo.Column("actorId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("actorRole", TableInfo.Column("actorRole", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvents.put("payloadJson", TableInfo.Column("payloadJson", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEvents: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEvents: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesEvents.add(TableInfo.Index("index_events_workItemId", false, listOf("workItemId"),
            listOf("ASC")))
        _indicesEvents.add(TableInfo.Index("index_events_actorId", false, listOf("actorId"),
            listOf("ASC")))
        _indicesEvents.add(TableInfo.Index("index_events_workItemId_timestamp", false,
            listOf("workItemId", "timestamp"), listOf("ASC", "ASC")))
        val _infoEvents: TableInfo = TableInfo("events", _columnsEvents, _foreignKeysEvents,
            _indicesEvents)
        val _existingEvents: TableInfo = read(connection, "events")
        if (!_infoEvents.equals(_existingEvents)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |events(com.example.arweld.core.data.db.entity.EventEntity).
              | Expected:
              |""".trimMargin() + _infoEvents + """
              |
              | Found:
              |""".trimMargin() + _existingEvents)
        }
        val _columnsEvidence: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEvidence.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("workItemId", TableInfo.Column("workItemId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("eventId", TableInfo.Column("eventId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("kind", TableInfo.Column("kind", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("uri", TableInfo.Column("uri", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("sha256", TableInfo.Column("sha256", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("sizeBytes", TableInfo.Column("sizeBytes", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("metaJson", TableInfo.Column("metaJson", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEvidence.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEvidence: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEvidence: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesEvidence.add(TableInfo.Index("index_evidence_eventId", false, listOf("eventId"),
            listOf("ASC")))
        _indicesEvidence.add(TableInfo.Index("index_evidence_workItemId", false,
            listOf("workItemId"), listOf("ASC")))
        val _infoEvidence: TableInfo = TableInfo("evidence", _columnsEvidence, _foreignKeysEvidence,
            _indicesEvidence)
        val _existingEvidence: TableInfo = read(connection, "evidence")
        if (!_infoEvidence.equals(_existingEvidence)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |evidence(com.example.arweld.core.data.db.entity.EvidenceEntity).
              | Expected:
              |""".trimMargin() + _infoEvidence + """
              |
              | Found:
              |""".trimMargin() + _existingEvidence)
        }
        val _columnsUsers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUsers.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("name", TableInfo.Column("name", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("lastSeenAt", TableInfo.Column("lastSeenAt", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUsers.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUsers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUsers: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUsers: TableInfo = TableInfo("users", _columnsUsers, _foreignKeysUsers,
            _indicesUsers)
        val _existingUsers: TableInfo = read(connection, "users")
        if (!_infoUsers.equals(_existingUsers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |users(com.example.arweld.core.data.db.entity.UserEntity).
              | Expected:
              |""".trimMargin() + _infoUsers + """
              |
              | Found:
              |""".trimMargin() + _existingUsers)
        }
        val _columnsSyncQueue: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSyncQueue.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("payloadJson", TableInfo.Column("payloadJson", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("retryCount", TableInfo.Column("retryCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSyncQueue: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSyncQueue: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSyncQueue.add(TableInfo.Index("index_sync_queue_status", false, listOf("status"),
            listOf("ASC")))
        val _infoSyncQueue: TableInfo = TableInfo("sync_queue", _columnsSyncQueue,
            _foreignKeysSyncQueue, _indicesSyncQueue)
        val _existingSyncQueue: TableInfo = read(connection, "sync_queue")
        if (!_infoSyncQueue.equals(_existingSyncQueue)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sync_queue(com.example.arweld.core.data.db.entity.SyncQueueEntity).
              | Expected:
              |""".trimMargin() + _infoSyncQueue + """
              |
              | Found:
              |""".trimMargin() + _existingSyncQueue)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "work_items", "events",
        "evidence", "users", "sync_queue")
  }

  public override fun clearAllTables() {
    super.performClear(false, "work_items", "events", "evidence", "users", "sync_queue")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(WorkItemDao::class, WorkItemDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(EventDao::class, EventDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(EvidenceDao::class, EvidenceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserDao::class, UserDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SyncQueueDao::class, SyncQueueDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun workItemDao(): WorkItemDao = _workItemDao.value

  public override fun eventDao(): EventDao = _eventDao.value

  public override fun evidenceDao(): EvidenceDao = _evidenceDao.value

  public override fun userDao(): UserDao = _userDao.value

  public override fun syncQueueDao(): SyncQueueDao = _syncQueueDao.value
}
