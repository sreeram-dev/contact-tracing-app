package com.project.covidguard.data.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.covidguard.data.entities.RPI;

import java.util.List;

@Dao
public interface RPIRepository {

    @Insert
    void saveRPI(RPI rpi);

    @Query("SELECT * FROM rpis where received_at >= :timestamp")
    List<RPI> getRPIFromTimestamp(Integer timestamp);

}

