package com.wasdkiller.taximeter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private TextView firstKMView, otherKMView, waitingMinView;
    private SharedPreferences sharedPreferences;
    private Float firstkm, otherkm, waiting;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firstKMView = (TextView) findViewById(R.id.firstKMView);
        otherKMView = (TextView) findViewById(R.id.otherKMView);
        waitingMinView = (TextView) findViewById(R.id.waitingMinView);
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        firstKMView.setRawInputType(Configuration.KEYBOARD_12KEY);
        otherKMView.setRawInputType(Configuration.KEYBOARD_12KEY);
        waitingMinView.setRawInputType(Configuration.KEYBOARD_12KEY);

        sharedPreferences = this.getSharedPreferences("com.wasdkiller.taximeter", MODE_PRIVATE);

        firstkm = sharedPreferences.getFloat("firstkm",-1f);
        otherkm = sharedPreferences.getFloat("otherkm",-1f);
        waiting = sharedPreferences.getFloat("waiting",-1f);

        setValues(firstkm, otherkm, waiting);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TaxiMeter", "SAVE BUTTON CLICKED");
                try{
                    Log.i("TaxiMeter", "firstkm : " + firstKMView.getText());
                    Log.i("TaxiMeter", "otherkm : " + otherKMView.getText());
                    Log.i("TaxiMeter", "waiting : " + waitingMinView.getText());

                    firstkm = Float.valueOf("" + firstKMView.getText());
                    otherkm = Float.valueOf("" + otherKMView.getText());
                    waiting = Float.valueOf("" +  waitingMinView.getText());

                    Log.i("TaxiMeter", "firstkm : " + firstkm.toString());
                    Log.i("TaxiMeter", "otherkm : " + otherkm.toString());
                    Log.i("TaxiMeter", "waiting : " + waiting.toString());

                    setValues(firstkm, otherkm, waiting);
                }
                catch (Exception e){
                    Log.i("TaxiMeter", "Exception : " + e.toString());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void setValues(Float setFirstKM, Float setOtherKM, Float setWaitingMin){
        if(setFirstKM==-1f){
            sharedPreferences.edit().putFloat("firstkm", -1f);
            firstKMView.setText("50.0");
        }
        else {
            sharedPreferences.edit().putFloat("firstkm", setFirstKM).apply();
            firstKMView.setText(setFirstKM.toString());
        }
        if(setOtherKM==-1f){
            sharedPreferences.edit().putFloat("otherkm",-1f);
            otherKMView.setText("30.0");
        }
        else {
            sharedPreferences.edit().putFloat("otherkm", setOtherKM).apply();
            otherKMView.setText(setOtherKM.toString());
        }
        if(setWaitingMin==-1f){
            sharedPreferences.edit().putFloat("waiting",-1f);
            waitingMinView.setText("1.50");
        }
        else {
            sharedPreferences.edit().putFloat("waiting", setWaitingMin).apply();
            waitingMinView.setText(setWaitingMin.toString());
        }
    }
}
