package com.project.covidguard.data.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.covidguard.data.entities.TEK;

import java.util.List;

@Dao
public interface  TEKRepository {

    @Insert
    void insertTEK(TEK tek);

    @Query("SELECT * FROM teks where created_at >= :timestamp")
    List<TEK> getTEKFromTimeStamp(Integer timestamp);
}
