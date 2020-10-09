
package com.project.covidguard.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.project.covidguard.ExposureKeyService;
import com.project.covidguard.R;
import com.project.covidguard.StorageUtils;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.gaen.Utils;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RegisterUUIDResponse;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;

import org.altbeacon.beacon.BeaconManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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

    public void clickRegistrationHandler(View view) {
        final String uuid = UUID.randomUUID().toString().replace("-", "");

        if (!isNetworkAvailable() && !StorageUtils.isTokenPresent(getApplicationContext(), LOG_TAG)) {
            Toast.makeText(this, "No Network connection available to store uuid", Toast.LENGTH_LONG).show();
        }

        if (isNetworkAvailable() && !StorageUtils.isTokenPresent(getApplicationContext(), LOG_TAG)) {
            generateAndStoreToken(uuid);
        } else {
            Toast.makeText(this, "Token present in system", Toast.LENGTH_LONG).show();
        }

        Intent serviceIntent = new Intent(this, ExposureKeyService.class);
        serviceIntent.putExtra("inputExtra", "Do not force stop this");
        ContextCompat.startForegroundService(this, serviceIntent);
        setContentView(R.layout.diagnose_fragment);
    }

    public void clickSubmitHandler(View view) {
        Toast.makeText(this, "Submitted", Toast.LENGTH_LONG).show();
        TEKRepository repo = new TEKRepository(getApplicationContext());
        LiveData<List<TEK>> teks = repo.getAllTEKs();
        teks.observe(this, new Observer<List<TEK>>() {
            @Override
            public void onChanged(List<TEK> teks) {
                Log.d(LOG_TAG, "Recent TEKS from SQLITE: " + teks.size());
            }
        });

    }

    private void storeUUIDAndToken(String uuid, String token) {
        try {
            SharedPreferences sharedPref = StorageUtils.getEncryptedSharedPref(
                    getApplicationContext(), (getString(R.string.preference_file_key)));
            SharedPreferences.Editor editor = sharedPref.edit();
            // token and uuid are related.
            editor.putString("token", token);
            editor.putString("uuid", uuid);
            editor.commit();
            Log.d(LOG_TAG, "App has been registered with uuid: " + uuid);
            Toast.makeText(this, "App Registration Succeeded", Toast.LENGTH_LONG).show();
        } catch (GeneralSecurityException | IOException exception) {
            exception.printStackTrace();
            Log.d(LOG_TAG, "MasterKey Creation for encrypted shared preferences failed " + uuid);
            Toast.makeText(this, "Shared Preferences Security Registration Failed", Toast.LENGTH_LONG).show();
        }
    }

    private void generateAndStoreToken(String uuid) {
        VerificationEndpointInterface verificationService = VerificationService.getService();
        Call<RegisterUUIDResponse> response = verificationService.registerUUID(uuid);
        response.enqueue(new Callback<RegisterUUIDResponse>() {
            @Override
            public void onResponse(Call<RegisterUUIDResponse> call, Response<RegisterUUIDResponse> response) {
                if (response.isSuccessful()) {
                    RegisterUUIDResponse res = response.body();
                    String token = res.getToken();
                    storeUUIDAndToken(uuid, token);
                    Toast.makeText(getApplicationContext(), "App Registration Succeeded", Toast.LENGTH_LONG).show();
                } else {

                    try {
                        ErrorResponse err = ErrorResponse.buildFromSource(response.errorBody().source());
                        Log.d(LOG_TAG, "App Registration failed with error code: " + err.toString());
                        Log.d(LOG_TAG, "Request URL: " + call.request().url());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Toast.makeText(getApplicationContext(), "App Registration Failed", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterUUIDResponse> call, Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }

        return true;
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

    public void clickMatchMaker(View view) {
        TEKRepository repo = new TEKRepository(getApplicationContext());
        LiveData<List<TEK>> teks = repo.getAllTEKs();
        ArrayList<byte[]> RPIList = new ArrayList<>();
        teks.observe(this, new Observer<List<TEK>>() {
            @SuppressLint("GetInstance")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChanged(List<TEK> teks) {
                for (TEK tek : teks) {
                    //initialise GAEN variables based on fetched TEK and ENIN

                    byte[] TEKByteArray = Base64.decode(tek.getTekId(), Base64.DEFAULT);
                    long ENIntervalNumber = tek.getEnIntervalNumber();
                    Utils.generateAllRPIsForTEKAndEnIntervalNumber(TEKByteArray, ENIntervalNumber);
                }
            }
        });
    }

    public void clickDeveloperMetricsHandler(View view) {

        setContentView(R.layout.metrics);
    }

    public void clickTEKMetric(View view) {

        TEKRepository repo = new TEKRepository(getApplicationContext());
        TEK currentTEK = repo.getLastTek();
        byte[] currentTEKByteArray = Base64.decode(currentTEK.getTekId(), Base64.DEFAULT);
        Toast.makeText(getApplicationContext(), "Current RPIs are derived from the TEK: " + Arrays.toString(currentTEKByteArray), Toast.LENGTH_SHORT).show();

    }

    public void clickRPIMetric(View view) {
        RPIRepository repo = new RPIRepository(getApplicationContext());
        RPI rpi = repo.getLastRPI();

        if (rpi.rpi.isEmpty())
            Toast.makeText(getApplicationContext(), "No RPI is currently being received", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Current anonymised RPI being received is: " + rpi.rpi, Toast.LENGTH_SHORT).show();


    }

    public void clickENINMetric(View view) {
        TEKRepository repo = new TEKRepository(getApplicationContext());
        TEK currentENIN = repo.getLastTek();

        Toast.makeText(getApplicationContext(), "Current TEK is derived from the ENIntervalNumber: " + currentENIN.getEnIntervalNumber(), Toast.LENGTH_SHORT).show();

    }
}
