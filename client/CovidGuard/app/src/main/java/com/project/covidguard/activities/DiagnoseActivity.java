
package com.project.covidguard.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.project.covidguard.ExposureKeyService;
import com.project.covidguard.R;
import com.project.covidguard.StorageUtils;
import com.project.covidguard.data.entities.DownloadTEK;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.gaen.Utils;
import com.project.covidguard.tasks.DownloadTEKTask;
import com.project.covidguard.tasks.RequestTANTask;
import com.project.covidguard.tasks.UploadTEKTask;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RegisterUUIDResponse;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;

import org.altbeacon.beacon.Identifier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.WorkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DiagnoseActivity extends AppCompatActivity {

    private static final String LOG_TAG = DiagnoseActivity.class.getName();

    Toolbar mTopToolbar;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.diagnose);
        mTopToolbar = findViewById(R.id.toolbar2);
        mTopToolbar.setTitleTextColor(getColor(R.color.white));
        mTopToolbar.setTitle(getString(R.string.diagnose));
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

    public void clickSubmitHandler(View view) {
        Data data = new Data.Builder()
            .putString("TAN", "1234-5678")
            .build();
        WorkManager wm = WorkManager.getInstance(getApplicationContext());
        wm.beginWith(RequestTANTask.getOneTimeRequest())
            .then(UploadTEKTask.getOneTimeRequestWithoutParams())
            .enqueue();
        Toast.makeText(this, "Submitted", Toast.LENGTH_LONG).show();
    }

    public void clickMatchMaker(View view) {
        WorkManager.getInstance(getApplicationContext()).enqueue(DownloadTEKTask.getOneTimeRequest());

        TEKRepository repo = new TEKRepository(getApplicationContext());
        List<DownloadTEK> teks = repo.getAllDownloadedTEKsSync();
        RPIRepository repoRPI = new RPIRepository(getApplicationContext());
        List<RPI> rpis = repoRPI.getLatestRPIs();
        ArrayList<byte[]> rpiArrayList = new ArrayList<>();
        int result = 1000;

        for (RPI rpi : rpis) {
            byte[] rpiFromRoom = Identifier.parse(rpi.rpi, 16).toByteArray();
            rpiArrayList.add(rpiFromRoom);
        }

        for (DownloadTEK tek : teks) {
            //initialise GAEN variables based on fetched TEK and ENIN


            //initialise GAEN variables based on fetched TEK and ENIN

            byte[] TEKByteArray = Base64.decode(tek.getTek(), Base64.DEFAULT);
            long ENIntervalNumber = tek.getEnIntervalNumber();
            result = Utils.generateAllRPIsForTEKAndEnIntervalNumber(TEKByteArray, ENIntervalNumber, rpiArrayList);
            if (result == 1) {
                Toast.makeText(this, "You have been in contact with a COVID positive case. Seek medical attention immediately!", Toast.LENGTH_LONG).show();
                break;
            }

        }
        Log.d("RESULT FROM UTILS", String.valueOf(result));
        if (result == 0 || result == 1000) {
            Toast.makeText(this, "You are safe!", Toast.LENGTH_LONG).show();
        } else if (result == 999) {
            Toast.makeText(this, "System Error", Toast.LENGTH_LONG).show();

        }

    }

    public void clickDeveloperMetricsHandler(View view) {
        Intent localIntent = new Intent(DiagnoseActivity.this, MetricsActivity.class);
        startActivity(localIntent);
    }

    public void clickTerms(View view) {
        Intent localIntent = new Intent(DiagnoseActivity.this, Terms.class);
        startActivity(localIntent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish() ;// close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
