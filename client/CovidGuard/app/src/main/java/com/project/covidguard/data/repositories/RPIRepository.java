package com.project.covidguard.data.repositories;

import android.content.Context;
import android.util.Log;

import com.project.covidguard.AppExecutors;
import com.project.covidguard.data.AppDatabase;
import com.project.covidguard.data.dao.RPIDao;
import com.project.covidguard.data.entities.RPI;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RPIRepository {

    private static final String LOG_TAG = RPIRepository.class.getCanonicalName();
    private final ZoneId zoneId = ZoneId.systemDefault();

    private RPIDao mRPIDao;

    private final AppExecutors executors = AppExecutors.getInstance();

    public RPIRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        mRPIDao = db.rpiDao();
    }

    /**
     * Get the latest RPI by
     * @return
     */
    public List<RPI> getLatestRPIs() {
        List<RPI> rpis;

        try {
            Future<List<RPI>> future = executors.diskIO().submit(() -> {
                Long timestamp = LocalDateTime.now().minusDays(15).atZone(zoneId).toEpochSecond();
                return mRPIDao.getRPIFromTimestamp(timestamp );
            });
            rpis = future.get();
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            Log.d(LOG_TAG, "Execution Exception: Latest RPIs get Failed");
            rpis = null;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Log.d(LOG_TAG, "Interrupted Exception: Latest RPIs get failed");
            rpis = null;
        }

        return rpis;
    }




    public RPI getLastRPI() {
        Future<RPI> future = executors.diskIO().submit(() -> mRPIDao.getLastRPI());

        try {
            RPI lastRPI = future.get();
            return lastRPI;
        } catch(InterruptedException ex) {
            ex.printStackTrace();
            Log.e(LOG_TAG, "Last Tek Fetch Failed - Interrupted Exception");
        } catch(ExecutionException ex) {
            ex.printStackTrace();
            Log.e(LOG_TAG, "Last Tek Fetch Failed - Execution Exception");
        }

        return null;
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
