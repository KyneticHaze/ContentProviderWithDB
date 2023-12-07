package com.example.contentprovider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.provider.BaseColumns


class StudentProvider : ContentProvider() {

   companion object {
       private const val DATABASE_NAME = "students.db"
       private const val DATABASE_VERSION = 1
       private const val TABLE_NAME = "students"

       private const val STUDENTS = 1
       private const val STUDENT_ID = 2
       private const val AUTHORITY = "com.example.contentprovider.studentprovider"
       private const val CONTENT_PATH = "studentscontent"
       private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$CONTENT_PATH")
   }

    private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(AUTHORITY, CONTENT_PATH, STUDENTS)
        uriMatcher.addURI(AUTHORITY,"$CONTENT_PATH/#", STUDENT_ID)
    }

    private inner class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

            private val CREATE_TABLE =
                "CREATE TABLE $TABLE_NAME (${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, grade INTEGER)"

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    private lateinit var database : SQLiteDatabase


    override fun onCreate(): Boolean {
        val context = context

        val dbHelper = context?.let {
            DatabaseHelper(it)
        }
        database = dbHelper!!.writableDatabase
        return database != null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = TABLE_NAME

        when (uriMatcher.match(uri)) {
            STUDENT_ID -> queryBuilder.appendWhere("${BaseColumns._ID}=${uri.pathSegments[1]}")
        }

        val cursor = queryBuilder.query(
            database, projection, selection,
            selectionArgs, null, null, sortOrder
        )
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            STUDENTS -> "vnd.android.cursor.dir/$CONTENT_PATH"
            STUDENT_ID -> "vnd.android.cursor.item/$CONTENT_PATH"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = database.insert(TABLE_NAME, "", values)
        if (rowID > 0) {
            val studentUri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(studentUri, null)
            return studentUri
        }
        return throw SQLException("Failed to add a record into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val count = database.delete(TABLE_NAME, selection, selectionArgs)
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val count = database.update(TABLE_NAME, values, selection, selectionArgs)
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }
}