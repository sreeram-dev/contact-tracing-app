package com.project.covidguard.data.entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity (tableName = "rpis", indices = {@Index(value = {"rpi"}, unique = true)})
public class RPI {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * RPI received from the user
     */
    @ColumnInfo(name = "rpi")
    private String rpi;

    /**
     * Store Associated Encrypted Metadata
     */
    @ColumnInfo(name = "aem")
    private String aem;

    /**
     * Store the running average RSSI of the beacon
     */
    @ColumnInfo(name ="rssi")
    private Double rssi;

    /**
     * Store the TxPower of the beacon
     */
    @ColumnInfo(name = "tx_power")
    private Integer txPower;

    /**
     * Store the distance calculated by the beacon
     */
    @ColumnInfo(name = "distance")
    private Double distance;

    /**
     * Store only the second precision value since epochs passed
     */
    @ColumnInfo(name = "received_at")
    private Long receivedAt;

    @Ignore
    public RPI(String rpi, String encryptedAEM, Long epoch) {
        this.rpi = rpi;
        this.aem = encryptedAEM;
        this.receivedAt = epoch;
    }

    public RPI(int id, String rpi, String aem, Double rssi, Integer txPower, Double distance, Long receivedAt) {
        this.id = id;
        this.rpi = rpi;
        this.aem = aem;
        this.rssi = rssi;
        this.txPower = txPower;
        this.distance = distance;
        this.receivedAt = receivedAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public String getRpi() {
        return this.rpi;
    }

    public void setRpi(String rpi) {
        this.rpi = rpi;
    }

    public void setAem(String aem) {
        this.aem = aem;
    }

    public String getAem() {
        return this.aem;
    }

    private void setReceivedAt(Long receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Long getReceivedAt() {
        return this.receivedAt;
    }

    public void setTxPower(Integer txPower) {
        this.txPower = txPower;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setRssi(Double rssi) {
        this.rssi = rssi;
    }

    public Integer getTxPower() { return this.txPower; }

    public Double getDistance() { return this.distance; }

    public Double getRssi() { return this.rssi; }
}
