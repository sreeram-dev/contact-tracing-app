package com.project.covidguard;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


    public class DatabaseHelper1 extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "rpi.db";
        public static final String TABLE_NAME = "rpi_table";
        public static final String COL_1 = "ID";
        public static final String COL_2 = "RPI";
//        public static final String COL_3 = "Timestamp";

        public DatabaseHelper1(Context context) {
            super(context, DATABASE_NAME, null, 1);
            SQLiteDatabase db = this.getWritableDatabase();

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_NAME +" ( ID INTEGER PRIMARY KEY AUTOINCREMENT, RPI TEXT UNIQUE)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(db);
        }
        public boolean insertData(String RPI) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_2,RPI);

            long result = db.insert(TABLE_NAME,null ,contentValues);
            if(result == -1)
                return false;
            else
                return true;
        }

    }
