package com.example.files.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.files.Statics.favorites
import com.example.files.models.FavoriteItem
import com.example.files.models.JFile

class DBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    companion object {
        const val DATABASE_NAME = "favorites.db"
        const val FAV_TABLE_NAME = "favorites"
        const val FAV_COLUMN_ID = "id"
        const val FAV_COLUMN_PATH = "path"

        @JvmStatic
        fun idFromPath(path: String): Int {
            for (fav in favorites!!.allPaths) {
                if (fav.path == path) return fav.id
            }
            Log.d("##### idFromPath #####", "not exist")
            return -1
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table $FAV_TABLE_NAME ($FAV_COLUMN_ID integer primary key, $FAV_COLUMN_PATH text)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $FAV_TABLE_NAME")
        onCreate(db)
    }

    fun insertPath(path: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(FAV_COLUMN_PATH, path)
        db.insert(FAV_TABLE_NAME, null, contentValues)
    }

    fun update(id: Int, path: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(FAV_COLUMN_PATH, path)
        db.update(FAV_TABLE_NAME, contentValues, "id = ? ", arrayOf(id.toString()))
        return true
    }

    fun numberOfRows(): Int {
        val db = this.readableDatabase
        return DatabaseUtils.queryNumEntries(db, FAV_TABLE_NAME).toInt()
    }

    fun deletePath(id: Int) {
        val db = this.writableDatabase
        db.delete(FAV_TABLE_NAME, "id = ? ", arrayOf(id.toString()))
    }

    val allPaths: ArrayList<FavoriteItem>
        @SuppressLint("Range", "Recycle")
        get() {
            val favItems = ArrayList<FavoriteItem>()
            val db = this.readableDatabase
            val res = db.rawQuery("select * from $FAV_TABLE_NAME", null)
            res.moveToFirst()

            while (!res.isAfterLast) {
                favItems.add(
                    FavoriteItem(
                        res.getString(res.getColumnIndex(FAV_COLUMN_PATH)),
                        res.getInt(res.getColumnIndex(FAV_COLUMN_ID))
                    )
                )
                res.moveToNext()
            }
            res.close()
            return favItems
        }

    fun addToFavorites(jFile: JFile) {
        if (numberOfRows() > 0) {
            var notExist = false
            for (fav in allPaths) {
                if (jFile.path == fav.path) {
                    notExist = false
                    break
                } else {
                    notExist = true
                }
            }
            if (notExist) insertPath(jFile.path)
            Log.d("##### addToFavorites #####", "notExist = $notExist")
        } else {
            insertPath(jFile.path)
        }
    }

    fun exist(jFile: JFile): Boolean {
        if (numberOfRows() > 0) {
            for (fav in allPaths) {
                if (jFile.path == fav.path) return true
            }
        }
        return false
    }

    fun existPath(path: String): Boolean {
        if (numberOfRows() > 0) {
            for (fav in allPaths) {
                if (path == fav.path) return true
            }
        }
        return false
    }

    fun allExist(jFiles: ArrayList<JFile>): Boolean {
        for (jFile in jFiles) {
            if (!exist(jFile)) return false
        }
        return true
    }

    @SuppressLint("Range", "Recycle")
    fun IDFromPath(path: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $FAV_TABLE_NAME WHERE path LIKE '%$path%'", null)
        cursor.moveToFirst()
        if (cursor.count == 0) {
            cursor.close()
            return -1
        }
        val id = cursor.getInt(cursor.getColumnIndex(FAV_COLUMN_ID))
        cursor.close()
        return id
    }

    fun getData(id: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("select * from $FAV_TABLE_NAME where id=$id", null)
    }
}
