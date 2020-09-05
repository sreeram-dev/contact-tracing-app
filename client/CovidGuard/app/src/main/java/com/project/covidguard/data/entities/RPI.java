package com.project.covidguard.data.entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity (tableName = "rpis", indices = {@Index(value = {"rpi"}, unique = true)})
public class RPI {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * RPI received from the user
     */
    @ColumnInfo(name = "rpi")
    public String rpi;

    /**
     * Store Associated Encrypted Metadata
     */
    @ColumnInfo(name = "aem")
    public String aem;

    /**
     * Store only the second precision value since epochs passed
     */
    @ColumnInfo(name = "received_at")
    private Integer receivedAt;

    @Ignore
    public RPI(String rpi, String encryptedAEM, Integer epoch) {
        this.rpi = rpi;
        this.aem = encryptedAEM;
        this.receivedAt = epoch;
    }

    public RPI(int id, String rpi, String aem, Integer receivedAt) {
        this.id = id;
        this.rpi = rpi;
        this.aem = aem;
        this.receivedAt = receivedAt;
    }

    public String getRPI() {
        return this.rpi;
    }

    public void setRPI(String rpi) {
        this.rpi = rpi;
    }

    public void setAem(String aem) {
        this.aem = aem;
    }

    public String getAem() {
        return this.aem;
    }

    public Integer getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(Integer receivedAt) {
        this.receivedAt = receivedAt;
    }
}
