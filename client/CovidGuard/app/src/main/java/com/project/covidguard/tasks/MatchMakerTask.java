package com.project.covidguard.tasks;

import android.content.Context;
import android.service.autofill.FieldClassification;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.project.covidguard.data.entities.DownloadTEK;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.data.repositories.TEKRepository;
import com.project.covidguard.gaen.Utils;

import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MatchMakerTask extends Worker {


    private static final String LOG_TAG = MatchMakerTask.class.getCanonicalName();
    private final TEKRepository tekRepo = new TEKRepository(getApplicationContext());

    public MatchMakerTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static OneTimeWorkRequest getOneTimeRequest() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MatchMakerTask.class).build();
        return request;
    }

    @NonNull
    @Override
    public Result doWork() {
        TEKRepository repo = new TEKRepository(getApplicationContext());
        List<DownloadTEK> teks = repo.getAllDownloadedTEKsSync();
        RPIRepository repoRPI = new RPIRepository(getApplicationContext());
        List<RPI> rpis = repoRPI.getLatestRPIs();
        ArrayList<byte[]> rpiArrayList = new ArrayList<>();
        int result = 1000;

        for (RPI rpi : rpis) {
            byte[] rpiFromRoom = Identifier.parse(rpi.getRpi(), 16).toByteArray();
            rpiArrayList.add(rpiFromRoom);
        }

        for (DownloadTEK tek : teks) {
            //initialise GAEN variables based on fetched TEK and ENIN

            byte[] TEKByteArray = Base64.decode(tek.getTek(), Base64.DEFAULT);
            long ENIntervalNumber = tek.getEnIntervalNumber();

            result = Utils.generateAllRPIsForTEKAndEnIntervalNumber(TEKByteArray, ENIntervalNumber, rpiArrayList);
            if (result == 1) {
                Data data = new Data.Builder()
                    .putBoolean("is_positive", true)
                    .putString("message", "You have been in contact with a COVID positive case. Seek medical attention immediately!")
                    .build();

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
}
