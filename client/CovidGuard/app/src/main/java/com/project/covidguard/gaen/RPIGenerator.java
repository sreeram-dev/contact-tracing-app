package com.project.covidguard.gaen;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import com.project.covidguard.data.repositories.TEKRepository;

import org.altbeacon.beacon.Identifier;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.project.covidguard.gaen.GAENConstants.RPI_INTERVAL;
import static com.project.covidguard.gaen.GAENConstants.TEK_INTERVAL;


public class RPIGenerator extends Observable implements Runnable {

    private static final String LOG_TAG = RPIGenerator.class.getCanonicalName();

    private TEKRepository tekRepo;

    private SecureRandom secureRandom;

    public RPIGenerator(SecureRandom secureRandom, Context context, List<Observer> observers) {
        // If the rpi interval is greater than tek interval, this service will not work as intended.
        assert RPI_INTERVAL < TEK_INTERVAL;

        this.secureRandom = secureRandom;
        this.tekRepo = new TEKRepository(context);
        for (Observer ob: observers) {
            this.addObserver(ob);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        com.project.covidguard.data.entities.TEK tek = tekRepo.getLastTek();
        ZonedDateTime time = LocalDateTime.now().atZone(ZoneId.systemDefault());
        // EnIntervalNumber of the current epoch timestamp
        Long presentENIN = Utils.getENIntervalNumber(time.toEpochSecond());

        byte[] TEK;
        Long tekENIN;

        if (tek == null || presentENIN - tek.getEnIntervalNumber() >= TEK_INTERVAL) {
            TEK = secureRandom.generateSeed(16);
            String encodedTek = android.util.Base64.encodeToString(TEK, Base64.DEFAULT);
            tekRepo.storeTEKWithEnIntervalNumber(encodedTek, presentENIN);
            tekENIN = presentENIN;
            Log.d(LOG_TAG, "Payload NEW Tek Generated: "
                    + "\nENIntervalNumber: " + presentENIN
                    + "\nTIME: " + time.toEpochSecond()
                    + "\nTEK: " + Arrays.toString(TEK)
                    + "\nRPIKey: " + Arrays.toString(Utils.getRPIKeyFromTEK(TEK)));
        } else if (presentENIN - tek.getEnIntervalNumber() < TEK_INTERVAL) {
            TEK = Base64.decode(tek.getTekId(), Base64.DEFAULT);
            tekENIN = tek.getEnIntervalNumber();
        } else {
            TEK = null;
            tekENIN = 0L;
        }

        if (TEK == null || TEK.length != 16 || tekENIN.equals(0L)) {
            String msg = "TEK cannot be null or not equal to 16 chars";
            Log.e(LOG_TAG, msg);
            throw new IllegalStateException(msg);
        }

        byte[] rollingProximityID = Utils.generateRPIForTEKAndEnIntervalNumber(TEK, presentENIN);

        Log.d(LOG_TAG, "Payload "
                + "\nTEK ENIN: " + tekENIN
                + "\nTEK: " + Arrays.toString(TEK)
                + "\nRPIKey: " + Arrays.toString(Utils.getRPIKeyFromTEK(TEK))
                + "\nRPI ENIN: " + presentENIN
                + "\nRPI: " + Arrays.toString(rollingProximityID)
                + "\nRPI String: " + Identifier.fromBytes(rollingProximityID, 0, 16, false).toString());

        setChanged();
        notifyObservers(rollingProximityID);
    }
}
