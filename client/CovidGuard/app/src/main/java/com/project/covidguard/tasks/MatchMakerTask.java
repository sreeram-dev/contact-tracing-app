package com.project.covidguard.tasks;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.gaen.Utils;
import com.project.covidguard.web.responses.DownloadTEKResponse;
import com.project.covidguard.web.responses.ErrorResponse;
import com.project.covidguard.web.services.DiagnosisServerInterface;
import com.project.covidguard.web.services.ExposureNotificationService;

import org.altbeacon.beacon.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MatchMakerTask extends Worker {

    private static final String LOG_TAG = MatchMakerTask.class.getCanonicalName();
    public static final String TAG = "MatchMakerTask";
    private final Integer MAX_ATTEMPTS = 5;


    public MatchMakerTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (this.getRunAttemptCount() > MAX_ATTEMPTS) {
            return Result.failure();
        }

        List<Pair<String, Long>> teks;

        try {
            Log.d(LOG_TAG, "Downloading teks from Server");
            teks = downloadInfectedTEK();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Downloading of teks failed");
            return Result.retry();
        }

        RPIRepository repoRPI = new RPIRepository(getApplicationContext());
        List<RPI> rpis = repoRPI.getLatestRPIs();

        int result = 1000;

        ArrayList<byte[]> rpiArrayList = new ArrayList<>();
        for (RPI rpi : rpis) {
            byte[] rpiFromRoom = Identifier.parse(rpi.getRpi(), 16).toByteArray();
            rpiArrayList.add(rpiFromRoom);
        }

        for (Pair<String, Long> tekPair : teks) {
            //initialise GAEN variables based on fetched TEK and ENIN

            byte[] TEKByteArray = Base64.decode(tekPair.first, Base64.DEFAULT);
            long ENIntervalNumber = tekPair.second;

            result = Utils.generateAllRPIsForTEKAndEnIntervalNumber(TEKByteArray, ENIntervalNumber, rpiArrayList);
            if (result == 1) {
                Data data = new Data.Builder()
                    .putBoolean("is_positive", true)
                    .putString("message", "You have been in contact with a COVID positive case. Seek medical attention immediately!")
                    .build();
                Log.d(LOG_TAG, "data: " + data.toString());
                return Result.success(data);
            }
        }

        Log.d("RESULT FROM UTILS", String.valueOf(result));
        if (result == 0 || result == 1000) {
            Data data = new Data.Builder()
                .putBoolean("is_positive", false)
                .putString("message", "You are safe!")
                .build();
            return Result.success(data);
        } else if (result == 999) {
            Data data = new Data.Builder()
                .putBoolean("is_positive", false)
                .putString("message", "System Error")
                .build();
            return Result.failure(data);
        }

        return Result.failure();
    }

    /**
     * Downloads the infected teks
     * @return
     * @throws IOException
     */
    public List<Pair<String, Long>> downloadInfectedTEK() throws IOException {
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
}
