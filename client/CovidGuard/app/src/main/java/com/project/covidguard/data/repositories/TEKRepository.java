package com.project.covidguard.data.repositories;

import android.content.Context;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.covidguard.AppExecutors;
import com.project.covidguard.data.AppDatabase;
import com.project.covidguard.data.dao.TEKDao;
import com.project.covidguard.data.entities.TEK;

import java.util.ArrayList;
import java.util.Arrays;
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

    private final AppExecutors executors = AppExecutors.getInstance();

    // LiveData is a DataHolder class that allows for notifying database changes.
    private LiveData<List<TEK>> mTeks;

    public TEKRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        Log.d(LOG_TAG, "Is Database open: " + db.isOpen());
        mTekDao = db.tekDao();
    }

    /**
     * Get the liveData of all the TEKs
     * @return
     */
    public LiveData<List<TEK>> getAllTEKs() {
        // TODO: Time to use RxJava - https://github.com/ReactiveX/RxJava
        if (mTeks == null) {
            Long timestamp = LocalDateTime.now().minusDays(30).atZone(zoneId).toEpochSecond();
            try {
                Future<LiveData<List<TEK>>> future = executors.diskIO().submit(
                    () -> mTekDao.getTEKFromTimeStamp(timestamp));

                mTeks = future.get();
                Log.d(LOG_TAG, "TEKS in the database: " + String.valueOf(mTeks.getValue()));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Log.e(LOG_TAG, "Could not fetch all teks - Interrupted Exception");
                mTeks = null;
            } catch (ExecutionException ex) {
                ex.printStackTrace();
                Log.e(LOG_TAG, "Could not fetch all teks - Executed Exception");
                mTeks = null;
            }
        }

        return mTeks;
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
     * @param tekArray - Byte Array representing the TEK
     * @param enIntervalNumber - ENInterval Number when the TEK is generated
     * @return
     */
    public void storeTEKWithEnIntervalNumber(byte[] tekArray, Long enIntervalNumber) {
        String tekString = Arrays.toString(tekArray);
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
}
