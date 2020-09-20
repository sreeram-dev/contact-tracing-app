package com.project.covidguard.data;


import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.covidguard.data.dao.RPIDao;
import com.project.covidguard.data.dao.TEKDao;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.entities.TEK;

/**
 * SQLite Database
 * <TEK, ENIntervalNumber>
 * <RPI> </RPI>
 *
 */
@Database(entities = {TEK.class, RPI.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "covidguard";
    private static AppDatabase sInstance;

    public static AppDatabase getDatabase(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract RPIDao rpiDao();
    public abstract TEKDao tekDao();
}
