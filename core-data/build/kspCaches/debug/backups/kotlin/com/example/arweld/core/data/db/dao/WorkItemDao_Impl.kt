package com.example.arweld.core.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.arweld.core.`data`.db.entity.WorkItemEntity
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WorkItemDao_Impl(
  __db: RoomDatabase,
) : WorkItemDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkItemEntity: EntityInsertAdapter<WorkItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkItemEntity = object : EntityInsertAdapter<WorkItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `work_items` (`id`,`projectId`,`zoneId`,`type`,`code`,`description`,`nodeId`,`createdAt`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkItemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.projectId)
        val _tmpZoneId: String? = entity.zoneId
        if (_tmpZoneId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpZoneId)
        }
        statement.bindText(4, entity.type)
        val _tmpCode: String? = entity.code
        if (_tmpCode == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpCode)
        }
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpDescription)
        }
        val _tmpNodeId: String? = entity.nodeId
        if (_tmpNodeId == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNodeId)
        }
        val _tmpCreatedAt: Long? = entity.createdAt
        if (_tmpCreatedAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCreatedAt)
        }
      }
    }
  }

  public override suspend fun insert(workItem: WorkItemEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfWorkItemEntity.insert(_connection, workItem)
  }

  public override suspend fun insertAll(items: List<WorkItemEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfWorkItemEntity.insert(_connection, items)
  }

  public override suspend fun getByCode(code: String): WorkItemEntity? {
    val _sql: String = "SELECT * FROM work_items WHERE code = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, code)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfZoneId: Int = getColumnIndexOrThrow(_stmt, "zoneId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfNodeId: Int = getColumnIndexOrThrow(_stmt, "nodeId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: WorkItemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpProjectId: String
          _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          val _tmpZoneId: String?
          if (_stmt.isNull(_columnIndexOfZoneId)) {
            _tmpZoneId = null
          } else {
            _tmpZoneId = _stmt.getText(_columnIndexOfZoneId)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpNodeId: String?
          if (_stmt.isNull(_columnIndexOfNodeId)) {
            _tmpNodeId = null
          } else {
            _tmpNodeId = _stmt.getText(_columnIndexOfNodeId)
          }
          val _tmpCreatedAt: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmpCreatedAt = null
          } else {
            _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          _result =
              WorkItemEntity(_tmpId,_tmpProjectId,_tmpZoneId,_tmpType,_tmpCode,_tmpDescription,_tmpNodeId,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countAll(): Int {
    val _sql: String = "SELECT COUNT(*) FROM work_items"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): WorkItemEntity? {
    val _sql: String = "SELECT * FROM work_items WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfZoneId: Int = getColumnIndexOrThrow(_stmt, "zoneId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfNodeId: Int = getColumnIndexOrThrow(_stmt, "nodeId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: WorkItemEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpProjectId: String
          _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          val _tmpZoneId: String?
          if (_stmt.isNull(_columnIndexOfZoneId)) {
            _tmpZoneId = null
          } else {
            _tmpZoneId = _stmt.getText(_columnIndexOfZoneId)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpNodeId: String?
          if (_stmt.isNull(_columnIndexOfNodeId)) {
            _tmpNodeId = null
          } else {
            _tmpNodeId = _stmt.getText(_columnIndexOfNodeId)
          }
          val _tmpCreatedAt: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmpCreatedAt = null
          } else {
            _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          _result =
              WorkItemEntity(_tmpId,_tmpProjectId,_tmpZoneId,_tmpType,_tmpCode,_tmpDescription,_tmpNodeId,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAll(): Flow<List<WorkItemEntity>> {
    val _sql: String = "SELECT * FROM work_items ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("work_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfZoneId: Int = getColumnIndexOrThrow(_stmt, "zoneId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfNodeId: Int = getColumnIndexOrThrow(_stmt, "nodeId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<WorkItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpProjectId: String
          _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          val _tmpZoneId: String?
          if (_stmt.isNull(_columnIndexOfZoneId)) {
            _tmpZoneId = null
          } else {
            _tmpZoneId = _stmt.getText(_columnIndexOfZoneId)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCode: String?
          if (_stmt.isNull(_columnIndexOfCode)) {
            _tmpCode = null
          } else {
            _tmpCode = _stmt.getText(_columnIndexOfCode)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpNodeId: String?
          if (_stmt.isNull(_columnIndexOfNodeId)) {
            _tmpNodeId = null
          } else {
            _tmpNodeId = _stmt.getText(_columnIndexOfNodeId)
          }
          val _tmpCreatedAt: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmpCreatedAt = null
          } else {
            _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          _item =
              WorkItemEntity(_tmpId,_tmpProjectId,_tmpZoneId,_tmpType,_tmpCode,_tmpDescription,_tmpNodeId,_tmpCreatedAt)
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
