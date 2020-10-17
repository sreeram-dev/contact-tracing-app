package com.project.covidguard;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.project.covidguard.activities.SplashActivity;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.gaen.BLEAdvertiser;
import com.project.covidguard.gaen.GAENConstants;
import com.project.covidguard.gaen.RPIGenerator;
import com.project.covidguard.gaen.Utils;

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
public class ExposureKeyService extends Service implements BeaconConsumer {

    private static final String LOG_TAG = ExposureKeyService.class.getCanonicalName();

    private AppExecutors executors;
    static SecureRandom secureRandom;

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

        String input = "Do not force stop this";

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExposureService::ExposureNotificationService");
        wakeLock.acquire();

        List<Observer> rpiObservers = new ArrayList<>();
        rpiObservers.add(new BLEAdvertiser(getApplicationContext(), GAENConstants.ADVERTISING_INTERVAL));

        executors.scheduleIO().scheduleAtFixedRate(
                new RPIGenerator(secureRandom, getApplicationContext(), rpiObservers), 0, RPI_INTERVAL, RPI_TIME_UNIT);

        Intent notificationIntent = new Intent(this, SplashActivity.class);
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
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
