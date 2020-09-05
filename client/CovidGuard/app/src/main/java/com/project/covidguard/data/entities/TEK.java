package com.project.covidguard.data.entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "teks")
public class TEK {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "tek_id")
    private String tekId;

    @ColumnInfo(name = "en_interval_number")
    private String enIntervalNumber;

    public TEK(String tekId, String enIntervalNumber) {
        this.tekId = tekId;
        this.enIntervalNumber = enIntervalNumber;
    }

}
