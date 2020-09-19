package com.project.covidguard;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.project.covidguard.activities.SplashActivity;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.data.repositories.TEKRepository;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import at.favre.lib.crypto.HKDF;

import static com.project.covidguard.App.CHANNEL_ID;



public class ExposureKeyService extends Service implements BeaconConsumer {

    private static final String LOG_TAG = ExposureKeyService.class.getCanonicalName();
    private static final Integer SECS_PER_MIN = 60;
    private static final Integer MINUTES_PER_INTERVAL = 1;

    // Volatile variables accessed by RPI and TEK Generator
    volatile static byte[] TEK;
    volatile static byte[] TEK1;
    volatile static byte[] RPIKey;
    volatile static byte[] rollingProximityID;
    volatile static long ENIntervalNumber;
    static volatile Cipher cipher;
    volatile static SecretKeySpec aesKey;

    static SecureRandom secureRandom;

    /*
      This beacon layout is for the Exposure Notification service Bluetooth Spec
      That layout string above is what tells the library how to understand this new beacon type.
      The layout “s:0-1=fd6f,p:-:-59,i:2-17,d:18-21” means that the advertisement is a gatt service type (“s:”) with a 16-bit service UUID of 0xfd6f (“0-1=fd6f”)
      and it has a single 16-byte identifier in byte positions 2-17 of the advertisement (“i:2-17”)
      The “p:-:-59” indicates that there is no unencrypted measured power calibration reference transmitted with this beacon,
      and the library should default to using a 1-meter reference of -59 dBm for its built-in distance estimates.
    */
    private static final String BEACON_LAYOUT = "s:0-1=fd6f,p:-:-59,i:2-17";

    ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    TEKGenerator tekGenerator;
    RPIGenerator rpiGenerator;

    BeaconManager beaconManager;

    public RPIRepository rpiRepo;

    @Override
    public void onCreate() {
        super.onCreate();
        rpiRepo = new RPIRepository(getApplicationContext());
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_LAYOUT));
        beaconManager.bind(this);
    }

    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static long getENIntervalNumber(long secsSinceEpoch) {
        return secsSinceEpoch / (1000 * (SECS_PER_MIN * MINUTES_PER_INTERVAL));
    }

    public static <Beacon> Beacon getLastElement(final Iterable<Beacon> elements) {
        Beacon lastElement = null;

        for (Beacon element : elements) {
            lastElement = element;
        }

        return lastElement;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier((Collection<Beacon> beacons, Region region) -> {
            if (beacons.size() != 0) {
                Beacon beacon = getLastElement(beacons);
                ZonedDateTime time = LocalDateTime.now().atZone(ZoneId.systemDefault());
                rpiRepo.storeReceivedRPI(beacon.getId1().toString(), time.toEpochSecond());
                Log.d(LOG_TAG, "didRangeBeaconsInRegion: UUID: " + beacon.getId1().toString()
                        + "\nTime: " + time.toString()
                        + "\nRSSI: " + beacon.getRssi()
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

    private static class TEKGenerator implements Runnable {

        private TEKRepository tekRepo;

        public TEKGenerator(Context context) {
            this.tekRepo = new TEKRepository(context);
        }

        @SuppressLint("SecureRandom")
        @Override
        public void run() {
            byte[] info = "EN-RPIK".getBytes();
            //TEK1 = new byte[]{-42, -103, -22, -10, 69, -70, 95, -67, 71, 2, 125, -3, -86, 68, -30, -59};
            TEK = secureRandom.generateSeed(16);
            ZonedDateTime time = LocalDateTime.now().atZone(ZoneId.systemDefault());
            Long enIntervalNumber = getENIntervalNumber(time.toEpochSecond());
            if (!tekRepo.tekExistsForInterval(enIntervalNumber)) {
                String encodedTek = Base64.encodeToString(TEK, Base64.DEFAULT);
                tekRepo.storeTEKWithEnIntervalNumber(encodedTek, enIntervalNumber);
            } else {
                com.project.covidguard.data.entities.TEK tek  = tekRepo.getTekWithInterval(enIntervalNumber);
                String encodedTek = tek.getTekId();
                TEK = Base64.decode(encodedTek, Base64.DEFAULT);
                Log.d(LOG_TAG, "tekExists: " + " " + Arrays.toString(TEK));

            }

            RPIKey = HKDF.fromHmacSha256().expand(TEK, info, 16);
            Log.d("TEK", Arrays.toString(TEK));
            Log.d("RPIKey", Arrays.toString(RPIKey));
            aesKey = new SecretKeySpec(RPIKey, 0, 16, "AES");
        }

    }

    private class RPIGenerator implements Runnable {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            try {

                byte[] paddedData = new byte[16];

                System.arraycopy("EN-RPI".getBytes(StandardCharsets.UTF_8), 0, paddedData, 0, "EN-RPI".length());
                ENIntervalNumber = getENIntervalNumber(System.currentTimeMillis());
                paddedData[12] = (byte) (ENIntervalNumber & 0xFF);
                paddedData[13] = (byte) (ENIntervalNumber >> 8 % 0xFF);
                paddedData[14] = (byte) (ENIntervalNumber >> 16 % 0xFF);
                paddedData[15] = (byte) (ENIntervalNumber >> 24 % 0xFF);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                rollingProximityID = cipher.doFinal(paddedData);
                Log.d("ENIntervalNumber", String.valueOf(ENIntervalNumber));
                Log.d("RPI", Arrays.toString(rollingProximityID));
                Log.d("RPIString", Identifier.fromBytes(rollingProximityID, 0, 16, false).toString());
                BeaconParser beaconParser = new BeaconParser()
                        .setBeaconLayout(BEACON_LAYOUT);

                Beacon beacon = new Beacon.Builder()
                        .setId1(Identifier.fromBytes(rollingProximityID, 0, 16, false).toString())
                        .build();
                BeaconTransmitter beaconTransmitter = new
                        BeaconTransmitter(getApplicationContext(), beaconParser);
                beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        Log.e(LOG_TAG, "Advertisement Failed because of error code: " + errorCode);
                    }
                });


            } catch (BadPaddingException | IllegalBlockSizeException | NullPointerException | ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                Log.d("RPI", "Issue");
                e.printStackTrace();
            } catch (InvalidKeyException key) {
                Log.d("AESKey", "Started generation");
            }

        }
    }

    @SuppressLint("GetInstance")
    public ExposureKeyService() {
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"GetInstance", "WakelockTimeout"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String input = "Do not force stop this";

        tekGenerator = new TEKGenerator(getApplicationContext());
        rpiGenerator = new RPIGenerator();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExposureService::ExposureNotificationService");
        wakeLock.acquire();

        scheduleTaskExecutor.scheduleAtFixedRate(tekGenerator, 0, 30, TimeUnit.MINUTES);
        scheduleTaskExecutor.scheduleAtFixedRate(rpiGenerator, 0, 1, TimeUnit.MINUTES);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
