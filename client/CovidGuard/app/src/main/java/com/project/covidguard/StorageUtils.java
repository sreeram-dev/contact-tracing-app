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

    public static Boolean isPatientRegistered(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
            "user_details", Context.MODE_PRIVATE);

        if (!sharedPreferences.contains("patient_profile_id")) {
            return false;
        }

        return true;
    }

    public static void storePatientDetails(Context context, String id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
            "user_details", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("patient_profile_id", id);
        editor.commit();
    }

    public static String getUUIDFromSharedPreferences(Context context) {
        SharedPreferences sharedPref = null;
        try {
            sharedPref = getEncryptedSharedPref(context, context.getString(R.string.preference_file_key));
        } catch (Exception e) {
            Log.d("StoregeUtils", "Encrypted shared preferences is not accesible");
        }

        if (sharedPref == null) {
            Log.d("StorageUtils", "Token could not be checked as the shared pref is not accessible");
            return "";
        }

        return sharedPref.getString("uuid", "");
    }
}
