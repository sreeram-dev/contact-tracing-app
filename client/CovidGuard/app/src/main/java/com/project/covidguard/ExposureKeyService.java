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
//            bytesTemp = secureRandom.generateSeed(16);
            RPIKey = HKDF.fromHmacSha256().expand(TEK, info, 16);
            Log.d("TEK", Arrays.toString(TEK));
            Log.d("RPIKey", Arrays.toString(RPIKey));
            aesKey = new SecretKeySpec(RPIKey, 0, 16, "AES");


        }
    }

    private static class RPIGenerator implements Runnable {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            try {
                byte[] paddedData = new byte[16];

                System.arraycopy("EN-RPI".getBytes(StandardCharsets.UTF_8), 0, paddedData, 0, "EN-RPI".length());//                byte[] RPI = AES.main(outputKeyingMaterial, paddedData);


                ENIntervalNumber = getENIntervalNumber(System.currentTimeMillis() / 1000);

                paddedData[12] = (byte) (ENIntervalNumber & 0xFF);
                paddedData[13] = (byte) (ENIntervalNumber >> 8 % 0xFF);
                paddedData[14] = (byte) (ENIntervalNumber >> 16 % 0xFF);
                paddedData[15] = (byte) (ENIntervalNumber >> 24 % 0xFF);


                cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                byte[] rollingProximityID = cipher.doFinal(paddedData);
                System.out.println(Arrays.toString(rollingProximityID));

                Log.d("RPI", Arrays.toString(rollingProximityID));
//                Log.d("MilliSeconds", String.valueOf(((System.currentTimeMillis()/1000))/60*10));

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
        scheduleTaskExecutor.scheduleAtFixedRate(tekGenerator, 0, 6, TimeUnit.MINUTES);
        scheduleTaskExecutor.scheduleAtFixedRate(rpiGenerator, 0, 2, TimeUnit.MINUTES);


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