package com.project.covidguard.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "teks", indices = {@Index(value = {"tek_id"}, unique = true)})
public class TEK {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "tek_id")
    private String tekId;

    @ColumnInfo(name = "en_interval_number")
    private Long enIntervalNumber;

    /**
     * When was the tek created?
     */
    @ColumnInfo(name = "created_at")
    private Long createdAt;

    @Ignore
    public TEK(String tekId, Long enIntervalNumber, Long createdAt) {
        this.tekId = tekId;
        this.enIntervalNumber = enIntervalNumber;
        this.createdAt = createdAt;
    }

    public TEK(int id, String tekId, Long enIntervalNumber, Long createdAt) {
        this.tekId = tekId;
        this.enIntervalNumber = enIntervalNumber;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTekId() {
        return this.tekId;
    }

    public void setTekId(String tekID) {
        this.tekId = tekId;
    }

    public Long getEnIntervalNumber() {
        return enIntervalNumber;
    }

    public void setEnIntervalNumber(Long enIntervalNumber) {
        this.enIntervalNumber = enIntervalNumber;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getTekId() +" ");
        sb.append(this.getEnIntervalNumber());
        return sb.toString();
    }
}
