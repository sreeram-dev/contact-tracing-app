package com.project.covidguard;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.project.covidguard.data.AppDatabase;

import org.conscrypt.Conscrypt;

import java.security.Security;


public class App extends Application {

    public static final String CHANNEL_ID = "temporaryExposureKeyChannel";

    static DatabaseHelper KEY_SERVER_DB;
    static DatabaseHelper1 RPI_SERVER_DB;

    private static final String LOG_TAG = "CovidGuardApplication";

    // Executor Pool to execute network, thread and IO
    private AppExecutors mExecutors;

    // Perform App DB
    private AppDatabase mDB;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "Initialising App Executors");
        mExecutors = AppExecutors.getInstance();
        Log.d(LOG_TAG, "Initialising SQLITE Database");
        mDB = AppDatabase.getInstance(getApplicationContext());

        // Add conscrypt if the android version is less than SDK Level 29
        // TLS 1.3 is by default in Android Version Q
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
        }
        createNotificationChannel();
        KEY_SERVER_DB=new DatabaseHelper(this);
        RPI_SERVER_DB=new DatabaseHelper1(this);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Temporary Exposure Key Generation",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // terminate the executors
        if (mExecutors != null) {
            Log.d(LOG_TAG, "Shutting down async executors");
            mExecutors.shutdownServices();
        }

        // close the database on app termination
        if (mDB != null && mDB.isOpen()) {
            Log.d(LOG_TAG, "Shutting down database");
            mDB.close();
        }
    }

    public AppExecutors getExecutors() {
        return mExecutors;
    }

    public AppDatabase getDB() {
        return mDB;
    }
}
