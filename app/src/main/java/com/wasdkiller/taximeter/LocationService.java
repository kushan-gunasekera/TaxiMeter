package com.wasdkiller.taximeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LocationService extends Service implements LocationListener{

    private final Context context;

    public LocationService(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(MainActivity.statusChanged){
            MainActivity.progressBarView.setVisibility(View.GONE);
            MainActivity.end.setEnabled(true);
            MainActivity.pause.setEnabled(true);
            float taxiSpeed = location.getSpeed()*(float)3.5;
//        test.taxiMeterUpdate();
//        Log.i("TaxiMeter", "onLocationChanged");
//        Log.i("TaxiMeter", MainActivity.speed.toString());
////        MainActivity.speed.setText("fuck");
//        Date dt = new Date(location.getTime());
//        Log.i("TaxiMeter", String.valueOf(dt));

            if(MainActivity.previousLocation==null){
            Log.i("TaxiMeter", "MainActivity.previousLocation==null");
//            Log.i("TaxiMeter", "set previousLocation values " + location.toString());
                MainActivity.previousLocation = new Location("");
                MainActivity.previousLocation = location;
//            Log.i("TaxiMeter", String.valueOf(MainActivity.previousLocation));
//            MainActivity.previousLocation.setLongitude();
//            MainActivity.previousLocation.setLatitude();

            }
            else{
//            Log.i("TaxiMeter", "MainActivity.previousLocation!=null");
//            Log.i("TaxiMeter", "current gps location " + location.toString());
                float distance = location.distanceTo(MainActivity.previousLocation);
                Log.i("TaxiMeter", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + String.valueOf(distance));
                Log.i("TaxiMeter", "distance : "+String.format("%.5f",distance));
                MainActivity.previousLocation = new Location("");
                MainActivity.previousLocation = location;
//            Log.i("TaxiMeter", "set previousLocation values " + MainActivity.previousLocation.toString());
                MainActivity.totalDistance = MainActivity.totalDistance + distance/1000;
                Log.i("TaxiMeter", "total distance : "+String.format("%.1f",MainActivity.totalDistance));
                MainActivity.distance.setText(String.format("%.1f",MainActivity.totalDistance));
                Log.i("TaxiMeter", String.format("%.1f",MainActivity.totalDistance));
                MainActivity.speed.setText(String.format("%.1f",taxiSpeed));
                if(taxiSpeed==(float)0.0){
                    try {
                        calculateTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("TaxiMeter", "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("TaxiMeter", "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("TaxiMeter", "onProviderDisabled");
//        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        startActivity(intent);
//        displayGpsStatus();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public void calculateTime() throws ParseException {
//        String myTime = "14:10";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date d = df.parse(MainActivity.waitingTimePeriod);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.SECOND, 1);
        MainActivity.waitingTimePeriod = df.format(cal.getTime());
        MainActivity.waitingTime.setText(MainActivity.waitingTimePeriod);
    }

//    private void displayGpsStatus(){
//        ContentResolver contentResolver = getBaseContext().getContentResolver();
//        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
//        if(gpsStatus){
//            Toast.makeText(LocationService.this, "GPS Enabled: ", Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(LocationService.this, "GPS Disabled: ", Toast.LENGTH_LONG).show();
//        }
//    }

//    private void CheckEnableGPS(){
//        String provider = Settings.Secure.getString(getContentResolver(),
//                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//        if(!provider.equals("")){
//            //GPS Enabled
//            Toast.makeText(LocationService.this, "GPS Enabled: " + provider,
//                    Toast.LENGTH_LONG).show();
//        }else{
//            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//            startActivity(intent);
//        }
//
//    }
}
