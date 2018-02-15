package com.locationtestapp.locationtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class LocationCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        PendingResult result = goAsync();
        CheckHighAccuracyLocationEnabled.checkHighAccuracyLocationAndMaybeSendNotification(context, result);
        LocationCheckAlarm.getInstance(context).setAlarm(context);
    }
}
