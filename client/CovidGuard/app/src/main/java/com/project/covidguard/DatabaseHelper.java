package com.project.covidguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Date;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "tek.db";
    public static final String TABLE_NAME = "covid_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "TEK";
    public static final String COL_3 = "Timestamp";
    public static final String COL_4 = "ENInterval";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,TEK TEXT,timestamp DATE DEFAULT (datetime('now','localtime')), ENInterval TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String TEK) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,TEK);

        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public Cursor geLastData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME+" Order by ID DESC LIMIT 1",null);
        return res;
    }

    public Boolean updateData(String id, String valueOf) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put("ENInterval", valueOf);
        db.update(TABLE_NAME, values,"ID = ?",new String[]{id});
        return true;
    }
    public void deleteData () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM covid_table WHERE timestamp <= date('now','-15 day')");

    }

    public Boolean updateData1() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(COL_3, "2020-08-10 05:18:00");
        db.update(TABLE_NAME, values,"ID = ?",new String[]{"1"});
        return true;
    }
}
