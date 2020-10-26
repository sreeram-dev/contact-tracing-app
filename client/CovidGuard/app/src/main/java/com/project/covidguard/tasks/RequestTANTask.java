package com.project.covidguard.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.R;
import com.project.covidguard.StorageUtils;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.verification.RequestTANResponse;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class RequestTANTask extends Worker {

    private static final String LOG_TAG = RequestTANTask.class.getCanonicalName();
    public static final String TAG = "RequestTANTask";
    private final Integer MAX_ATTEMPTS = 5;

    public RequestTANTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    private String getTokenFromSharedPreferences() {
        try {
            SharedPreferences pref = StorageUtils.getEncryptedSharedPref(
                    getApplicationContext(),
                    getApplicationContext().getString(R.string.preference_file_key));
            return pref.getString("token", "");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Cannot get shared preferences from the storage");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Cannot get sharedPreferences from the storage");
            return null;
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        String TAN = "";

        if (this.getRunAttemptCount() > MAX_ATTEMPTS) {
            Log.e(LOG_TAG, "Failed to fetch TAN");
            Data data = new Data.Builder()
                .putString("TAN", null)
                .build();
            return Result.failure(data);
        }

        if (!StorageUtils.isTokenPresent(getApplicationContext(), LOG_TAG)) {
            Log.e(LOG_TAG, "App has not been registered, Cannot submit teks");
            Data data = new Data.Builder()
                .putString("TAN", null)
                .build();
            return Result.failure(data);
        }

        String token = getTokenFromSharedPreferences();

        if (token.equals("") || token.length() == 0 || token == null) {
            Log.e(LOG_TAG, "Token is empty, may not be stored");
            Data data = new Data.Builder()
                .putString("TAN", null)
                .build();
            return Result.failure(data);
        }

        try {
            TAN = requestTANFromServer(token);
        } catch (IOException ex) {
            return Result.retry();
        }

        if (TAN.equals("") || TAN.length() == 0) {
            Log.e(LOG_TAG, "Failed to fetch TAN");
            Data data = new Data.Builder()
                .putString("TAN", null)
                .build();
            return Result.failure(data);
        }

        Data data = new Data.Builder()
            .putString("TAN", TAN)
            .build();
        return Result.success(data);
    }

    private String requestTANFromServer(String token) throws IOException {
        VerificationEndpointInterface service = VerificationService.getService();
        Call<RequestTANResponse> call = service.requestTAN(token);
        Response<RequestTANResponse> retrofitResponse = call.execute();
        if (retrofitResponse.isSuccessful()) {
            RequestTANResponse response = retrofitResponse.body();
            Log.d(LOG_TAG, "Response Successful: " + response.toString());
            return response.getTAN();
        } else {
            ErrorResponse err = ErrorResponse.buildFromSource(retrofitResponse.errorBody().source());
            Log.e(LOG_TAG, "Request failed at server: " + err.toString());
            throw new IOException("Request failed: " + err.toString());
        }
    }
}
