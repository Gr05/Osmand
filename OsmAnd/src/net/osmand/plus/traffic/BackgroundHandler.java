package net.osmand.plus.traffic;

import android.os.Handler;

import net.osmand.plus.OsmandApplication;

/**
 * Created by vial-grelier on 25/03/2017.
 */

public class BackgroundHandler {

    /*
    Here is the management of looping and threading information gathering.
     */

    private final static int INTERVAL = 1000 * 60 * 2; //2 minutes (ms * s * min)
    static Handler mHandler = new Handler();

    static OsmandApplication app;

    static Runnable mHandlerTask = new Runnable() {

        @Override
        public void run() {
            // calls the function that retrieves information
            TrafficParser.getTrafficInfo(app);
            EventParser.getEventsInfo(app);
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }

    };

    public static void startRepeatingGetTrafficInfo(OsmandApplication application) {
        app = application;
        mHandlerTask.run();
    }

    public static void stopRepeatingGetTrafficInfo()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

}
