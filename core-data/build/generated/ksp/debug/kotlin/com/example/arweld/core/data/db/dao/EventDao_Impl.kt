package com.example.arweld.core.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.arweld.core.`data`.db.entity.EventEntity
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
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class EventDao_Impl(
  __db: RoomDatabase,
) : EventDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfEventEntity: EntityInsertAdapter<EventEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfEventEntity = object : EntityInsertAdapter<EventEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `events` (`id`,`workItemId`,`type`,`timestamp`,`actorId`,`actorRole`,`deviceId`,`payloadJson`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: EventEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.workItemId)
        statement.bindText(3, entity.type)
        statement.bindLong(4, entity.timestamp)
        statement.bindText(5, entity.actorId)
        statement.bindText(6, entity.actorRole)
        statement.bindText(7, entity.deviceId)
        val _tmpPayloadJson: String? = entity.payloadJson
        if (_tmpPayloadJson == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpPayloadJson)
        }
      }
    }
  }

  public override suspend fun insert(event: EventEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfEventEntity.insert(_connection, event)
  }

  public override suspend fun insertAll(events: List<EventEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfEventEntity.insert(_connection, events)
  }

  public override suspend fun getByWorkItemId(workItemId: String): List<EventEntity> {
    val _sql: String = "SELECT * FROM events WHERE workItemId = ? ORDER BY timestamp ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, workItemId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfActorId: Int = getColumnIndexOrThrow(_stmt, "actorId")
        val _columnIndexOfActorRole: Int = getColumnIndexOrThrow(_stmt, "actorRole")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfPayloadJson: Int = getColumnIndexOrThrow(_stmt, "payloadJson")
        val _result: MutableList<EventEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: EventEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpActorId: String
          _tmpActorId = _stmt.getText(_columnIndexOfActorId)
          val _tmpActorRole: String
          _tmpActorRole = _stmt.getText(_columnIndexOfActorRole)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpPayloadJson: String?
          if (_stmt.isNull(_columnIndexOfPayloadJson)) {
            _tmpPayloadJson = null
          } else {
            _tmpPayloadJson = _stmt.getText(_columnIndexOfPayloadJson)
          }
          _item =
              EventEntity(_tmpId,_tmpWorkItemId,_tmpType,_tmpTimestamp,_tmpActorId,_tmpActorRole,_tmpDeviceId,_tmpPayloadJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByWorkItemIds(workItemIds: List<String>): List<EventEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM events WHERE workItemId IN (")
    val _inputSize: Int = workItemIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") ORDER BY timestamp ASC")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in workItemIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfActorId: Int = getColumnIndexOrThrow(_stmt, "actorId")
        val _columnIndexOfActorRole: Int = getColumnIndexOrThrow(_stmt, "actorRole")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfPayloadJson: Int = getColumnIndexOrThrow(_stmt, "payloadJson")
        val _result: MutableList<EventEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: EventEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpActorId: String
          _tmpActorId = _stmt.getText(_columnIndexOfActorId)
          val _tmpActorRole: String
          _tmpActorRole = _stmt.getText(_columnIndexOfActorRole)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpPayloadJson: String?
          if (_stmt.isNull(_columnIndexOfPayloadJson)) {
            _tmpPayloadJson = null
          } else {
            _tmpPayloadJson = _stmt.getText(_columnIndexOfPayloadJson)
          }
          _item_1 =
              EventEntity(_tmpId,_tmpWorkItemId,_tmpType,_tmpTimestamp,_tmpActorId,_tmpActorRole,_tmpDeviceId,_tmpPayloadJson)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastEventByUser(userId: String): EventEntity? {
    val _sql: String = "SELECT * FROM events WHERE actorId = ? ORDER BY timestamp DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfActorId: Int = getColumnIndexOrThrow(_stmt, "actorId")
        val _columnIndexOfActorRole: Int = getColumnIndexOrThrow(_stmt, "actorRole")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfPayloadJson: Int = getColumnIndexOrThrow(_stmt, "payloadJson")
        val _result: EventEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpActorId: String
          _tmpActorId = _stmt.getText(_columnIndexOfActorId)
          val _tmpActorRole: String
          _tmpActorRole = _stmt.getText(_columnIndexOfActorRole)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpPayloadJson: String?
          if (_stmt.isNull(_columnIndexOfPayloadJson)) {
            _tmpPayloadJson = null
          } else {
            _tmpPayloadJson = _stmt.getText(_columnIndexOfPayloadJson)
          }
          _result =
              EventEntity(_tmpId,_tmpWorkItemId,_tmpType,_tmpTimestamp,_tmpActorId,_tmpActorRole,_tmpDeviceId,_tmpPayloadJson)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastEventsByUser(userId: String): List<EventEntity> {
    val _sql: String = "SELECT * FROM events WHERE actorId = ? ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfActorId: Int = getColumnIndexOrThrow(_stmt, "actorId")
        val _columnIndexOfActorRole: Int = getColumnIndexOrThrow(_stmt, "actorRole")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfPayloadJson: Int = getColumnIndexOrThrow(_stmt, "payloadJson")
        val _result: MutableList<EventEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: EventEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpActorId: String
          _tmpActorId = _stmt.getText(_columnIndexOfActorId)
          val _tmpActorRole: String
          _tmpActorRole = _stmt.getText(_columnIndexOfActorRole)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpPayloadJson: String?
          if (_stmt.isNull(_columnIndexOfPayloadJson)) {
            _tmpPayloadJson = null
          } else {
            _tmpPayloadJson = _stmt.getText(_columnIndexOfPayloadJson)
          }
          _item =
              EventEntity(_tmpId,_tmpWorkItemId,_tmpType,_tmpTimestamp,_tmpActorId,_tmpActorRole,_tmpDeviceId,_tmpPayloadJson)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeByWorkItem(workItemId: String): Flow<List<EventEntity>> {
    val _sql: String = "SELECT * FROM events WHERE workItemId = ? ORDER BY timestamp ASC"
    return createFlow(__db, false, arrayOf("events")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, workItemId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfActorId: Int = getColumnIndexOrThrow(_stmt, "actorId")
        val _columnIndexOfActorRole: Int = getColumnIndexOrThrow(_stmt, "actorRole")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfPayloadJson: Int = getColumnIndexOrThrow(_stmt, "payloadJson")
        val _result: MutableList<EventEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: EventEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpActorId: String
          _tmpActorId = _stmt.getText(_columnIndexOfActorId)
          val _tmpActorRole: String
          _tmpActorRole = _stmt.getText(_columnIndexOfActorRole)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpPayloadJson: String?
          if (_stmt.isNull(_columnIndexOfPayloadJson)) {
            _tmpPayloadJson = null
          } else {
            _tmpPayloadJson = _stmt.getText(_columnIndexOfPayloadJson)
          }
          _item =
              EventEntity(_tmpId,_tmpWorkItemId,_tmpType,_tmpTimestamp,_tmpActorId,_tmpActorRole,_tmpDeviceId,_tmpPayloadJson)
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
