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
    public static final String TAG = "DownloadTEKTask";
    private final TEKRepository tekRepo = new TEKRepository(getApplicationContext());

    private final Integer MAX_ATTEMPTS = 5;

    public DownloadTEKTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (this.getRunAttemptCount() > MAX_ATTEMPTS) {
            Log.e(LOG_TAG, "Failed to download TEKs");
            return Result.failure();
        }

        tekRepo.truncateDownloadTeksSync();

        deleteAndReset();
        List<DownloadTEK> teks = tekRepo.getAllDownloadedTEKsSync();
        Log.d(LOG_TAG, "downloaded teks size: " + teks.size());

        List<Pair<String, Long>> tekPairs;

        try {
            tekPairs = downloadInfectedTEK();
        } catch (IOException ex) {
            return Result.retry();
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

    private List<Pair<String, Long>> downloadInfectedTEK() throws IOException {
        DiagnosisServerInterface service = ExposureNotificationService.getService();
        Call<DownloadTEKResponse> call = service.downloadTEKs();

        Response<DownloadTEKResponse> retrofitResponse = call.execute();
        if (retrofitResponse.isSuccessful()) {
            DownloadTEKResponse response = retrofitResponse.body();
            return response.getTEKsWithENIN();
        } else {
            ErrorResponse response = ErrorResponse.buildFromSource(retrofitResponse.errorBody().source());
            Log.e(LOG_TAG, "Upload Failed err: " + response.toString());
            throw new IOException("Upload Failed err: " + response.toString());
        }
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
