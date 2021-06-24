package net.ienlab.study.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteOpenHelper
import net.ienlab.study.data.TimeData
import java.util.*
import kotlin.collections.ArrayList

class DBHelper(context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    //DB 처음 만들때 호출. - 테이블 생성 등의 초기 처리.
    override fun onCreate(db: SQLiteDatabase) {
        val sb = StringBuffer()
        sb.append(" CREATE TABLE $_TABLENAME0 ( ")
        sb.append(" $ID INTEGER PRIMARY KEY AUTOINCREMENT, ")
        sb.append(" $TYPE INTEGER, ")
        sb.append(" $DATETIME LONG, ")
        sb.append(" $STUDYTIME LONG )")

        db.execSQL(sb.toString())
    }

    //DB 업그레이드 필요 시 호출. (version값에 따라 반응)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $_TABLENAME0")
        onCreate(db)
    }

    fun addItem(item: TimeData) {
        val db = writableDatabase

        val sb = StringBuffer()
        sb.append(" INSERT INTO $_TABLENAME0 ( ")
        sb.append(" $TYPE, $DATETIME, $STUDYTIME ) ")
        sb.append(" VALUES ( ?, ?, ? )")

        db.execSQL(sb.toString(),
                arrayOf(
                    item.type,
                    item.dateTime,
                    item.studyTime
                )
        )
    }

    fun updateItem(item: TimeData) {
        val db = writableDatabase
        val value = ContentValues()

        value.put(ID, item.id)
        value.put(TYPE, item.type)
        value.put(DATETIME, item.dateTime)
        value.put(STUDYTIME, item.studyTime)

        try {
            db.update(_TABLENAME0, value, "ID=${item.id}", null)
        } catch (e: SQLiteDatabaseLockedException) {}
    }

    fun getDataAtDate(date: Long): List<TimeData> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val sb = StringBuffer()
        sb.append(" SELECT $ID, $TYPE, $DATETIME, $STUDYTIME FROM $_TABLENAME0 WHERE $DATETIME >= ${calendar.timeInMillis} AND $DATETIME < ${calendar.timeInMillis + 24 * 60 * 60 * 1000} ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<TimeData>()

        while (cursor.moveToNext()) {
            with (cursor) {
                arr.add(TimeData(getInt(0), getInt(1), getLong(2), getLong(3), TimeData.VIEWTYPE_NOTI))
            }
        }

        cursor.close()
        return arr
    }

    fun getDataById(id: Long): TimeData {
        val sb = StringBuffer()
        sb.append(" SELECT $ID, $TYPE, $DATETIME, $STUDYTIME FROM $_TABLENAME0 FROM $_TABLENAME0 WHERE $ID=$id ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        var data = TimeData()
        while (cursor.moveToNext()) {
            with (cursor) {
                data = TimeData(getInt(0), getInt(1), getLong(2), getLong(3), TimeData.VIEWTYPE_NOTI)
            }
        }

        cursor.close()
        return data
    }

    fun getAllData(): List<TimeData> {
        val sb = StringBuffer()
        sb.append(" SELECT $ID, $TYPE, $DATETIME, $STUDYTIME FROM $_TABLENAME0 ")

        val db = readableDatabase
        val cursor = db.rawQuery(sb.toString(), null)

        val arr = ArrayList<TimeData>()

        while (cursor.moveToNext()) {
            with (cursor) {
                arr.add(TimeData(getInt(0), getInt(1), getLong(2), getLong(3), TimeData.VIEWTYPE_NOTI))
            }
        }

        cursor.close()
        return arr
    }

    fun deleteData(id: Int) {
        val db = writableDatabase
        db.execSQL(" DELETE FROM $_TABLENAME0 WHERE $ID = $id")
    }

    fun checkIsDataAlreadyInDBorNot(dbfield: String, fieldValue: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $_TABLENAME0 WHERE $dbfield = $fieldValue"
        val cursor = db.rawQuery(query, null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    companion object {
        const val dbName = "StudyTimeData.db"
        const val dbVersion = 1

        const val _TABLENAME0 = "STUDY_TIME_DATA"

        const val ID = "ID"
        const val TYPE = "TYPE"
        const val DATETIME = "DATETIME"
        const val STUDYTIME = "STUDYTIME"
    }
}

