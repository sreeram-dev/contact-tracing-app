package com.project.covidguard.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "downloaded_teks", indices = {@Index(value = {"tek"}, unique = true)})
public class DownloadTEK {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "tek")
    private String tek;

    @ColumnInfo(name = "en_interval_number")
    private Long enIntervalNumber;

    @Ignore
    public DownloadTEK(String tek, Long enIntervalNumber) {
        this.tek = tek;
        this.enIntervalNumber = enIntervalNumber;
    }

    public DownloadTEK(int id, String tek, Long enIntervalNumber) {
        this.tek = tek;
        this.enIntervalNumber = enIntervalNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTek() {
        return this.tek;
    }

    public void setTekId(String tekID) {
        this.tek = tek;
    }

    public Long getEnIntervalNumber() {
        return enIntervalNumber;
    }

    public void setEnIntervalNumber(Long enIntervalNumber) {
        this.enIntervalNumber = enIntervalNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getTek() + " ");
        sb.append(this.getEnIntervalNumber());
        return sb.toString();
    }
}
