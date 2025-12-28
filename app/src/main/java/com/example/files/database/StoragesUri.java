package com.example.files.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class StoragesUri extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "storages_uri.db";
    public static final String FAV_TABLE_NAME = "storages_uri";
    public static final String FAV_COLUMN_ID = "id";
    public static final String FAV_COLUMN_PATH = "storage_path";
    public static final String FAV_COLUMN_NAME = "storage_name";
    public static final String FAV_COLUMN_URI = "storage_uri";

    public StoragesUri(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FAV_TABLE_NAME + " ("
                + FAV_COLUMN_ID + " integer primary key autoincrement, "
                + FAV_COLUMN_PATH + " text, "
                + FAV_COLUMN_NAME + " text, "
                + FAV_COLUMN_URI + " text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + FAV_TABLE_NAME);
        onCreate(db);
    }

    public void addStorage(String path, String name, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("insert into " + FAV_TABLE_NAME + " values (null, '" + path + "', '" + name + "', '" + uri + "');");
        db.close();
    }

    public void deleteStorage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + FAV_TABLE_NAME + " where id = " + id + ";");
        db.close();
    }

    public void updateStorage(String path, String name, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update " + FAV_TABLE_NAME + " set storage_path = '" + path + "', storage_name = '" + name + "', storage_uri = '" + uri + "' where name = " + name + ";");
        db.close();
    }

    @SuppressLint("Recycle")
    public boolean isStorageExist(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + FAV_TABLE_NAME + " where storage_name = '" + name + "'";
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount() > 0;
    }

    @SuppressLint("Recycle")
    public StorageUri getStorageUri(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + FAV_TABLE_NAME + " where storage_name = '" + name + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        return new StorageUri(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
    }

    public static class StorageUri {
        public int id;
        public String path;
        public String name;
        public String uri;

        public StorageUri(int id, String path, String name, String uri) {
            this.id = id;
            this.path = path;
            this.name = name;
            this.uri = uri;
        }
    }
}
