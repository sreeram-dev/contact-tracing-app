package com.project.covidguard.data.entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity (tableName = "rpis", indices = {@Index(value = {"rpi"}, unique = true)})
public class RPI{

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
    private Integer received_at;


    public RPI(String rpi, String encryptedAEM, Integer epoch) {
        this.rpi = rpi;
        this.aem = encryptedAEM;
        this.received_at = epoch;
    }
}
