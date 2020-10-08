package com.project.covidguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class StorageUtils {


    /**
     *
     * @param context The context in which the call is being made
     * @param TAG The calling tag of the service
     * @return
     */
    public static Boolean isTokenPresent(Context context, String TAG) {
        SharedPreferences sharedPref = null;

        try {
            sharedPref = getEncryptedSharedPref(context, context.getString(R.string.preference_file_key));
        } catch (Exception e) {
            Log.d(TAG, "Encrypted shared preferences is not accesible");
        }

        if (sharedPref == null) {
            Log.d(TAG, "Token could not be checked as the shared pref is not accessible");
            return false;
        }

        // if one of the keys, token and uuid, are absent, generate a new pair
        if (!sharedPref.contains("token") || !sharedPref.contains("uuid")) {
            return false;
        }


        return true;
    }

    public static SharedPreferences getEncryptedSharedPref(Context context, String file) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        SharedPreferences sharedPref = EncryptedSharedPreferences.create(
                file,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

        return sharedPref;
    }

}
