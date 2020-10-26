package com.project.covidguard.tasks;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.StorageUtils;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.responses.lis.PatientStatusResponse;
import com.project.covidguard.web.services.LISServerInterface;
import com.project.covidguard.web.services.LISService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class CheckPatientStatus extends Worker {

    private static final String LOG_TAG = CheckPatientStatus.class.getCanonicalName();

    public CheckPatientStatus(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!StorageUtils.isPatientRegistered(getApplicationContext())) {
            Log.d(LOG_TAG, "Patient has not been registered");
            return Result.failure();
        }

        String uuid = StorageUtils.getUUIDFromSharedPreferences(getApplicationContext());
        LISServerInterface service = LISService.getService();
        Call<PatientStatusResponse> call = service.getPatientStatus(uuid);
        try {
            Response<PatientStatusResponse> retrofitResponse = call.execute();
            if (retrofitResponse.isSuccessful()) {
                PatientStatusResponse res = retrofitResponse.body();
                Data data = new Data.Builder()
                    .putBoolean("isPositive", res.isPositive())
                    .putBoolean("isRecovered", res.isRecovered())
                    .putString("uuid", uuid)
                    .build();
                return Result.success(data);
            } else {
                ErrorResponse err = ErrorResponse.buildFromSource(retrofitResponse.errorBody().source());
                Log.d(LOG_TAG, "Error Response: " + err.toString());
                return Result.failure();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
