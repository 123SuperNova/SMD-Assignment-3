package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        var sql = "CREATE TABLE CONTACT (id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL)"
        db.execSQL(sql)
        // Create the CONTACTPHONE table with a foreign key constraint
        var sql2 = "CREATE TABLE CONTACTPHONE (id TEXT PRIMARY KEY, " +
                "phonenum TEXT NOT NULL, " +
                "contact_id TEXT NOT NULL, " +  // Foreign key column
                "FOREIGN KEY (contact_id) REFERENCES CONTACT(id))"
        db.execSQL(sql2)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS CONTACT")
        db.execSQL("DROP TABLE IF EXISTS CONTACTPHONE")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun addContactData(name: String?, id: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", name)
            put("id", id)
        }
        val result = db.insert("CONTACT", null, contentValues)
        db.close()
        return result != -1L
    }

    fun addContactNumberData(phoneNum: String?, contactid: String?, id: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("id", id)
            put("phonenum", phoneNum)
            put("contact_id", contactid)
        }
        val result = db.insert("CONTACTPHONE", null, contentValues)
        db.close()
        return result != -1L
    }

    fun updateContactName(id: String, newName: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", newName)
        }
        val result = db.update("CONTACT", contentValues, "id = ?", arrayOf(id))
        db.close()
        return result > 0
    }

    fun updateContactNumber(id: String, newPhone: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("phonenum", newPhone)
        }
        val result = db.update("CONTACTPHONE", contentValues, "id = ?", arrayOf(id))
        db.close()
        return result > 0
    }

    fun deleteContactData(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete("CONTACT", "id = ?", arrayOf(id))
        db.close()
        return result > 0
    }

    fun deleteContactNumberData(contact_id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete("CONTACTPHONE", "contact_id = ?", arrayOf(contact_id))
        db.close()
        return result > 0
    }

    val allUsers: ArrayList<Contact>
        get() {
            val userList = ArrayList<Contact>()
            val selectQuery = "SELECT * FROM CONTACT"
            val db = this.readableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val user = Contact(id = cursor.getLong(0), name = cursor.getString(1))
                    userList.add(user)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return userList
        }
    val allPhoneNumbers: ArrayList<ContactPhoneNumber>
        get() {
            val phoneNumList = ArrayList<ContactPhoneNumber>()

            // Use a placeholder for the reqid parameter
            val selectQuery = "SELECT * FROM CONTACTPHONE"

            // Use the rawQuery method with the selectionArgs parameter to provide the reqid value
            val db = this.readableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            if (cursor.moveToFirst()) {
                do {
                    val user = ContactPhoneNumber(id=cursor.getString(0),contact_id = cursor.getLong(2), phoneNumber = cursor.getString(1))
                    phoneNumList.add(user)
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
            return phoneNumList
        }

    fun allPhoneNumbersOf(reqid: String?): ArrayList<ContactPhoneNumber> {
        val phoneNumList: ArrayList<ContactPhoneNumber> = ArrayList<ContactPhoneNumber>()

        // Use a placeholder for the reqid parameter
        val selectQuery = "SELECT * FROM CONTACTPHONE WHERE contact_id = ?"

        // Use the rawQuery method with the selectionArgs parameter to provide the reqid value
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(reqid))

        if (cursor.moveToFirst()) {
            do {
                val user = ContactPhoneNumber(id=cursor.getString(0),contact_id = cursor.getLong(2), phoneNumber = cursor.getString(1))
                phoneNumList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return phoneNumList
    }

    fun getContactFromId(reqid: String?): Contact {
        var contact = Contact(0, "")

        // Use a placeholder for the reqid parameter
        val selectQuery = "SELECT * FROM CONTACT WHERE id = ?"

        // Use the rawQuery method with the selectionArgs parameter to provide the reqid value
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(reqid))

        if (cursor.moveToFirst()) {
            do {
                val user = Contact(id = cursor.getLong(0), name = cursor.getString(1))
                contact = user
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return contact
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "UserData.db"

        @Volatile
        private var instance: DBHelper? = null

        fun getInstance(context: Context): DBHelper {
            return instance ?: synchronized(this) {
                instance ?: DBHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}