package net.osmand.plus.traffic;

import android.os.Handler;

import net.osmand.plus.OsmandApplication;

/**
 * Created by vial-grelier on 25/03/2017.
 */

public class BackgroundHandler {

    private final static int INTERVAL = 1000 * 60 * 2; //2 minutes (ms * s * min)
    static Handler mHandler = new Handler();

    static OsmandApplication app;

    static Runnable mHandlerTask = new Runnable() {

        @Override
        public void run() {
            TrafficParser.getTrafficInfo(app);
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
