package com.project.covidguard;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
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


public class ExposureKeyService extends Service {

    volatile static byte[] TEK;
    volatile static byte[] RPIKey;
    volatile static byte[] rollingProximityID;
    static SecureRandom secureRandom;
    volatile static long ENIntervalNumber;
    private static final int SECS_PER_MIN = 60;
    private static final int MINUTES_PER_INTERVAL = 10;
    ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    TEKGenerator tekGenerator;
    RPIGenerator rpiGenerator;

    static volatile Cipher cipher;


    volatile static SecretKeySpec aesKey;


    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static long getENIntervalNumber(long secsSinceEpoch) {
        return secsSinceEpoch / (SECS_PER_MIN * MINUTES_PER_INTERVAL);
    }

    private static class TEKGenerator implements Runnable {


        @SuppressLint("SecureRandom")
        @Override
        public void run() {

            byte[] info = "EN-RPIK".getBytes();
            TEK = new byte[]{-42, -103, -22, -10, 69, -70, 95, -67, 71, 2, 125, -3, -86, 68, -30, -58};
//            TEK = secureRandom.generateSeed(16);
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
                ENIntervalNumber = getENIntervalNumber(System.currentTimeMillis() / 1000);
                paddedData[12] = (byte) (ENIntervalNumber & 0xFF);
                paddedData[13] = (byte) (ENIntervalNumber >> 8 % 0xFF);
                paddedData[14] = (byte) (ENIntervalNumber >> 16 % 0xFF);
                paddedData[15] = (byte) (ENIntervalNumber >> 24 % 0xFF);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                rollingProximityID = cipher.doFinal(paddedData);
                Log.d("ENIntervalNumber", String.valueOf(ENIntervalNumber));
                Log.d("RPI", Arrays.toString(rollingProximityID));
                String uuidString = "01020304-0506-0708-090a-0b0c0d0e0f10";
                BeaconParser beaconParser = new BeaconParser()
                        .setBeaconLayout("s:0-1=fd6f,p:-:-59,i:2-17");
                Beacon beacon = new Beacon.Builder()
                        .setId1(uuidString) //currently hardcoded, no data transmission but beacon detection works
                        .build();
                BeaconTransmitter beaconTransmitter = new
                        BeaconTransmitter(getApplicationContext(), beaconParser);
                beaconTransmitter.startAdvertising(beacon);
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                Log.d("RPI", "Issue");
                e.printStackTrace();
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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("GetInstance")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String input = intent.getStringExtra("inputExtra");
        tekGenerator = new TEKGenerator();
        rpiGenerator = new RPIGenerator();
        scheduleTaskExecutor.scheduleAtFixedRate(tekGenerator, 0, 60, TimeUnit.SECONDS );
        scheduleTaskExecutor.scheduleAtFixedRate(rpiGenerator, 0, 10, TimeUnit.SECONDS);
         /*
        This beacon layout is for the Exposure Notification service Bluetooth Spec
        That layout string above is what tells the library how to understand this new beacon type.
        The layout “s:0-1=fd6f,p:-:-59,i:2-17,d:18-21” means that the advertisement is a gatt service type (“s:”) with a 16-bit service UUID of 0xfd6f (“0-1=fd6f”)
        and it has a single 16-byte identifier in byte positions 2-17 of the advertisement (“i:2-17”)
        The “p:-:-59” indicates that there is no unencrypted measured power calibration reference transmitted with this beacon,
        and the library should default to using a 1-meter reference of -59 dBm for its built-in distance estimates.
         */


        Intent notificationIntent = new Intent(this, MainActivity.class);
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