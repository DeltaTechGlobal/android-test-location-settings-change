package com.locationtestapp.locationtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// Relevant URLs:
// Changing Location Settings: https://developer.android.com/training/location/change-location-settings.html
//
// Google Play Services location API reference:
// https://developers.google.com/android/reference/com/google/android/gms/location/package-summary
//
// LocationSettingsResult documentation:
// https://developers.google.com/android/reference/com/google/android/gms/location/LocationSettingsResult

public class CheckHighAccuracyLocationEnabled {
    private static final String TAG = "LocationTestSettings";

    public CheckHighAccuracyLocationEnabled(Context context) {

    }

    public static boolean checkLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED ==
                    PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            return true;
        }
    }

    public static LocationResult checkLocationSettings(Context context) {
        return checkIsHighAccuracyLocationEnabledSync(context, 5);
    }

    public static LocationResult checkIsHighAccuracyLocationEnabledSync(
            Context context, long timeoutSeconds) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            // Log exception appropriately using Crashlytics or some other service.
            //Crashlytics.logException(
            //        new IllegalStateException(
            //                "checkIsHighAccuracyLocationEnabledSync " +
            //                        "called on main thread")
            //);
        }
        GoogleApiClient client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
        ConnectionResult connectionResult = client.blockingConnect(timeoutSeconds,
                TimeUnit.SECONDS);
        LocationResult locationResult = new LocationResult();
        if (!connectionResult.isSuccess() || !client.isConnected()) {
            Log.e(TAG, "Play services connection failed - " + connectionResult.getErrorCode());
                    locationResult.errors.add(Error.GOOGLE_PLAY_SERVICES_CONNECTION_ERROR);
            return locationResult;
        }

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        LocationSettingsResult settingsResult = result.await(timeoutSeconds, TimeUnit.SECONDS);
        if (!settingsResult.getStatus().isSuccess()) {
            locationResult.errors.add(Error.GOOGLE_PLAY_SERVICES_ERROR_RESULT);
            locationResult.locationSettingsResultFromGooglePlayService = settingsResult;
        }
        return locationResult;
    }

    public static void checkHighAccuracyLocationAndMaybeSendNotification(final Context context,
                                                                         BroadcastReceiver.PendingResult result) {
        HighAccuracyLocationCheckTask highAccuracyLocationCheckTask =
                new HighAccuracyLocationCheckTask(context.getApplicationContext(), result);
        highAccuracyLocationCheckTask.executeOnExecutor(executorService);
    }

    public static class LocationResult {
        public LocationSettingsResult locationSettingsResultFromGooglePlayService;
        public List<Error> errors = new ArrayList<>();

        public LocationResult() {

        }

        public boolean isSuccess() { return errors.isEmpty(); }
    }

    public enum Error {
        GOOGLE_PLAY_SERVICES_ERROR_RESULT,
        GOOGLE_PLAY_SERVICES_CONNECTION_ERROR;
    }

    private static class HighAccuracyLocationCheckTask extends AsyncTask<Void,
            Void, Void> {
        @SuppressLint("StaticFieldLeak")  // Only stores application context
        private Context context;
        private BroadcastReceiver.PendingResult result;

        public HighAccuracyLocationCheckTask(Context context, BroadcastReceiver.PendingResult result){
            this.context = context;
            this.result = result;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final LocationResult locationSettingsResult = checkLocationSettings(context);
            maybeSendNotification(locationSettingsResult);
            return null;
        }

        private void maybeSendNotification(LocationResult locationSettingsResult) {
            if (!checkLocationPermission(context)) {
                createNotification("Location permission not granted",
                        "Please allow the app to access your location");
                Log.d(TAG, "Location permission not granted");
                return;
            }
            if (!locationSettingsResult.isSuccess()) {
                createNotification("Location not set correctly",
                        locationSettingsResult.errors.toString());
                Log.d(TAG, "Location not set correctly");
            } else {
                createNotification("Location set correctly",
                        "Need not do anything");
                Log.d(TAG, "Location set correctly");
            }
        }

        private void createNotification(String contentTitle, String contentText) {
            // Applications should ideally link this notification to their app
            // Once users clicks the notification, they should be shown the correct message in the
            // corresponding activity
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);


            //Show the notification
            notificationManager.notify(450, builder.build());
        }

        @Override
        protected void onPostExecute(Void integer) {
            result.finish();
        }

        @Override
        protected void onCancelled() {
            result.finish();
        }
    }

    private static ExecutorService executorService = new ScheduledThreadPoolExecutor(1);
}

