package com.wasdkiller.taximeter;

import android.util.Log;

public class ListItem {

    // ListItem is the class for display history details

    private String distance;
    private String price;
    private String waitingTime;
    private String startingLongitude;
    private String stringLatitude;
    private String endingLongitude;
    private String endingLatitude;
    private String startDataAndTime;
    private String endDataAndTime;

    public ListItem(){
        Log.i("TaxiMeter", "im here ListItem default");
    }

    public ListItem(String distance, String price, String waitingTime, String startingLongitude, String stringLatitude, String endingLongitude, String endingLatitude, String startDataAndTime, String endDataAndTime) {
        this.distance = "Distance : " + distance + "km";
        this.price = "Price : Rs." + price+"/=";
        this.waitingTime = "Waiting Time : " + waitingTime;
        this.startingLongitude = "Starting Longitude : " + startingLongitude;
        this.stringLatitude = "Starting Latitude : " + stringLatitude;
        this.endingLongitude = "Ending Longitude : " + endingLongitude;
        this.endingLatitude = "Ending Latitude : " + endingLatitude;
        this.startDataAndTime = "Starting Data & Time : " + startDataAndTime;
        this.endDataAndTime = "Ending Data & Time : " + endDataAndTime;
    }

    public String getDistance() {
        return distance;
    }

    public String getPrice() {
        return price;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public String getStartingLongitude() {
        return startingLongitude;
    }

    public String getStringLatitude() {
        return stringLatitude;
    }

    public String getEndingLongitude() {
        return endingLongitude;
    }

    public String getEndingLatitude() {
        return endingLatitude;
    }

    public String getStartDataAndTime() {
        return startDataAndTime;
    }

    public String getEndDataAndTime() {
        return endDataAndTime;
    }
}
