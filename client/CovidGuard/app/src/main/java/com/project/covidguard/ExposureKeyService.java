package com.project.covidguard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.project.covidguard.activities.DiagnoseActivity;
import com.project.covidguard.activities.SplashActivity;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.gaen.BLEAdvertiser;
import com.project.covidguard.gaen.GAENConstants;
import com.project.covidguard.gaen.RPIGenerator;
import com.project.covidguard.gaen.Utils;
import com.project.covidguard.tasks.MatchMakerTask;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import static com.project.covidguard.App.CHANNEL_ID;
import static com.project.covidguard.gaen.GAENConstants.BEACON_LAYOUT;
import static com.project.covidguard.gaen.GAENConstants.RPI_INTERVAL;
import static com.project.covidguard.gaen.GAENConstants.RPI_TIME_UNIT;


/**
 * Schedule with an rpi key at a fixed rate.
 * TEK - every 30 minutes - Generates an RPIKey - is this thread still running? no? after generating RPIKey? exit the thread?
 * RPI Generator(RPIKey) - > old code - it is a shared variable.
 * RPIKey call 1 - first call -- generate an RPI -- start advertising -- will this advertisement stop?
 * after 1 minute
 * RPI Generator - second call -- generate an RPI -- start advertising -- previous advertisement stops, default channel, new advertisement starts.
 * After 30 minutes --
 * Generate new TEK and RPIKey -
 * -----
 * OLD TEK, RPIKey will still be scheduled? old code - shared variable - new code - no shared variable - new code - how can we stop the old rpi generator scheduled service.
 * Old code -
 * They will use the same RPIKey - multiple scheduled RPIGenerator threads - how are we stopping the first scheduled thread.
 * Close old Scheduled Server of Old TEK based RPI Generator
 * Start new Scheduled Server of New TEK based RPI Generator
 */
public class ExposureKeyService extends  LifecycleService  implements BeaconConsumer {

    private static final String LOG_TAG = ExposureKeyService.class.getCanonicalName();

    private AppExecutors executors;
   public static SecureRandom secureRandom;

    RPIRepository rpiRepo;
    BeaconManager beaconManager;

    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executors = AppExecutors.getInstance();
        rpiRepo = new RPIRepository(getApplicationContext());
        // TODO Bind beacon manager after binding Service?
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_LAYOUT));
        beaconManager.bind(this);
    }

    @SuppressLint({"GetInstance", "WakelockTimeout"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        String input = "Do not force stop this";

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
            "ExposureService::ExposureNotificationService");
        wakeLock.acquire();

        List<Observer> rpiObservers = new ArrayList<>();
        rpiObservers.add(new BLEAdvertiser(getApplicationContext(), GAENConstants.ADVERTISING_INTERVAL));

        executors.scheduleIO().scheduleAtFixedRate(
            new RPIGenerator(secureRandom, getApplicationContext(), rpiObservers), 0, RPI_INTERVAL, RPI_TIME_UNIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            scheduleContactMatcher();
        }

        Intent notificationIntent = new Intent(this, SplashActivity.class);

        // If the token is registered with the verification server and location permission is granted,
        // do not show let's get started.
        if (StorageUtils.isTokenPresent(getApplicationContext(), LOG_TAG) &&
            this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            notificationIntent = new Intent(this, DiagnoseActivity.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
            0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Exposure Notification Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_android)
            .setContentIntent(pendingIntent)
            .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    /**
     * Periodically schedule download and matching of keys
     */
    public void scheduleContactMatcher() {
        WorkManager wm = WorkManager.getInstance(getApplicationContext());

        Constraints constraints = new Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(MatchMakerTask.class, 30, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build();

        wm.enqueueUniquePeriodicWork(MatchMakerTask.TAG, ExistingPeriodicWorkPolicy.REPLACE, request);
        wm.getWorkInfosByTagLiveData(MatchMakerTask.TAG).observe(this, new androidx.lifecycle.Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                for (WorkInfo workInfo : workInfos) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Data data1 = workInfo.getOutputData();
                        Boolean isPositive = data1.getBoolean("is_positive", false);
                        Log.d(LOG_TAG, "DatafromTask: " + data1.toString());
                        if (isPositive) {
                            Log.d(LOG_TAG, "Positive contact with infected patient");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                raisePositiveNotification();
                            }
                        } else {
                            // if there is a positive contact notification, cancel it if launched by exposure key service
                            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
                            manager.cancel(DiagnoseActivity.POSITIVE_CONTACT_NOTIFICATION_ID);
                        }
                    }
                }
            }
        });
    }

    private void raisePositiveNotification() {
        Intent intent = new Intent(getApplicationContext(), DiagnoseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        String title = "Possible COVID-19 Contact";
        String message = "You have been contact with a COVID-19 Patient, Please get yourself checked";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setLights(0xff0000ff, 2000, 500) // Blue color light flash for 2s on and 0.5 off
            .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(DiagnoseActivity.POSITIVE_CONTACT_NOTIFICATION_ID, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier((Collection<Beacon> beacons, Region region) -> {
            if (beacons.size() != 0) {
                Beacon beacon = Utils.getLastElement(beacons);
                ZonedDateTime time = LocalDateTime.now().atZone(ZoneId.systemDefault());
                rpiRepo.storeReceivedRPI(beacon.getId1().toString(), beacon.getRunningAverageRssi(),
                    beacon.getDistance(), beacon.getTxPower(), time.toEpochSecond());
                Log.d(LOG_TAG, "didRangeBeaconsInRegion: "
                        + "\nUUID: " + beacon.getId1()
                        + "\nTIME: " + time.toString()
                        + "\nRSSI: " + beacon.getRunningAverageRssi()
                        + "\nTX: " + beacon.getTxPower()
                        + "\nDISTANCE: " + beacon.getDistance());
            }
        });

        try {
            Region region = new Region("com.project.covidguard.ENRegion", null, null, null);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
