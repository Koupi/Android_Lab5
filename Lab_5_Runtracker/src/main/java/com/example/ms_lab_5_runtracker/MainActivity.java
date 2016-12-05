package com.example.ms_lab_5_runtracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static Location currentLocation = null;
    int elapsedSeconds = 0;

    TextView tvStarted;
    TextView tvElapsed;
    TextView tvLat;
    TextView tvLon;
    TextView tvAlt;

    Handler timerHandler;
    LocationManager manager;
    PendingIntent locationPendingIntent;
    final String LOCATION_ACTION = "com.example.ms_lab_5_runtracker.RUN_TRACKER_LOCATION_ACTION";

    boolean isWorking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStarted = (TextView) findViewById(R.id.started);
        tvElapsed = (TextView) findViewById(R.id.elapsed);
        tvLat = (TextView) findViewById(R.id.lat);
        tvLon = (TextView) findViewById(R.id.lon);
        tvAlt = (TextView) findViewById(R.id.alt);

        timerHandler = new Handler();
        manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    Runnable timeTick = new Runnable() {
        @Override
        public void run() {
            elapsedSeconds++;
            int sec = elapsedSeconds % 60;
            int min = (elapsedSeconds - sec) / 60;
            tvElapsed.setText(String.format("%02d:%02d", min, sec));
            if (currentLocation != null) {
                tvLat.setText(Double.toString(currentLocation.getLatitude()));
                tvLon.setText(Double.toString(currentLocation.getLongitude()));
                tvAlt.setText(Double.toString(currentLocation.getAltitude()));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    public void start(View view) {
        if (isWorking) {
            return;
        }

        Intent locIntent = new Intent(this, MainActivity.LocationReceiver.class);
        locIntent.setAction(LOCATION_ACTION);
        locationPendingIntent = PendingIntent.getBroadcast(this, 0, locIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationPendingIntent);

        elapsedSeconds = 0;
        int sec = elapsedSeconds % 60;
        int min = (elapsedSeconds - sec) / 60;
        tvElapsed.setText(String.format("%02d:%02d", min, sec));
        Calendar c = Calendar.getInstance();
        tvStarted.setText(String.format("%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));

        timerHandler.post(timeTick);

        isWorking = true;
    }

    public void stop(View view) {
        if (!isWorking) {
            return;
        }
        timerHandler.removeCallbacks(timeTick);
        manager.removeUpdates(locationPendingIntent);
        isWorking = false;
    }

    public static class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            Location loc = (Location) b.get(android.location.LocationManager.KEY_LOCATION_CHANGED);
            if (loc != null) {
                MainActivity.currentLocation = loc;
            }
        }
    }
}
