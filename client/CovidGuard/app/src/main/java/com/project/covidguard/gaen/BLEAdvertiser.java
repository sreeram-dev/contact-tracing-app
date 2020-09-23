package com.project.covidguard.gaen;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;

import java.util.Observable;
import java.util.Observer;

import static com.project.covidguard.gaen.GAENConstants.RPI_INTERVAL;


public class BLEAdvertiser implements Observer {

    private static final String LOG_TAG = BLEAdvertiser.class.getCanonicalName();
    private static final String BEACON_ADVERTISEMENT_TAG = LOG_TAG + ":BEACON_ADVERTISEMENT";

    // Use a static variable for single initialization of beaconTransmitter across all objects of BLEAdvertiser
    private static BeaconTransmitter beaconTransmitter = null;
    private static final BeaconParser BEACON_PARSER = new BeaconParser()
            .setBeaconLayout(GAENConstants.BEACON_LAYOUT);

    Integer advertisingInterval;
    Context context;

    public BLEAdvertiser(Context context, Integer advertisingInterval) {
        if (advertisingInterval >= RPI_INTERVAL) {
            Log.e(LOG_TAG, "Advertising Interval: " + advertisingInterval + " greater than RPI Interval: " + RPI_INTERVAL);
            //throw new IllegalArgumentException("Advertising Interval cannot be greater than RPI Interval");
        }

        this.context = context;
        this.advertisingInterval = advertisingInterval;
    }

    /**
     * Synchonized Update to make sure only one thread is starting advertisement at any time
     * @param observable
     * @param o
     */
    @Override
    public synchronized void update(Observable observable, Object o) {
        byte[] rollingProximityID = (byte[]) o;

        String key = Identifier.fromBytes(rollingProximityID, 0, 16, false).toString();
        Beacon beacon = new Beacon.Builder()
                .setId1(key)
                .build();

        // If the beacon transmitter has already been transmitting,
        // stop it and start again with the new beacon
        if (beaconTransmitter == null) {
            beaconTransmitter = new BeaconTransmitter(context, BEACON_PARSER);
        } else if (beaconTransmitter.isStarted()) {
            Log.d(BEACON_ADVERTISEMENT_TAG, "Stopping Advertisement of earlier key");
            beaconTransmitter.stopAdvertising();
        }

        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.d(BEACON_ADVERTISEMENT_TAG, "Beacon Advertisement on main thread successful");
                Log.d(BEACON_ADVERTISEMENT_TAG, "Advertising RPI: " + key);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                Log.d(BEACON_ADVERTISEMENT_TAG, "Beacon Advertisement failed for key: " + key);
                if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                    Log.d(BEACON_ADVERTISEMENT_TAG, "Too many advertisers already advertising: " + errorCode);
                } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                    Log.d(BEACON_ADVERTISEMENT_TAG, "Advertisement has already started, errorCode: " + errorCode);
                } else {
                    Log.d(BEACON_ADVERTISEMENT_TAG, "Error code unknown or multiple errors occurred, errorCode: "+ errorCode);
                }
            }
        });
    }
}
