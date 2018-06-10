package com.wasdkiller.taximeter;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ListItem> listItems;
    private DatabaseReference databaseReference;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseReference = FirebaseDatabase.getInstance().getReference(MainActivity.userID+"/history");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        context = this;

        listItems = new ArrayList<ListItem>();
        Log.i("TaxiMeter", "this : " + this.toString());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                listItems.clear();

                for(DataSnapshot historySnapshot : dataSnapshot.getChildren()){
                    ListItem tripDetails = historySnapshot.getValue(ListItem.class);
                    ListItem tripDetailswithDescription = new ListItem(tripDetails.getDistance(), tripDetails.getPrice(), tripDetails.getWaitingTime(), tripDetails.getStartingLongitude(), tripDetails.getStringLatitude(), tripDetails.getEndingLongitude(), tripDetails.getEndingLatitude(), tripDetails.getStartDataAndTime(), tripDetails.getEndDataAndTime());
                    listItems.add(tripDetailswithDescription);
                }

                adapter = new ListAdapter(listItems, context);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
