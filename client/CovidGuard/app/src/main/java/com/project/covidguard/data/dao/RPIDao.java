package com.project.covidguard.data.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.covidguard.data.entities.RPI;

import java.util.List;

@Dao
public interface RPIDao {

    @Insert
    void insert(RPI rpi);

    @Update
    void update(RPI rpi);

    @Query("SELECT * FROM rpis where received_at >= :timestamp")
    List<RPI> getRPIFromTimestamp(Long timestamp);

    @Query("SELECT * FROM rpis ORDER BY received_at DESC LIMIT 1")
    RPI getLastRPI();

    @Query("SELECT * FROM rpis where rpi=:rpiString")
    RPI getRPIByID(String rpiString);
}



