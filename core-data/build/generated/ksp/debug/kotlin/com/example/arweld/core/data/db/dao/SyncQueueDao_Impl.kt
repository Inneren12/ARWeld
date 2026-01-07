package com.example.arweld.core.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.arweld.core.`data`.db.entity.SyncQueueEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SyncQueueDao_Impl(
  __db: RoomDatabase,
) : SyncQueueDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSyncQueueEntity: EntityInsertAdapter<SyncQueueEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSyncQueueEntity = object : EntityInsertAdapter<SyncQueueEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `sync_queue` (`id`,`payloadJson`,`createdAt`,`status`,`retryCount`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SyncQueueEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.payloadJson)
        statement.bindLong(3, entity.createdAt)
        statement.bindText(4, entity.status)
        statement.bindLong(5, entity.retryCount.toLong())
      }
    }
  }

  public override suspend fun insert(item: SyncQueueEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfSyncQueueEntity.insert(_connection, item)
  }

  public override suspend fun insertAll(items: List<SyncQueueEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSyncQueueEntity.insert(_connection, items)
  }

  public override suspend fun getPending(pendingStatus: String, limit: Int): List<SyncQueueEntity> {
    val _sql: String = "SELECT * FROM sync_queue WHERE status = ? ORDER BY createdAt ASC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, pendingStatus)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPayloadJson: Int = getColumnIndexOrThrow(_stmt, "payloadJson")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfRetryCount: Int = getColumnIndexOrThrow(_stmt, "retryCount")
        val _result: MutableList<SyncQueueEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SyncQueueEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPayloadJson: String
          _tmpPayloadJson = _stmt.getText(_columnIndexOfPayloadJson)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          _item = SyncQueueEntity(_tmpId,_tmpPayloadJson,_tmpCreatedAt,_tmpStatus,_tmpRetryCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
