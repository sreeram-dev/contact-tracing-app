package com.project.covidguard.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.web.requests.UploadTEKRequest;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.UploadDiagnosisKeyResponse;
import com.project.covidguard.web.services.DiagnosisServerInterface;
import com.project.covidguard.web.services.ExposureNotificationService;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class UploadTEKTask extends Worker {

    private static final String LOG_TAG = UploadTEKTask.class.getCanonicalName();
    private final ZoneId zoneId = ZoneId.systemDefault();


    public UploadTEKTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static WorkRequest getOneTimeRequest(Data data) {
        WorkRequest request = new OneTimeWorkRequest.Builder(UploadTEKTask.class)
            .setInputData(data)
            .build();
        return request;
    }

    public static OneTimeWorkRequest getOneTimeRequestWithoutParams() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadTEKTask.class).build();
        return request;
    }

    @NonNull
    @Override
    public Result doWork() {

        String TAN = getInputData().getString("TAN");
        if (TAN == null || TAN.equals("") || TAN.isEmpty()) {
            Log.d(LOG_TAG, "TAN is not passed to the input array, cannot work with this");
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

        if (TAN == null || TAN.equals("") || TAN.length() == 0) {
            Log.e(LOG_TAG, "Failed to fetch TAN");
            return Result.failure();
        }

        uploadDiagnosisKeys(TAN, TEKs, present);

        return Result.success();
    }

    private void uploadDiagnosisKeys(String tan, List<TEK> teks, Long present) {
        DiagnosisServerInterface service = ExposureNotificationService.getService();
        UploadTEKRequest request = UploadTEKRequest.buildRequest(tan, teks);
        Call<UploadDiagnosisKeyResponse> call = service.uploadTEKs(request);
        try {
            Response<UploadDiagnosisKeyResponse> retrofitResponse = call.execute();
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
}
