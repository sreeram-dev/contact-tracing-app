
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
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.project.covidguard.ExposureKeyService;
import com.project.covidguard.R;
import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RegisterUUIDResponse;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.altbeacon.beacon.BeaconManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import at.favre.lib.crypto.HKDF;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSION_MAIN_REQUEST = 1;
    private static final int PERMISSION_REPEAT_REQUEST = 2;

    private static final String LOG_TAG = SplashActivity.class.getName();
    long ENIntervalNumber;
    byte[] RPIKey;
    SecretKeySpec aesKey;
    Cipher cipher;
    byte[] info = "EN-RPIK".getBytes();

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

        if (!isNetworkAvailable() && !isTokenPresent()) {
            Toast.makeText(this, "No Network connection available to store uuid", Toast.LENGTH_LONG).show();
        }

        if (isNetworkAvailable() && !isTokenPresent()) {
            generateAndStoreToken(uuid);
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
            SharedPreferences sharedPref = getEncryptedSharedPref(getString(R.string.preference_file_key));
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
                    ResponseBody res = response.errorBody();
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<ErrorResponse> errorResponseJsonAdapter = moshi.adapter(ErrorResponse.class);
                    try {
                        ErrorResponse err = errorResponseJsonAdapter.fromJson(res.source());
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

    public boolean isTokenPresent() {
        SharedPreferences sharedPref = null;
        try {
            sharedPref = getEncryptedSharedPref(getString(R.string.preference_file_key));
        } catch (Exception e) {
            Log.d(LOG_TAG, "Encrypted shared preferences is not accesible");
        }

        if (sharedPref == null) {
            Log.d(LOG_TAG, "Token could not be checked as the shared pref is not accessible");
            return false;
        }

        // if one of the keys, token and uuid, are absent, generate a new pair
        if (!sharedPref.contains("token") || !sharedPref.contains("uuid")) {
            return false;
        }

        Toast.makeText(this, "Token present in system", Toast.LENGTH_LONG).show();
        return true;
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

    private SharedPreferences getEncryptedSharedPref(String file) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        SharedPreferences sharedPref = EncryptedSharedPreferences.create(
                file,
                masterKeyAlias,
                getApplicationContext(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

        return sharedPref;
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
                    byte[] paddedData = new byte[16];

                    System.arraycopy("EN-RPI".getBytes(StandardCharsets.UTF_8), 0, paddedData, 0, "EN-RPI".length());

                    ENIntervalNumber = tek.getEnIntervalNumber();
                    long ENIntervalNumberLimit = ENIntervalNumber + 5;
                    RPIKey = HKDF.fromHmacSha256().expand(TEKByteArray, info, 16);
                    aesKey = new SecretKeySpec(RPIKey, 0, 16, "AES");

                    try {
                        cipher = Cipher.getInstance("AES/ECB/NoPadding");
                        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                        e.printStackTrace();
                    }

                    //Start regeneration of RPIs
                    for (long currentENIN = ENIntervalNumber; currentENIN < ENIntervalNumberLimit; currentENIN++) {

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
                        Log.d(LOG_TAG, "TEK: " + Arrays.toString(TEKByteArray)
                                + " ENIN: " + currentENIN
                                + " RPI: " + Arrays.toString(rollingProximityID));
                        RPIList.add(rollingProximityID);
                    }
                }
            }
        });
    }
}
