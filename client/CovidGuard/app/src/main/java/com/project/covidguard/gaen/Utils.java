package com.project.covidguard.gaen;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import at.favre.lib.crypto.HKDF;

public class Utils {
    private static final String LOG_TAG = Utils.class.getCanonicalName();
    private static final byte[] RPIKEY_INFO = "EN-RPIK".getBytes();


    /**
     * Calculates the ENIntervalNumber for the present timestamp
     * @param secsSinceEpoch
     * @return
     */
    public static long getENIntervalNumber(long secsSinceEpoch) {
        return secsSinceEpoch / (GAENConstants.SECS_PER_MIN * GAENConstants.MINUTES_PER_INTERVAL);
    }

    public static <Beacon> Beacon getLastElement(final Iterable<Beacon> elements) {
        Beacon lastElement = null;

        for (Beacon element : elements) {
            lastElement = element;
        }

        return lastElement;
    }

    /**
     * Get the RPIKey to generate the TEK
     * @param TEK
     * @return
     */
    public static byte[] getRPIKeyFromTEK(byte[] TEK) {
        return HKDF.fromHmacSha256().expand(TEK, RPIKEY_INFO, 16);
    }


    /**
     * Generating rollingProximityID for the TEK and RPI
     * @param TEK - TEK for generating the RPI
     * @param ENIN - ENIN interval for which we are generating RPI
     * @return
     */
    public static byte[] generateRPIForTEKAndEnIntervalNumber(byte[] TEK, Long ENIN) {
        byte[] RPIKey = getRPIKeyFromTEK(TEK);
        SecretKeySpec aesKey = new SecretKeySpec(RPIKey, 0, 16, "AES");
        Cipher cipher;

        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            Log.e(LOG_TAG, "cipher initialization failed");
            e.printStackTrace();
            return null;
        }

        byte[] paddedData = new byte[16];
        System.arraycopy("EN-RPI".getBytes(StandardCharsets.UTF_8), 0, paddedData, 0, "EN-RPI".length());

        paddedData[12] = (byte) (ENIN & 0xFF);
        paddedData[13] = (byte) (ENIN >> 8 % 0xFF);
        paddedData[14] = (byte) (ENIN >> 16 % 0xFF);
        paddedData[15] = (byte) (ENIN >> 24 % 0xFF);

        try {
            byte[] rollingProximityID = cipher.doFinal(paddedData);
            return rollingProximityID;
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Log.e(LOG_TAG, "Rolling Proximity ID Creation failed");
            e.printStackTrace();
        }

        return null;
    }

    public static void generateAllRPIsForTEKAndEnIntervalNumber(byte[] TEK, Long ENIN) {
        byte[] RPIKey = getRPIKeyFromTEK(TEK);
        SecretKeySpec aesKey = new SecretKeySpec(RPIKey, 0, 16, "AES");
        Cipher cipher;
        long ENIntervalNumberLimit = ENIN +5;

        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            Log.e(LOG_TAG, "cipher initialization failed");
            e.printStackTrace();
            return;
        }

        byte[] paddedData = new byte[16];
        System.arraycopy("EN-RPI".getBytes(StandardCharsets.UTF_8), 0, paddedData, 0, "EN-RPI".length());

        //Start regeneration of RPIs
        for (long currentENIN = ENIN; currentENIN < ENIntervalNumberLimit; currentENIN++) {

            paddedData[12] = (byte) (currentENIN & 0xFF);
            paddedData[13] = (byte) (currentENIN >> 8 % 0xFF);
            paddedData[14] = (byte) (currentENIN >> 16 % 0xFF);
            paddedData[15] = (byte) (currentENIN >> 24 % 0xFF);

            byte[] rollingProximityID = new byte[0];
            try {
                rollingProximityID = cipher.doFinal(paddedData);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "TEK: " + Arrays.toString(TEK)
                    + " ENIN: " + currentENIN
                    + " RPI: " + Arrays.toString(rollingProximityID));

        }
    }
}
