package com.wasdkiller.taximeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static LocationManager locationManager;
    LocationListener locationListener;
    public static Button start, end, pause, resume;
    public static TextView speed, distance, waitingTime, fare, username, email;
    public static ImageView userImage;
    public static String userFullName, userEmail, userID, waitingTimePeriod;
    public static Location previousLocation, startLocation, endLocation;
    public static float totalDistance, finalFirstKM, finalOtherKM, finalWaitingPrice, totalPrice;
    public static boolean statusChanged, firstOrLast;
    public static RelativeLayout progressBarView;
    public static SharedPreferences shrPrf;
    public static Date startNow, endNow;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    Typeface taxiFont;
    Uri uri;

    public static DatabaseReference databaseReference;

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

        // Checking Firebase authentication status
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                progressBarView.setVisibility(View.VISIBLE);
                if(firebaseAuth.getCurrentUser()== null){
                    // Load into the login window
                    mAuth.removeAuthStateListener(mAuthListener);
                    Toast.makeText(getApplicationContext(), "Signing out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                else{
                    // Display user infomation in a menu bar
                    userFullName = mAuth.getCurrentUser().getDisplayName();
                    userEmail = mAuth.getCurrentUser().getEmail();
                    userID = mAuth.getCurrentUser().getUid();
                    username.setText(userFullName);
                    email.setText(userEmail);
                    uri = Uri.parse(mAuth.getCurrentUser().getPhotoUrl().toString());

                    userImage.setImageURI(uri);

                    // Loading user's image in menu bar with cicrle
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

        // Checking the permission when app starting
        checkPermission();

        // START button function
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
                firstOrLast = true;
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    checkPermission();
                    return;
                }

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

        // END button pressed
        end.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View v) {
                locationManager.removeUpdates(locationListener);
                endNow = new Date( );
                start.setVisibility(View.VISIBLE);
                end.setVisibility(View.GONE);
                pause.setVisibility(View.GONE);
                resume.setVisibility(View.GONE);
                progressBarView.setVisibility(View.GONE);

                BigDecimal bd = new BigDecimal(Float.toString(totalDistance));
                bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

                BigDecimal db = new BigDecimal(Float.toString(totalPrice));
                db = db.setScale(2, BigDecimal.ROUND_HALF_UP);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a");

                String pathID = databaseReference.child(userID).child("history").push().getKey();
                TripDetails tripDetails = new TripDetails(bd.toString(), db.toString(), waitingTimePeriod, Double.toString(startLocation.getLongitude()), Double.toString(startLocation.getLatitude()), Double.toString(endLocation.getLongitude()), Double.toString(endLocation.getLatitude()), simpleDateFormat.format(startNow), simpleDateFormat.format(endNow));

                databaseReference.child(userID).child("history").child(pathID).setValue(tripDetails);
            }
        });

        // PAUSE button pressed
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

        // RESUME button pressed
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

    // Checking location permission
    public void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signout) {
            mAuth.signOut();
            LoginActivity.mGoogleSignInClient.signOut();
            Log.i("TaxiMeter","nav_signout window clicked");
        } else if (id == R.id.nav_history) {
            Log.i("TaxiMeter","nav_history window clicked");
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        } else if (id == R.id.nav_settings) {
            Log.i("TaxiMeter","nav_settings window click start");
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
