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

    /**
     * Created at
     */
    @ColumnInfo(name = "created_at")
    private Integer createdAt;

    public TEK(String tekId, String enIntervalNumber, Integer createdAt) {
        this.tekId = tekId;
        this.enIntervalNumber = enIntervalNumber;
        this.createdAt = createdAt;
    }
}
