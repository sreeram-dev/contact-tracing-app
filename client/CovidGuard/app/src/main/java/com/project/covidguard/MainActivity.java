package com.project.covidguard;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.project.covidguard.web.services.VerificationService;
import com.project.covidguard.web.services.VerificationServiceImpl;

import org.altbeacon.beacon.BeaconManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST= 1;

    private static final String TAG = MainActivity.class.getName();;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyBluetooth();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        ArrayList<String>  permissionsNeeded = new ArrayList<>();

        // Permissions needed
        for (String permission: permissions) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        // Permissions for which you can ask the user
        ArrayList<String> manualPermissionRequests = new ArrayList<>();
        ArrayList<String> autoPermissionRequests = new ArrayList<>();

        for (String permission: permissionsNeeded) {
            if (!this.shouldShowRequestPermissionRationale(permission)) {
                manualPermissionRequests.add(permission);
            } else {
                autoPermissionRequests.add(permission);
            }
        }

        if (!autoPermissionRequests.isEmpty()) {
            String[] askPermission = new String[autoPermissionRequests.size()];
            for (int i=0; i<autoPermissionRequests.size(); i++) {
                askPermission[i] = autoPermissionRequests.get(i);
            }

            requestPermissions(askPermission, PERMISSION_REQUEST);
        }

        if (!manualPermissionRequests.isEmpty()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs additional permissions");
            StringBuilder sb = new StringBuilder("Please grant access to the following permissions. \n");
            for (int i=0; i<manualPermissionRequests.size(); i++) {
                sb.append((i+1) + ": " + manualPermissionRequests.get(i) + "\n");
            }

            builder.setMessage(sb.toString());
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onDismiss(DialogInterface dialog) {
                }
            });

            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        ArrayList<String> compulsoryPermissions = new ArrayList<String>() {
            {
                add(Manifest.permission.ACCESS_FINE_LOCATION);
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        };

        ArrayList<String> compulsoryNotGranted = new ArrayList<>();

        for (int i=0; i<permissions.length; i++) {
            if (compulsoryPermissions.contains(permissions[i]) && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                compulsoryNotGranted.add(permissions[i]);
            } else {
                Log.d(TAG, permissions[i] + " has been granted");
            }
        }

        if (!compulsoryNotGranted.isEmpty()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Functionality limited");
            builder.setMessage("Location permissions have not been granted.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                }

            });
        }
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();

        }

    }

    public void clickRegistrationHandler(View view) {

        final String uuid = UUID.randomUUID().toString().replace("-", "");
/*
        new RegisterUUIDTask().execute();

        try {

            String token = service.registerUUIDAndGetToken(uuid);
            SharedPreferences sharedPref = getApplicationContext()
                    .getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(token, uuid);
            editor.commit();

        } catch (IOException exception) {
            exception.printStackTrace();
            Toast.makeText(this, "App Registration Failed", Toast.LENGTH_LONG).show();
        }
*/
        Toast.makeText(this, uuid, Toast.LENGTH_LONG).show();
        Intent serviceIntent = new Intent(this, ExposureKeyService.class);
        serviceIntent.putExtra("inputExtra", "Do not force stop this");
        ContextCompat.startForegroundService(this, serviceIntent);
        setContentView(R.layout.diagnose_fragment);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void termsConditionsLink(View view) {
        TextView termsConditions = findViewById(R.id.textView7);
        termsConditions.setMovementMethod(LinkMovementMethod.getInstance());
    }



}
