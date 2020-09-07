package com.project.covidguard;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import org.conscrypt.Conscrypt;

import java.security.Security;


public class App extends Application {
    public static final String CHANNEL_ID = "temporaryExposureKeyChannel";
static DatabaseHelper mydb;

    @Override
    public void onCreate() {
        super.onCreate();

        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        createNotificationChannel();
        mydb=new DatabaseHelper(this);

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
}
