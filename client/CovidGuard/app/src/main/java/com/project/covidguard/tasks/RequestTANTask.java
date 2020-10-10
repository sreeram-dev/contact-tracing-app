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
import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.web.requests.UploadTEKRequest;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RequestTANResponse;
import com.project.covidguard.web.responses.UploadDiagnosisKeyResponse;
import com.project.covidguard.web.services.DiagnosisServerInterface;
import com.project.covidguard.web.services.ExposureNotificationService;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class RequestTANTask extends Worker {

    private static final String LOG_TAG = RequestTANTask.class.getCanonicalName();

    public RequestTANTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    /**
     * Container to create the workRequest to run the tasky
     * This might be an anti-pattern, however, it gives information in one file
     * about how we should go about running the task by default
     * @return
     */
    public static WorkRequest getAssociatedWorkRequest() {
        WorkRequest  request = new PeriodicWorkRequest.Builder(RequestTANTask.class, 3, TimeUnit.MINUTES)
                .build();
        return request;
    }

    public static OneTimeWorkRequest getOneTimeRequest() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(RequestTANTask.class).build();
        return request;
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

        String TAN = requestTANFromServer(token);

        if (TAN == null || TAN.equals("") || TAN.length() == 0) {
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

    private String requestTANFromServer(String token) {
        VerificationEndpointInterface service = VerificationService.getService();
        Call<RequestTANResponse> call = service.requestTAN(token);
        try {
            Response<RequestTANResponse> retrofitResponse = call.execute();
            if (retrofitResponse.isSuccessful()) {
                RequestTANResponse response = retrofitResponse.body();
                Log.d(LOG_TAG, "Response Successful: " + response.toString());
                return response.getTAN();
            } else {
                ErrorResponse err = ErrorResponse.buildFromSource(retrofitResponse.errorBody().source());
                Log.e(LOG_TAG, "Request failed at server: " + err.toString());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Request to fetch TAN failed");
            return null;
        }
    }
}
