package com.project.covidguard;


import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private Cipher encryptCipher;

    @SuppressLint("GetInstance")
    AES(SecretKey key) {
        try {
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            System.out.println("Failed in initialization");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] encrypt(byte[] utf8) {
        try {
            byte[] enc = encryptCipher.doFinal(utf8);
            return Base64.getEncoder().encode(enc);
        } catch (Exception e) {
            System.out.println("Failed in Encryption");
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] main(byte[] RPIKey, byte[] paddedData) {
        SecretKey key = new SecretKeySpec(RPIKey, "AES");
        AES encipher = new AES(key);

        return encipher.encrypt(paddedData);
    }
}
