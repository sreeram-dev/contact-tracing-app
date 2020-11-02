
package com.project.covidguard.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.project.covidguard.ExposureKeyService;
import com.project.covidguard.R;
import com.project.covidguard.StorageUtils;
import com.project.covidguard.tasks.CheckPatientStatus;
import com.project.covidguard.tasks.MatchMakerTask;
import com.project.covidguard.tasks.RequestTANTask;
import com.project.covidguard.tasks.UploadTEKTask;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.lis.PatientStatusResponse;
import com.project.covidguard.web.responses.lis.RegisterPatientResponse;
import com.project.covidguard.web.responses.verification.RegisterUUIDResponse;
import com.project.covidguard.web.services.LISServerInterface;
import com.project.covidguard.web.services.LISService;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.project.covidguard.App.CHANNEL_ID;

public class DiagnoseActivity extends AppCompatActivity {

    private static final String LOG_TAG = DiagnoseActivity.class.getName();
    public static final Integer POSITIVE_CONTACT_NOTIFICATION_ID = 2;
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
            registerWithLISServer(uuid);
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
            Log.d(LOG_TAG, "App has been registered with uuid");
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

    public void clickCheckInfected(View view) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CheckPatientStatus.class)
            .addTag(CheckPatientStatus.TAG)
            .build();

        WorkManager wm = WorkManager.getInstance(getApplicationContext());
        wm.enqueue(request);

        wm.getWorkInfoByIdLiveData(request.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    Data outputData = workInfo.getOutputData();
                    Boolean isPositive = outputData.getBoolean("isPositive", false);
                    Boolean isRecovered = outputData.getBoolean("isRecovered", false);
                    if (isPositive && !isRecovered) {
                        Log.d(LOG_TAG, "You are covid-19 positive");
                        Toast.makeText(getApplicationContext(), "You are covid-19 positive",
                            Toast.LENGTH_LONG).show();
                    }  else if (isRecovered) {
                        Log.d(LOG_TAG, "You have recovered.");
                        Toast.makeText(getApplicationContext(), "You have recovered.",
                            Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(LOG_TAG, "ou are not covid-19 positive.");
                        Toast.makeText(getApplicationContext(), "You are not covid-19 positive.",
                            Toast.LENGTH_LONG).show();
                    }
                } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                    Data outputData = workInfo.getOutputData();
                    if (outputData != null) {
                        String message = outputData.getString("message");

                        Log.d(LOG_TAG, "Patient Status Check Failed: " + message);
                        Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void clickSubmitHandler(View view) {
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(),  "Network is not available!", Toast.LENGTH_LONG).show();
            return;
        }

        OneTimeWorkRequest tanRequest = new OneTimeWorkRequest.Builder(RequestTANTask.class)
            .addTag(RequestTANTask.TAG)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build();

        OneTimeWorkRequest uploadTEKRequest = new OneTimeWorkRequest.Builder(UploadTEKTask.class)
            .addTag(UploadTEKTask.TAG)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build();

        WorkManager wm = WorkManager.getInstance(getApplicationContext());
        wm.beginWith(tanRequest)
            .then(uploadTEKRequest)
            .enqueue();

        wm.getWorkInfoByIdLiveData(uploadTEKRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    Toast.makeText(getApplicationContext(), "Submitted TEK Successfully", Toast.LENGTH_LONG).show();
                }
            }
        });

        wm.getWorkInfoByIdLiveData(tanRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo.getState() == WorkInfo.State.FAILED) {
                    Data data = workInfo.getOutputData();
                    String message = data.getString("message");
                    Log.d(LOG_TAG, "Tan request failed: " + message);
                    if (data != null) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void clickMatchMaker(View view) {

        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(),  "Network is not available!", Toast.LENGTH_LONG).show();
            return;
        }

        WorkManager wm = WorkManager.getInstance(getApplicationContext());

        OneTimeWorkRequest matchRequest = new OneTimeWorkRequest.Builder(MatchMakerTask.class)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 2, TimeUnit.MINUTES)
            .build();

        wm.enqueue(matchRequest);

        wm.getWorkInfoByIdLiveData(matchRequest.getId())
            .observe(this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Data data = workInfo.getOutputData();
                        String msg = data.getString("message");
                        Boolean isPositive = data.getBoolean("is_positive", false);
                        if (!isPositive) {
                            // if there is a positive contact notification, cancel it if launched by exposure key service
                            NotificationManagerCompat manager =  NotificationManagerCompat.from(getApplicationContext());
                            manager.cancel(DiagnoseActivity.POSITIVE_CONTACT_NOTIFICATION_ID);
                        } else {
                            raisePositiveNotification();
                        }
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        AlertDialog.Builder alert = new AlertDialog.Builder(DiagnoseActivity.this);
                        alert.setTitle("Alert");
                        alert.setMessage(msg);
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface alert, int which) {
                                // TODO Auto-generated method stub
                                //Do something
                                alert.dismiss();
                            }
                        });
                        alert.show();
                    }
                }
            });
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish() ;// close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
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

    private void raisePositiveNotification() {
        Intent intent = new Intent(getApplicationContext(), DiagnoseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        String title = "Possible COVID-19 Contact";
        String message = "You have been contact with a COVID-19 Patient, Please get yourself checked";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setLights(0xff0000ff, 2000, 500) // Blue color light flash for 2s on and 0.5 off
            .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(DiagnoseActivity.POSITIVE_CONTACT_NOTIFICATION_ID, builder.build());
    }
}
