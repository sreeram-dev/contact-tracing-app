package com.project.covidguard.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.data.entities.DownloadTEK;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.web.responses.DownloadTEKResponse;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.services.DiagnosisServerInterface;
import com.project.covidguard.web.services.ExposureNotificationService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class DownloadTEKTask extends Worker {

    private static final String LOG_TAG = DownloadTEKTask.class.getCanonicalName();
    private final TEKRepository tekRepo = new TEKRepository(getApplicationContext());

    public DownloadTEKTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static OneTimeWorkRequest getOneTimeRequest() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadTEKTask.class).build();
        return request;
    }

    @NonNull
    @Override
    public Result doWork() {
        tekRepo.truncateDownloadTeksSync();

        deleteAndReset();
        List<DownloadTEK> teks = tekRepo.getAllDownloadedTEKsSync();
        Log.d(LOG_TAG, "downloaded teks size: " + teks.size());

        List<Pair<String, Long>> tekPairs = downloadInfectedTEK();

        if (tekPairs == null) {
            return Result.failure();
        }

        if (tekPairs.isEmpty()) {
            return Result.success();
        }

        Log.d(LOG_TAG, "Storing downloaded teks, size: " + tekPairs.size());

        for (Pair<String, Long> pair: tekPairs) {
            tekRepo.storeDownloadedTEKWithEnIntervalNumber(pair.first, pair.second);
        }

        return Result.success();
    }

    private List<Pair<String, Long>> downloadInfectedTEK() {
        DiagnosisServerInterface service = ExposureNotificationService.getService();
        Call<DownloadTEKResponse> call = service.downloadTEKs();

        try {
            Response<DownloadTEKResponse> retrofitResponse = call.execute();
            if (retrofitResponse.isSuccessful()) {
                DownloadTEKResponse response = retrofitResponse.body();
                return response.getTEKsWithENIN();
            } else {
                ErrorResponse response = ErrorResponse.buildFromSource(retrofitResponse.errorBody().source());
                Log.e(LOG_TAG, "Upload Failed err: " + response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Download TEKs request failed");
        }

        return null;
    }

    public void deleteAndReset() {
        SQLiteDatabase database;
        database = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getDatabasePath("covidguard"), null);
        String deleteTable = "DELETE FROM downloaded_teks";
        database.execSQL(deleteTable);
        String deleteSqliteSequence = "DELETE FROM sqlite_sequence WHERE name = 'downloaded_teks'";
        database.execSQL(deleteSqliteSequence);
    }
}
