package com.locationtestapp.locationtest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;

public class LocationCheckAlarm {
    private final String LOG_TAG = "LocationTestCheckAlarm";

    public static LocationCheckAlarm getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocationCheckAlarm(context);
        }
        return INSTANCE;
    }

    private LocationCheckAlarm(Context ctx) {
    }

    /**
     * Must *not* be called from a synchronized block
     */
    public void setAlarm(Context context) {
        AlarmManager alarmManager = getAlarmManager(context);
        // TODO(post-O): Get rid of this mutex if possible.
        synchronized (mutex) {
            PendingIntent alarmIntent = getAlarmIntent(context);
            // Cancel pending alarms, if any
            alarmManager.cancel(alarmIntent);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + LOCATION_CHECK_INTERVAL_MILLIS,
                    alarmIntent);
            Log.d(LOG_TAG, "Setting alarm for " + LOCATION_CHECK_INTERVAL_MILLIS);
        }
    }

    public void unsetAlarm(Context context) {
        AlarmManager alarmManager = getAlarmManager(context);
        synchronized (mutex) {
            Log.d(LOG_TAG, "Stopping alarm");
            alarmManager.cancel(getAlarmIntent(context));
        }
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, LocationCheckReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    // Check every 15 sec
    private static final long LOCATION_CHECK_INTERVAL_MILLIS = INTERVAL_FIFTEEN_MINUTES / 60;

    private static LocationCheckAlarm INSTANCE;
    private final Object mutex = new Object();

}
