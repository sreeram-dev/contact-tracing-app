package com.project.covidguard;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ref: https://jswizzy.gitbooks.io/modern-android/content/app-architecture/appexecutors.html
 *
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    // For Singleton instantiation
    private static final String LOG_TAG = AppExecutors.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static AppExecutors sInstance;
    private final ExecutorService diskIO;
    private final Executor mainThread;
    private final ExecutorService networkIO;

    private AppExecutors(ExecutorService diskIO, ExecutorService networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating the disk, network and main threads");
                sInstance = new AppExecutors(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        new MainThreadExecutor());
            }
        }
        return sInstance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public ExecutorService networkIO() {
        return networkIO;
    }

    public void shutdownServices() {
        Log.d(LOG_TAG, "Shutting down executors - network, disk");
        diskIO.shutdown();
        networkIO.shutdown();
    }

    /**
     * TODO: Make it into a callable interface
     * or Submit callback in the runnable class
     */
    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);

        }
    }
}
