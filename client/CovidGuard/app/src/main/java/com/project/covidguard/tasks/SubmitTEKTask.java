package com.project.covidguard.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.R;
import com.project.covidguard.StorageUtils;
import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.web.dto.UploadTEKRequest;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.RequestTANResponse;
import com.project.covidguard.web.responses.UploadDiagnosisKeyResponse;
import com.project.covidguard.web.services.DiagnosisServerInterface;
import com.project.covidguard.web.services.ExposureNotificationService;
import com.project.covidguard.web.services.VerificationEndpointInterface;
import com.project.covidguard.web.services.VerificationService;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class SubmitTEKTask extends Worker {

    private static final String LOG_TAG = SubmitTEKTask.class.getCanonicalName();
    private final ZoneId zoneId = ZoneId.systemDefault();

    public SubmitTEKTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    /**
     * Container to create the workRequest to run the tasky
     * This might be an anti-pattern, however, it gives information in one file
     * about how we should go about running the task by default
     * @return
     */
    public static WorkRequest getAssociatedWorkRequest() {
        WorkRequest  request = new PeriodicWorkRequest.Builder(SubmitTEKTask.class, 3, TimeUnit.MINUTES)
                .build();
        return request;
     }

     public static WorkRequest getOneTimeRequest() {
        WorkRequest request = new OneTimeWorkRequest.Builder(SubmitTEKTask.class).build();
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
            return Result.failure();
        }

        Long lastUploadTimeStamp = getLastUploadTimeStamp();
        Long present = LocalDateTime.now().atZone(zoneId).toEpochSecond();
        List<TEK> TEKs = getTEKToUpload(lastUploadTimeStamp, present);

        if (TEKs == null || TEKs.size() == 0) {
            Log.d(LOG_TAG, "No new teks to upload from timestamp: " + lastUploadTimeStamp);
            return Result.success();
        } else if (TEKs.size() > 0) {
            Log.d(LOG_TAG, "Initiating upload of teks, size: " + TEKs.size());
        }

        String token = getTokenFromSharedPreferences();

        if (token.equals("") || token.length() == 0 || token == null) {
            Log.e(LOG_TAG, "Token is empty, may not be stored");
            return Result.failure();
        }

        String TAN = requestTANFromServer(token);

        if (TAN == null || TAN.equals("") || TAN.length() == 0) {
            Log.e(LOG_TAG, "Failed to fetch TAN");
            return Result.failure();
        }

        uploadDiagnosisKeys(TAN, TEKs, present);

        return Result.success();
    }

    private Long getLastUploadTimeStamp() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                "user_details", Context.MODE_PRIVATE);

        Long lastUploadTimeStamp = sharedPreferences.getLong("last_uploaded_timestamp", 0);
        if (lastUploadTimeStamp == 0 || lastUploadTimeStamp == null) {
            Log.d(LOG_TAG, "lastUploadedTimestamp is null, Doing upload first time");
            lastUploadTimeStamp = LocalDateTime.now().minusDays(14).atZone(zoneId).toEpochSecond();
        }

        return lastUploadTimeStamp;
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

    private void storeLastUploadTimeStamp(Long lastUploadTimeStamp) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                "user_details", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("last_uploaded_timestamp", lastUploadTimeStamp);
        editor.commit();
    }

    private List<TEK> getTEKToUpload(Long from, Long to) {
        List<TEK> teks = new TEKRepository(getApplicationContext()).getAllTEKSync(from, to);
        return teks;
    }

    private void uploadDiagnosisKeys(String tan, List<TEK> teks, Long present) {
        DiagnosisServerInterface service = ExposureNotificationService.getService();
        UploadTEKRequest request = UploadTEKRequest.buildRequest(tan, teks);
        Call<UploadDiagnosisKeyResponse> call = service.uploadTEKs(request);
        try {
            Response<UploadDiagnosisKeyResponse>  retrofitResponse = call.execute();
            if (retrofitResponse.isSuccessful()) {
                UploadDiagnosisKeyResponse response = retrofitResponse.body();
                Log.d(LOG_TAG, "Upload successful msg: " + response.toString());
                storeLastUploadTimeStamp(present);
            } else {
                ErrorResponse response = ErrorResponse.buildFromSource(retrofitResponse.errorBody().source());
                Log.e(LOG_TAG, "Upload Failed err: " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Response failed");
        }
    }
}
