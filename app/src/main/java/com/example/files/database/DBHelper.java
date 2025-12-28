package com.example.files.database;

import static com.example.files.Statics.favorites;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.files.models.FavoriteItem;
import com.example.files.models.JFile;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    public static final String FAV_TABLE_NAME = "favorites";
    public static final String FAV_COLUMN_ID = "id";
    public static final String FAV_COLUMN_PATH = "path";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FAV_TABLE_NAME +
                " (" + FAV_COLUMN_ID + " integer primary key, " +
                FAV_COLUMN_PATH + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FAV_TABLE_NAME);
        onCreate(db);
    }

    public void insertPath(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FAV_COLUMN_PATH, path);
        db.insert(FAV_TABLE_NAME, null, contentValues);
    }

    public boolean update(Integer id, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FAV_COLUMN_PATH, path);
        db.update(FAV_TABLE_NAME, contentValues, "id = ? ", new String[] {Integer.toString(id)});
        return true;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, FAV_TABLE_NAME);
    }

    public void deletePath(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FAV_TABLE_NAME, "id = ? ", new String[]{Integer.toString(id)});
    }

    @SuppressLint("Range")
    public ArrayList<FavoriteItem> getAllPaths() {
        ArrayList<FavoriteItem> favItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor res = db.rawQuery("select * from " + FAV_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            favItems.add(new FavoriteItem(res.getString(res.getColumnIndex(FAV_COLUMN_PATH)),
                    res.getInt(res.getColumnIndex(FAV_COLUMN_ID))));
            res.moveToNext();
        }
        return favItems;
    }

    public void addToFavorites(JFile jFile) {
        if (numberOfRows() > 0) {
            boolean notExist = false;
            for (FavoriteItem fav : getAllPaths()) {
                if (jFile.getPath().equals(fav.getPath())) {
                    notExist = false;
                    break;
                } else notExist = true;
            }
            if (notExist) insertPath(jFile.getPath());
            Log.d("##### addToFavorites #####", "notExist = " + notExist);
        } else insertPath(jFile.getPath());
    }

    public boolean exist(JFile jFile) {
        if (numberOfRows() > 0) {
            for (FavoriteItem fav : getAllPaths()) {
                if (jFile.getPath().equals(fav.getPath())) return true;
            }
        }

        return false;
    }

    public boolean existPath(String path) {
        if (numberOfRows() > 0) {
            for (FavoriteItem fav : getAllPaths()) {
                if (path.equals(fav.getPath())) return true;
            }
        }

        return false;
    }

    public boolean allExist(ArrayList<JFile> jFiles) {

        for (JFile jFile : jFiles) {
            if (!exist(jFile)) return false;
        }
        return true;
    }

    public static int idFromPath(String path) {
        for (FavoriteItem fav : favorites.getAllPaths()) {
            if (fav.getPath().equals(path)) return fav.getId();
        }
        Log.d("##### idFromPath #####", "not exist");
        return -1;
    }

    @SuppressLint("Range")
    public int IDFromPath(String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor =  db.rawQuery("SELECT * FROM " + FAV_TABLE_NAME + " WHERE path LIKE '%" + path + "%'", null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) return -1;
        return cursor.getInt(cursor.getColumnIndex(FAV_COLUMN_ID));
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + FAV_TABLE_NAME + " where id=" + id + "", null);
    }
}
