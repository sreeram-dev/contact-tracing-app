package com.project.covidguard.data.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import androidx.room.Update;

import com.project.covidguard.data.entities.DownloadTEK;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.entities.TEK;

import java.util.List;

@Dao
public interface  DownloadTEKDao {

    @Insert
    void insert(DownloadTEK tek);

    @Update
    void update(DownloadTEK tek);

    /**
     * Fetch the TEK corresponding to the ENIntervalNumber
     * @param ENIntervalNumber
     * @return
     */
    @Query("SELECT * FROM downloaded_teks where en_interval_number = :ENIntervalNumber")
    DownloadTEK fetchByENInterval(Long ENIntervalNumber);

    /**
     * Get all downloaded teks
     */
    @Query("SELECT * FROM downloaded_teks;")
    LiveData<List<DownloadTEK>> getAllDownloadedTEKS();

    /**
     * Drop all downloaded teks
     */
    @Query("DELETE FROM downloaded_teks")
    void deleteFromDownloadedTEKs();
}
