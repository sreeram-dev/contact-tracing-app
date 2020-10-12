
package com.project.covidguard.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.project.covidguard.R;

import org.altbeacon.beacon.BeaconManager;

import java.util.ArrayList;


public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSION_MAIN_REQUEST = 1;
    private static final int PERMISSION_REPEAT_REQUEST = 2;

    private static final String LOG_TAG = SplashActivity.class.getName();


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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkAndRequestPermissions() {
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION // manual permission required.
        };

        ArrayList<String> permissionsNeeded = new ArrayList<>();

        // Permissions needed
        for (String permission : permissions) {
            if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            String[] askPermission = permissionsNeeded.toArray(new String[permissionsNeeded.size()]);
            requestPermissions(askPermission, PERMISSION_MAIN_REQUEST);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {


        ArrayList<String> compulsoryPermissions = new ArrayList<String>() {
            {
                add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        };


        switch (requestCode) {
            case PERMISSION_MAIN_REQUEST:
                ArrayList<String> deniedPermissions = new ArrayList<>();
                for (int i = 0; i < permissions.length; i++) {
                    if (compulsoryPermissions.contains(permissions[i]) &&
                        grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    } else {
                        Log.d(LOG_TAG, permissions[i] + " has been granted");
                    }
                }

                if (!deniedPermissions.isEmpty()) {
                    showPermissionRationaleDialog(deniedPermissions);
                }
                break;

            case PERMISSION_REPEAT_REQUEST:
                deniedPermissions = new ArrayList<>();
                for (int i = 0; i < permissions.length; i++) {
                    if (compulsoryPermissions.contains(permissions[i]) &&
                        grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }

                // We have already asked for permissions and have been denied
                if (!deniedPermissions.isEmpty()) {
                    showLimitedFunctionalityDialog(deniedPermissions);
                }
                break;

            default:
                break;
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
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE  not available");
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

    public void termsConditionsLink(View view) {
        TextView termsConditions = findViewById(R.id.textView7);
        termsConditions.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showPermissionRationaleDialog(ArrayList<String> deniedPermissions) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("We need the permissions");
        builder.setMessage("Compulsory permissions have not been granted. " +
            "They are needed for full functionality of the app");
        builder.setNegativeButton(R.string.dont_allow, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String[] request = deniedPermissions.toArray(new String[deniedPermissions.size()]);
                requestPermissions(request, PERMISSION_REPEAT_REQUEST);
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                showLimitedFunctionalityDialog(deniedPermissions);
            }

        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                showLimitedFunctionalityDialog(deniedPermissions);

            }
        });

        builder.create().show();
    }

    private void showLimitedFunctionalityDialog(ArrayList<String> deniedPermissions) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Functionality limited");
        StringBuilder sb = new StringBuilder();
        sb.append("The functionality of the app is limited as the required permissions have not been granted\n");
        for (int i = 0; i < deniedPermissions.size(); i++) {
            sb.append((i + 1) + ": " + deniedPermissions.get(i) + "\n");
        }
        builder.setMessage(sb.toString());
        builder.setPositiveButton(android.R.string.ok, null);

        builder.create().show();
    }

    public void clickRegistrationHandler(View view) {
        Intent localIntent = new Intent(SplashActivity.this, DiagnoseActivity.class);
        startActivity(localIntent);
    }
}
