package com.wasdkiller.taximeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Time;

import static com.wasdkiller.taximeter.R.id.emailView;
import static com.wasdkiller.taximeter.R.id.userImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static LocationManager locationManager;
    LocationListener locationListener;
    public static Button start, end, pause, resume;
    public static TextView speed, distance, waitingTime, fare, username, email;
    public static ImageView userImage;
    public static String userFullName, userEmail, userID, waitingTimePeriod;
    public static Location previousLocation;
    public static float totalDistance, finalFirstKM, finalOtherKM, finalWaitingPrice, totalPrice;
    public static boolean statusChanged;
    public static RelativeLayout progressBarView;
    public static SharedPreferences shrPrf;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    Typeface taxiFont;
    Uri uri;

    private DatabaseReference databaseReference;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        start = (Button) findViewById(R.id.startButton);
        end = (Button) findViewById(R.id.endButton);
        pause = (Button) findViewById(R.id.pauseButton);
        resume = (Button) findViewById(R.id.resumeButton);
        speed = (TextView) findViewById(R.id.speed);
        distance = (TextView) findViewById(R.id.distance);
        waitingTime = (TextView) findViewById(R.id.waitingTime);
        fare = (TextView) findViewById(R.id.fare);
        username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.usernameView);
        email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.emailView);
        userImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.userImageView);
        locationListener = new LocationService(getApplicationContext());
        progressBarView = (RelativeLayout) findViewById(R.id.progressBarLayout);
        mAuth = FirebaseAuth.getInstance();
        taxiFont = Typeface.createFromAsset(getAssets(), "fonts/taximeter.ttf");
        speed.setTypeface(taxiFont);
        distance.setTypeface(taxiFont);
        waitingTime.setTypeface(taxiFont);
        fare.setTypeface(taxiFont);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        shrPrf = this.getSharedPreferences("com.wasdkiller.taximeter", MODE_PRIVATE);

//        TableLayout t = (TableLayout) findViewById(R.id.activity_settings);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                progressBarView.setVisibility(View.VISIBLE);
                if(firebaseAuth.getCurrentUser()== null){
                    Log.i("TaxiMeter",".getCurrentUser()== null");
                    mAuth.removeAuthStateListener(mAuthListener);
                    Toast.makeText(getApplicationContext(), "Signing out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                else{
                    userFullName = mAuth.getCurrentUser().getDisplayName();
                    userEmail = mAuth.getCurrentUser().getEmail();
                    userID = mAuth.getCurrentUser().getUid();
                    username.setText(userFullName);
                    email.setText(userEmail);
                    uri = Uri.parse(mAuth.getCurrentUser().getPhotoUrl().toString());

                    userImage.setImageURI(uri);

                    Glide.with(getApplicationContext()).load(mAuth.getCurrentUser().getPhotoUrl().toString())
                            .crossFade()
                            .thumbnail(0.5f)
                            .bitmapTransform(new CircleTransform(getApplicationContext()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(userImage);

                    databaseReference.child(userID).child("userFullName").setValue(userFullName);
                    databaseReference.child(userID).child("userEmail").setValue(userEmail);
                }
                progressBarView.setVisibility(View.GONE);
            }
        };

        checkPermission();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finalFirstKM = shrPrf.getFloat("firstkm",50);
                finalOtherKM = shrPrf.getFloat("otherkm",30);
                finalWaitingPrice = shrPrf.getFloat("waiting",1.5f);

                totalPrice = finalFirstKM;

                fare.setText("" + totalPrice);
                distance.setText("00.0");
                waitingTime.setText("00:00:00");
                speed.setText("00.0");
                Log.i("TaxiMeter", "START button pressed");
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//                        ActivityCompat.requestPermissions(getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
//                    }
//                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                    checkPermission();
                    Log.i("TaxiMeter", "ELSE PART");
                    return;
                }
                Log.i("TaxiMeter", "IF PART");
                previousLocation = null;
                waitingTimePeriod = "00:00:00";
                totalDistance = (float) 0;
                statusChanged = true;
                progressBarView.setVisibility(View.VISIBLE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                start.setVisibility(View.GONE);
                end.setEnabled(false);
                pause.setEnabled(false);
                end.setVisibility(View.VISIBLE);
                pause.setVisibility(View.VISIBLE);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TaxiMeter", "END button pressed");
                locationManager.removeUpdates(locationListener);
                start.setVisibility(View.VISIBLE);
                end.setVisibility(View.GONE);
                pause.setVisibility(View.GONE);
                resume.setVisibility(View.GONE);
                progressBarView.setVisibility(View.GONE);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusChanged = false;
                resume.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
                previousLocation = null;
                progressBarView.setVisibility(View.VISIBLE);
            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusChanged = true;
                pause.setVisibility(View.VISIBLE);
                resume.setVisibility(View.GONE);
                progressBarView.setVisibility(View.GONE);
            }
        });

    }

//    public class LocationService extends Service implements LocationListener{
//
//        @Nullable
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//
//        @Override
//        public void onLocationChanged(Location location) {
//            Log.i("TaxiMeter", location.toString());
//            Log.i("TaxiMeter", "Speed + " + String.valueOf(location.getSpeed()));
////        String.valueOf(location.getSpeed());
//            taxiMeterUpdate(String.valueOf(location.getSpeed()));
////        test.taxiMeterUpdate();
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Log.i("TaxiMeter", "onStatusChanged");
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            Log.i("TaxiMeter", "onProviderEnabled");
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            Log.i("TaxiMeter", "onProviderDisabled");
////        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
////        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////            startActivity(intent);
////        displayGpsStatus();
//        }

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
//    }

    public void taxiMeterUpdate(String kmh){
        Log.i("TaxiMeter", "taxiMeterUpdate " + kmh);
        speed.setText(kmh);
        Toast.makeText(this, kmh, Toast.LENGTH_SHORT).show();
    }

    public void checkPermission(){
        Log.i("TaxiMeter", "checkPermission");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }


//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signout) {
            mAuth.signOut();
            LoginActivity.mGoogleSignInClient.signOut();
            Log.i("TaxiMeter","nav_signout window clicked");
        } else if (id == R.id.nav_user_details) {
            Log.i("TaxiMeter","nav_user_details window clicked");
        } else if (id == R.id.nav_history) {
            Log.i("TaxiMeter","nav_history window clicked");
        } else if (id == R.id.nav_settings) {
            Log.i("TaxiMeter","nav_settings window click start");
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
