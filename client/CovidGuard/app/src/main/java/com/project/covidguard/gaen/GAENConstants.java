package com.project.covidguard.gaen;

import java.util.concurrent.TimeUnit;

public class GAENConstants {

    /**
        This beacon layout is for the Exposure Notification service Bluetooth Spec
        That layout string above is what tells the library how to understand this new beacon type.
        The layout “s:0-1=fd6f,p:-:-59,i:2-17,d:18-21” means that the advertisement is a gatt service type (“s:”) with a 16-bit service UUID of 0xfd6f (“0-1=fd6f”)
        and it has a single 16-byte identifier in byte positions 2-17 of the advertisement (“i:2-17”)
        The “p:-:-59” indicates that there is no unencrypted measured power calibration reference transmitted with this beacon,
        and the library should default to using a 1-meter reference of -59 dBm for its built-in distance estimates.
    */
    public static final String BEACON_LAYOUT = "s:0-1=fd6f,p:-:-59,i:2-17";

    /**
     * For how long do you want to advertise the present RPI
     * It should be less than RPI Interval
     */
    public static final Integer ADVERTISING_INTERVAL = 10;

    /**
     * Time units for the periodic interval in the ScheduledExecutorService (KeyGenerationService)
     * NEW TEK is generated if the Present epoch interval number is TEK_INTERVAL apart from the last one stored in DB
     * or If the Key Generation Service initially starts.
     * RPI_INTERVAL has to be lesser than TEK_INTERVAL
     */
    public static final Integer TEK_INTERVAL = 5; // minutes

    /**
     * Time Units for the periodic interval for RPI Generation in the ScheduledExecutorService
     * RPIInterval is the interval at which RPI Generation service creates the system
     */
    public static final TimeUnit RPI_TIME_UNIT = TimeUnit.MINUTES;
    public static final Integer RPI_INTERVAL = 1;

    /**
     * SECS_PER_MIN and MINUTES_PER_INTERVAL
     * MINUTES_PER_INTERVAL is equal to RPI_INTERVAL because during each ENIN,
     * We advertise an RPI unique to the ENIN
     */
    public static final int SECS_PER_MIN = 60;
    public static final int MINUTES_PER_INTERVAL = RPI_INTERVAL;
}
