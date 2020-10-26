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
import com.project.covidguard.StorageUtils;

import org.altbeacon.beacon.BeaconManager;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.lis.RegisterPatientResponse;
import com.project.covidguard.web.services.LISServerInterface;
import com.project.covidguard.web.services.LISService;


import java.io.IOException;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSION_MAIN_REQUEST = 1;
    private static final int PERMISSION_REPEAT_REQUEST = 2;

    private final String[] requiredPermissions =   new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION // manual permission required.
    };

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

        // If the token is registered with the verification server and location permission is granted,
        // do not show let's get started.
        if (StorageUtils.isTokenPresent(getApplicationContext(), LOG_TAG) &&
            this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent localIntent = new Intent(SplashActivity.this, DiagnoseActivity.class);
            // clear the backstack
            localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(localIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkAndRequestPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        // Permissions needed
        for (String permission : requiredPermissions) {
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

    private void registerWithLISServer(String uuid) {
        LISServerInterface service = LISService.getService();

        Call<RegisterPatientResponse> call = service.registerPatient(uuid);

        call.enqueue(new Callback<RegisterPatientResponse>() {
            @Override
            public void onResponse(Call<RegisterPatientResponse> call, Response<RegisterPatientResponse> response) {
                if (response.isSuccessful()) {
                    RegisterPatientResponse res = response.body();
                    StorageUtils.storePatientDetails(getApplicationContext(), res.getId());
                    Log.d(LOG_TAG, "Patient Registered Successfully: res: " + res.toString());
                } else {
                    try {
                        ErrorResponse err = ErrorResponse.buildFromSource(response.errorBody().source());
                        Log.d(LOG_TAG, "Patient Registration Failed: err: " + err.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterPatientResponse> call, Throwable t) {
                Log.d(LOG_TAG, "Request to LIS Server failed");
            }
        });
    }
}
