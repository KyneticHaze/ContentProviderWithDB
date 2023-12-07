package com.example.contentprovider

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val uriContent = "content://com.example.contentprovider.studentprovider/studentscontent"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        insertStudent("Furkan Harmancı", 26)

        queryStudents()
    }

    private fun insertStudent(name : String, grade : Int) {
        val uri = Uri.parse(uriContent)
        val values = ContentValues().apply {
            put("name", name)
            put("grade", grade)
        }

        contentResolver.insert(uri, values)
    }

    @SuppressLint("Range")
    private fun queryStudents() {
        val uri = Uri.parse(uriContent)
        val projection = arrayOf("_id", "name", "grade")
        val cursor : Cursor? = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex("_id"))
                val name = it.getString(it.getColumnIndex("name"))
                val grade = it.getInt(it.getColumnIndex("grade"))

                println("Student ID: $id, Name: $name, Grade: $grade")
            }
        }
    }
}