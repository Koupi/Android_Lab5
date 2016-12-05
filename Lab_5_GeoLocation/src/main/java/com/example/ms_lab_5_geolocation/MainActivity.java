package com.example.ms_lab_5_geolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView tvOut;
    TextView tvLon;
    TextView tvLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOut = (TextView) findViewById(R.id.out);
        tvLon = (TextView) findViewById(R.id.lon);
        tvLat = (TextView) findViewById(R.id.lat);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener mlocListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                tvLat.append(" " + location.getLatitude());
                tvLon.append(" " + location.getLongitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            tvOut.setText("GPS is turned on...");
        } else {
            tvOut.setText("GPS is not turned on...");
        }
    }
}
