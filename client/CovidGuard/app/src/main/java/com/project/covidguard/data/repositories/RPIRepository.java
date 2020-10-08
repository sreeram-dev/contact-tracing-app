package com.project.covidguard.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.project.covidguard.AppExecutors;
import com.project.covidguard.data.AppDatabase;
import com.project.covidguard.data.dao.RPIDao;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.entities.TEK;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RPIRepository {

    private static final String LOG_TAG = RPIRepository.class.getCanonicalName();
    private final ZoneId zoneId = ZoneId.systemDefault();

    private RPIDao mRPIDao;

    private final AppExecutors executors = AppExecutors.getInstance();

    // LiveData is a DataHolder class that allows for notifying database changes.
    private LiveData<List<RPI>> mLatestRPIs;

    public RPIRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        mRPIDao = db.rpiDao();
    }

    /**
     * Get the latest RPI by
     * @param limit - the number of latest rpi
     * @return
     */
    public LiveData<List<RPI>> getLatestRPIs(Integer limit) {
        if (mLatestRPIs == null) {
            try {
                Future<LiveData<List<RPI>>> future = executors.diskIO().submit(() -> {
                    Long timestamp = LocalDateTime.now().minusDays(15).atZone(zoneId).toEpochSecond();
                    return mRPIDao.getRPIFromTimestamp(timestamp, limit);
                });
                mLatestRPIs = future.get();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
                Log.d(LOG_TAG,"Execution Exception: Latest RPIs get Failed");
                mLatestRPIs = null;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Log.d(LOG_TAG, "Interrupted Exception: Latest RPIs get failed");
                mLatestRPIs = null;
            }
        }

        return mLatestRPIs;
    }


    /**
     * Store the RPI and received timestamp
     */
    public void storeReceivedRPI(String rpiString, Long timestamp) {
        RPI rpi = new RPI(rpiString, "",  timestamp);
        executors.diskIO().submit(new Runnable() {
            @Override
            public void run() {
                mRPIDao.insert(rpi);
            }
        });

    }
}
