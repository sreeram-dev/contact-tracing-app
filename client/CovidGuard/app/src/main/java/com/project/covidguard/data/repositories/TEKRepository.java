package com.project.covidguard.data.repositories;

import android.annotation.SuppressLint;
import android.content.Context;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.project.covidguard.AppExecutors;
import com.project.covidguard.data.AppDatabase;
import com.project.covidguard.data.dao.DownloadTEKDao;
import com.project.covidguard.data.dao.TEKDao;
import com.project.covidguard.data.entities.DownloadTEK;
import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.web.responses.DownloadTEKResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

/**
 * We create a repository for accessing multiple dao and act as a business layer over the dao layer.
 * TEK Repository is the business layer calls that interact with SQLite database through room ORM layer.
 */
public class TEKRepository {

    private static final String LOG_TAG = TEKRepository.class.getCanonicalName();
    private final ZoneId zoneId = ZoneId.systemDefault();

    private TEKDao mTekDao;
    private DownloadTEKDao mDownloadedTEKDao;

    private final AppExecutors executors = AppExecutors.getInstance();

    // LiveData is a DataHolder class that allows for notifying database changes.
    private LiveData<List<TEK>> mTeks;
    private LiveData<List<DownloadTEK>> mDownloadedTeks;

    public TEKRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        Log.d(LOG_TAG, "Is Database open: " + db.isOpen());
        mTekDao = db.tekDao();
        mDownloadedTEKDao = db.downloadTEKDao();
    }


    /**
     * Get the liveData of all the TEKs
     * @return
     */
    public LiveData<List<TEK>> getAllTEKs() {
        if (mTeks == null) {
            Long timestamp = LocalDateTime.now().minusDays(30).atZone(zoneId).toEpochSecond();
            mTeks = mTekDao.getTEKFromTimeStamp(timestamp);
        }

        return mTeks;
    }

    /**
     * To be used in background threads
     * @return
     */
    public List<TEK> getAllTEKSync(Long from, Long to) {
        return mTekDao.getTEKFromTimeStampSync(from, to);
    }

    /**
     * Get the last tek stored in the database
     * @return
     */
    public TEK getLastTek() {
        Future<TEK> future = executors.diskIO().submit(() -> mTekDao.getLastTEK());

        try {
            TEK lastTek = future.get();
            return lastTek;
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
     * Stores the TEK and ENIntervalNumber
     * @param tekString - Encoded String representing the TEK
     * @param enIntervalNumber - ENInterval Number when the TEK is generated
     * @return
     */
    public void storeTEKWithEnIntervalNumber(String tekString, Long enIntervalNumber) {
        Long createdAt = LocalDateTime.now().atZone(zoneId).toEpochSecond();
        TEK tek = new TEK(tekString, enIntervalNumber, createdAt);
        executors.diskIO().submit(new Runnable() {
            @Override
            public void run() {
                mTekDao.insert(tek);
            }
        });
    }

    /**
     * Asynchronous process that deletes the stale TEKs
     * Stale TEKs are the TEKs that are older than 30 days.
     */
    public void deleteStaleTEKs()  {
        Long timestamp = LocalDateTime.now().minusDays(30).atZone(zoneId).toEpochSecond();
        executors.diskIO().submit(() -> mTekDao.deleteBeforeTimeStamp(timestamp));
    }

    /**
     * Check if TEK exists for the interval
     * @param enIntervalNumber
     */
    public Boolean tekExistsForInterval(Long enIntervalNumber) {
        Future<TEK> future = executors.diskIO().submit(
                () -> mTekDao.fetchByENInterval(enIntervalNumber));

        try {
            TEK tek = future.get();
            if (tek == null) {
                return false;
            }

            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public TEK getTekWithInterval(Long enIntervalNumber) {
        Future<TEK> future = executors.diskIO().submit(
                () -> mTekDao.fetchByENInterval(enIntervalNumber));

        try {
            TEK tek = future.get();
            return tek;
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * get all downloaded teks
     */
    public LiveData<List<DownloadTEK>> getDataFromTimestamp() {
        if (mDownloadedTeks == null) {
            mDownloadedTeks = mDownloadedTEKDao.getAllDownloadedTEKS();
        }

        return mDownloadedTeks;
    }

    /**
     *
     * @param tekString
     * @param enIntervalNumber
     */
    public void storeDownloadedTEKWithEnIntervalNumber(String tekString, Long enIntervalNumber) {
        DownloadTEK tek = new DownloadTEK(tekString, enIntervalNumber);
        executors.diskIO().submit(new Runnable() {
            @Override
            public void run() {
                mDownloadedTEKDao.insert(tek);
            }
        });
    }
}
