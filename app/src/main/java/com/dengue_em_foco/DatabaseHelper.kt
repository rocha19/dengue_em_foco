package com.dengue_em_foco

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import com.dengue_em_foco.entities.Municipio
import java.util.UUID
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dengue_foco.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CITY = "city"
        const val COLUMN_UF = "uf"
        const val TABLE_DENGUE_NOTICE = "dengue_notice"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_CASES = "cases"
        const val COLUMN_DATE_UPDATE = "date_update"
        const val COLUMN_NAME_DISTRICT = "name_district"
        const val COLUMN_GEOCODE_DISTRICT = "geocode_district"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS ("
                + "$COLUMN_ID TEXT PRIMARY KEY,"
                + "$COLUMN_NAME TEXT UNIQUE,"
                + "$COLUMN_CITY TEXT,"
                + "$COLUMN_UF TEXT" + ")")

        val createDengueNoticeTable = ("CREATE TABLE $TABLE_DENGUE_NOTICE ("
                + "$COLUMN_ID TEXT PRIMARY KEY,"
                + "$COLUMN_USER_ID TEXT,"
                + "$COLUMN_CASES INTEGER,"
                + "$COLUMN_DATE_UPDATE TEXT,"
                + "$COLUMN_NAME_DISTRICT TEXT,"
                + "$COLUMN_GEOCODE_DISTRICT TEXT,"
                + "FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)" + ")")

        db.execSQL(createUsersTable)
        db.execSQL(createDengueNoticeTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DENGUE_NOTICE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun addUser(name: String, city: String, uf: String): String? {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        val id = UUID.randomUUID().toString()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_NAME, name)
        contentValues.put(COLUMN_CITY, city)
        contentValues.put(COLUMN_UF, uf)

        return try{
            val result = db.insert(TABLE_USERS, null, contentValues)
            if(result != -1L) id else null
        } finally {
            db.close()
        }
    }

    fun userExists(name: String): Boolean {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS, arrayOf(COLUMN_ID),
            "$COLUMN_NAME = ?", arrayOf(name),
            null, null, null
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun userWithCityNameExists(name: String, nameCity: String): Boolean {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_DENGUE_NOTICE, arrayOf(COLUMN_ID),
            "$COLUMN_NAME = ? AND $COLUMN_CITY = ?", arrayOf(name, nameCity),
            null, null, null
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun addDengueNotice(userId: String, cases: Int, municipio: Municipio): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, UUID.randomUUID().toString())
        contentValues.put(COLUMN_USER_ID, userId)
        contentValues.put(COLUMN_CASES, cases)

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)
        contentValues.put(COLUMN_DATE_UPDATE, formattedDateTime)

        contentValues.put(COLUMN_NAME_DISTRICT, municipio.nome)
        contentValues.put(COLUMN_GEOCODE_DISTRICT, municipio.id)

        val result = db.insert(TABLE_DENGUE_NOTICE, null, contentValues)
        db.close()
        return result != -1L
    }

    fun getDengueNoticesByUser(userId: String): Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_DENGUE_NOTICE, null,
            "$COLUMN_USER_ID = ?", arrayOf(userId),
            null, null, null
        )
    }
}