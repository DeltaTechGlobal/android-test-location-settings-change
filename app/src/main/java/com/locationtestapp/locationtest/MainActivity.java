package com.locationtestapp.locationtest;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int kLocationPermissionRequest = 42;
    private final String LOG_TAG = "LocationTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isLocationPermissionGranted = CheckHighAccuracyLocationEnabled.checkLocationPermission(this);
        requestLocationPermission(isLocationPermissionGranted);
    }

    public void setLocationChangeAlarm(View v) {
        LocationCheckAlarm.getInstance(this).setAlarm(this);
    }

    public void unsetLocationChangeAlarm(View v) {
        LocationCheckAlarm.getInstance(this).unsetAlarm(this);
    }

    public void requestLocationPermission(boolean granted) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!granted) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        kLocationPermissionRequest);
            } else {
                Toast.makeText(this, "Location permission granted",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            throw new RuntimeException("Callback on non marshmallow sdk");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grants) {
        if (requestCode == kLocationPermissionRequest) {
            if (grants[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "Permission granted for fine location");
            } else {
                new AlertDialog.Builder(this).setTitle("Location permission")
                        .setMessage("Permission denied for fine location")
                        .setPositiveButton("Ok", null).create().show();
                Log.d(LOG_TAG, "Permission denied for fine location");
            }
        }
    }
}
