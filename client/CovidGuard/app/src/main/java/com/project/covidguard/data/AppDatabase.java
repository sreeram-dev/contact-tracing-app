package com.project.covidguard.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.covidguard.R;
import com.project.covidguard.StorageUtils;
import com.project.covidguard.data.dao.RPIDao;
import com.project.covidguard.data.dao.TEKDao;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.entities.TEK;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

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
                String passphrase;
                try {
                    passphrase = getPassphraseFromSharedPreferences(context);
                } catch (GeneralSecurityException | IOException ex) {
                    Log.e(LOG_TAG, "Cannot open shared preferences using password");
                    return null;
                }
                SupportFactory factory = new SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()));
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .openHelperFactory(factory)
                        .build();
            }
        }

        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;

    }

    public abstract RPIDao rpiDao();
    public abstract TEKDao tekDao();

    private static String getPassphraseFromSharedPreferences(Context context) throws GeneralSecurityException, IOException {
        SharedPreferences pref = StorageUtils.getEncryptedSharedPref(
            context, context.getString(R.string.preference_file_key));

        if (!pref.contains("database-key")) {
            SharedPreferences.Editor editor = pref.edit();
            UUID uuid = UUID.randomUUID();
            editor.putString("database-key", uuid.toString());
            editor.commit();
        }

        return pref.getString("database-key", null);
    }
}
