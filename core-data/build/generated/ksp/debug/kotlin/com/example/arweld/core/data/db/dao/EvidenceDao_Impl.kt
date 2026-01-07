package com.example.arweld.core.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.arweld.core.`data`.db.entity.EvidenceEntity
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class EvidenceDao_Impl(
  __db: RoomDatabase,
) : EvidenceDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfEvidenceEntity: EntityInsertAdapter<EvidenceEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfEvidenceEntity = object : EntityInsertAdapter<EvidenceEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `evidence` (`id`,`workItemId`,`eventId`,`kind`,`uri`,`sha256`,`sizeBytes`,`metaJson`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: EvidenceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.workItemId)
        statement.bindText(3, entity.eventId)
        statement.bindText(4, entity.kind)
        statement.bindText(5, entity.uri)
        statement.bindText(6, entity.sha256)
        statement.bindLong(7, entity.sizeBytes)
        val _tmpMetaJson: String? = entity.metaJson
        if (_tmpMetaJson == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpMetaJson)
        }
        statement.bindLong(9, entity.createdAt)
      }
    }
  }

  public override suspend fun insert(evidence: EvidenceEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfEvidenceEntity.insert(_connection, evidence)
  }

  public override suspend fun insertAll(evidence: List<EvidenceEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfEvidenceEntity.insert(_connection, evidence)
  }

  public override suspend fun getById(id: String): EvidenceEntity? {
    val _sql: String = "SELECT * FROM evidence WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfEventId: Int = getColumnIndexOrThrow(_stmt, "eventId")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfUri: Int = getColumnIndexOrThrow(_stmt, "uri")
        val _columnIndexOfSha256: Int = getColumnIndexOrThrow(_stmt, "sha256")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfMetaJson: Int = getColumnIndexOrThrow(_stmt, "metaJson")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: EvidenceEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpEventId: String
          _tmpEventId = _stmt.getText(_columnIndexOfEventId)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpUri: String
          _tmpUri = _stmt.getText(_columnIndexOfUri)
          val _tmpSha256: String
          _tmpSha256 = _stmt.getText(_columnIndexOfSha256)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpMetaJson: String?
          if (_stmt.isNull(_columnIndexOfMetaJson)) {
            _tmpMetaJson = null
          } else {
            _tmpMetaJson = _stmt.getText(_columnIndexOfMetaJson)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result =
              EvidenceEntity(_tmpId,_tmpWorkItemId,_tmpEventId,_tmpKind,_tmpUri,_tmpSha256,_tmpSizeBytes,_tmpMetaJson,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun listByEvent(eventId: String): List<EvidenceEntity> {
    val _sql: String = "SELECT * FROM evidence WHERE eventId = ? ORDER BY createdAt ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, eventId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfEventId: Int = getColumnIndexOrThrow(_stmt, "eventId")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfUri: Int = getColumnIndexOrThrow(_stmt, "uri")
        val _columnIndexOfSha256: Int = getColumnIndexOrThrow(_stmt, "sha256")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfMetaJson: Int = getColumnIndexOrThrow(_stmt, "metaJson")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<EvidenceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: EvidenceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpEventId: String
          _tmpEventId = _stmt.getText(_columnIndexOfEventId)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpUri: String
          _tmpUri = _stmt.getText(_columnIndexOfUri)
          val _tmpSha256: String
          _tmpSha256 = _stmt.getText(_columnIndexOfSha256)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpMetaJson: String?
          if (_stmt.isNull(_columnIndexOfMetaJson)) {
            _tmpMetaJson = null
          } else {
            _tmpMetaJson = _stmt.getText(_columnIndexOfMetaJson)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              EvidenceEntity(_tmpId,_tmpWorkItemId,_tmpEventId,_tmpKind,_tmpUri,_tmpSha256,_tmpSizeBytes,_tmpMetaJson,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun listByEvents(eventIds: List<String>): List<EvidenceEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM evidence WHERE eventId IN (")
    val _inputSize: Int = eventIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in eventIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfEventId: Int = getColumnIndexOrThrow(_stmt, "eventId")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfUri: Int = getColumnIndexOrThrow(_stmt, "uri")
        val _columnIndexOfSha256: Int = getColumnIndexOrThrow(_stmt, "sha256")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfMetaJson: Int = getColumnIndexOrThrow(_stmt, "metaJson")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<EvidenceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: EvidenceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpEventId: String
          _tmpEventId = _stmt.getText(_columnIndexOfEventId)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpUri: String
          _tmpUri = _stmt.getText(_columnIndexOfUri)
          val _tmpSha256: String
          _tmpSha256 = _stmt.getText(_columnIndexOfSha256)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpMetaJson: String?
          if (_stmt.isNull(_columnIndexOfMetaJson)) {
            _tmpMetaJson = null
          } else {
            _tmpMetaJson = _stmt.getText(_columnIndexOfMetaJson)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item_1 =
              EvidenceEntity(_tmpId,_tmpWorkItemId,_tmpEventId,_tmpKind,_tmpUri,_tmpSha256,_tmpSizeBytes,_tmpMetaJson,_tmpCreatedAt)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun listByWorkItem(workItemId: String): List<EvidenceEntity> {
    val _sql: String = "SELECT * FROM evidence WHERE workItemId = ? ORDER BY createdAt ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, workItemId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfWorkItemId: Int = getColumnIndexOrThrow(_stmt, "workItemId")
        val _columnIndexOfEventId: Int = getColumnIndexOrThrow(_stmt, "eventId")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfUri: Int = getColumnIndexOrThrow(_stmt, "uri")
        val _columnIndexOfSha256: Int = getColumnIndexOrThrow(_stmt, "sha256")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfMetaJson: Int = getColumnIndexOrThrow(_stmt, "metaJson")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<EvidenceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: EvidenceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpWorkItemId: String
          _tmpWorkItemId = _stmt.getText(_columnIndexOfWorkItemId)
          val _tmpEventId: String
          _tmpEventId = _stmt.getText(_columnIndexOfEventId)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpUri: String
          _tmpUri = _stmt.getText(_columnIndexOfUri)
          val _tmpSha256: String
          _tmpSha256 = _stmt.getText(_columnIndexOfSha256)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpMetaJson: String?
          if (_stmt.isNull(_columnIndexOfMetaJson)) {
            _tmpMetaJson = null
          } else {
            _tmpMetaJson = _stmt.getText(_columnIndexOfMetaJson)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              EvidenceEntity(_tmpId,_tmpWorkItemId,_tmpEventId,_tmpKind,_tmpUri,_tmpSha256,_tmpSizeBytes,_tmpMetaJson,_tmpCreatedAt)
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
