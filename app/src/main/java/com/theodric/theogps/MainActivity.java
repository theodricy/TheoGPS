package com.theodric.theogps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    LocationManager _locationManager;
    TextView _statusView;
    TextView _gpsStatusView;
    TextView _mainView;
    GpsStatus _gpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _mainView = (TextView) findViewById(R.id.main_text);
        _statusView = (TextView) findViewById(R.id.status_text);
        _gpsStatusView = (TextView) findViewById(R.id.gps_status_text);
        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // if permission has not been granted, asynchonously ask the user for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        } else {
            _locationManager.requestLocationUpdates(GPS_PROVIDER, 500L, 0f, (LocationListener) this);
            _gpsStatus = _locationManager.getGpsStatus((GpsStatus) null);
            displayGpsStatus();
        }

        _statusView.setText("no status information");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED)
                        _locationManager.requestLocationUpdates(GPS_PROVIDER, 500L, 0f, (LocationListener) this);
                } else {
                    _statusView.setText("Cannot get GPS location due to lack of permission.");
                }
            }
        }
    }

    public void onLocationChanged(Location location) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.US);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
        calendar.setTimeInMillis(location.getTime());

        String text = location.getProvider() + "\n";
        text += "Lat: " + locationString(location.getLatitude()) + "\n";
        text += "Lon: " + locationString(location.getLongitude()) + "\n";
        text += "Alt: " + String.format("%.2f", location.getAltitude()) + " m\n";
        text += "Time: " + sdf.format(calendar.getTime()) + "\n";
        text += "Accuracy: " + location.getAccuracy() + " m \n";
        _mainView.setText(text);
    }

    public void onProviderDisabled(String provider) {
        String text = "Provider " + provider + " disabled";
        _statusView.setText(text);
    }

    public void onProviderEnabled(String provider) {
        String text = "Provider " + provider + " enabled";
        _statusView.setText(text);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        String text = "Provider " + provider + " changed status to: " + status + "\n";
        text += "bundle size = " + extras.size() + "\n";
        Set<String> keys = extras.keySet();
        String[] arr = new String[keys.size()];
        arr = keys.toArray(arr);
        for (int i = 0; i < extras.size(); i++) {
            text += "extras." + arr[i] + " = " + extras.getString(arr[i], "unknown") +
                    " (" + extras.get(arr[i].getClass() + ")\n");
        }
        _statusView.setText(text);
    }

    public void onGpsStatusChanged(int status_id) {
        String text = "GpsStatus chanaged to: " + status_id + "\n";
        try {
            GpsStatus status = _locationManager.getGpsStatus((GpsStatus) null);
            if (null != status) {
                text += "max satellites = " + status.getMaxSatellites() + "\n";
                text += "time to 1st fix = " + status.getTimeToFirstFix() + "\n";
            }
        } catch (SecurityException e) {
            _statusView.setText("Cannot get GPS status due to security: " + e);
        }
        _statusView.setText(text);
    }

    public void displayGpsStatus() {
        String text = "";
        if (null != _gpsStatus) {
            text += "max satellites = " + _gpsStatus.getMaxSatellites() + "\n";
            text += "time to 1st fix = " + _gpsStatus.getTimeToFirstFix() + "\n";
        } else {
            text = "no GPS status to display";
        }
        _gpsStatusView.setText(text);
    }

    private static String locationString(double x) {
        boolean positive = (x >= 0);
        x = Math.abs(x);
        int deg = (int) Math.floor(x);
        double min = (x - deg) * 60.0;
        String text = (positive ? "" : "-") + deg + "\u00b0 " + ((min < 10.0) ? "0" : "") +
            String.format("%.4f", min) + "'";
        return text;
    }

}
