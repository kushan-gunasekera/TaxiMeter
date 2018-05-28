package com.wasdkiller.taximeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onLocationChanged(Location location) {
        if(MainActivity.statusChanged){
            MainActivity.progressBarView.setVisibility(View.GONE);
            MainActivity.end.setEnabled(true);
            MainActivity.pause.setEnabled(true);
            float taxiSpeed = location.getSpeed()*(float)3.5;

            if(MainActivity.firstOrLast){
                MainActivity.startNow = new Date( );
                MainActivity.startLocation = location;
                MainActivity.firstOrLast = false;
            }
            else {
                MainActivity.endLocation = location;
            }

            if(MainActivity.previousLocation==null){
                MainActivity.previousLocation = new Location("");
                MainActivity.previousLocation = location;
            }
            else{
                if(taxiSpeed==(float)0.0){
                    MainActivity.speed.setText("0.0");
                    try {
                        calculateTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    float distance = location.distanceTo(MainActivity.previousLocation);
                    MainActivity.previousLocation = new Location("");
                    MainActivity.previousLocation = location;
                    MainActivity.totalDistance = MainActivity.totalDistance + distance/1000;

                    if(MainActivity.totalDistance >= 1.00){
                        float pricePerKM = distance * (MainActivity.finalOtherKM/1000);
                        BigDecimal bd = new BigDecimal(Float.toString(pricePerKM));
                        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                        setPrice(bd.floatValue());
                    }
                    MainActivity.distance.setText(String.format("%.2f",MainActivity.totalDistance));
                    MainActivity.speed.setText(String.format("%.1f",taxiSpeed));
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
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public void calculateTime() throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date d = df.parse(MainActivity.waitingTimePeriod);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.SECOND, 1);
        MainActivity.waitingTimePeriod = df.format(cal.getTime());
        MainActivity.waitingTime.setText(MainActivity.waitingTimePeriod);
        setPrice(MainActivity.finalWaitingPrice / 60);
    }

    public void setPrice(float addingValue){
        float getFareValue = Float.valueOf("" + MainActivity.fare.getText());
        MainActivity.totalPrice = getFareValue + addingValue;
        MainActivity.fare.setText(String.format("%.2f",MainActivity.totalPrice));
    }
}
