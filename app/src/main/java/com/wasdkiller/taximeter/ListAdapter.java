package com.wasdkiller.taximeter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<ListItem> listItems;
    private Context context;
    private LayoutInflater mInflater;

    public ListAdapter( List<ListItem> listItems, Context context){
//        super(context, R.layout.rowlayout, li);
        this.listItems = listItems;
        this.mInflater = LayoutInflater.from(context);
        this.context=context; //<< initialize here
    }

    // data is passed into the constructor
    ListAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
//        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem listItem = listItems.get(position);

        holder.stDateTime.setText(listItem.getStartDataAndTime());
        holder.edDateTime.setText(listItem.getEndDataAndTime());
        holder.dID.setText(listItem.getDistance());
        holder.prc.setText(listItem.getPrice());
        holder.wTime.setText(listItem.getWaitingTime());
        holder.srtLng.setText(listItem.getStartingLongitude());
        holder.srtLtd.setText(listItem.getStringLatitude());
        holder.endLng.setText(listItem.getEndingLongitude());
        holder.endLtd.setText(listItem.getEndingLatitude());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView stDateTime, edDateTime, dID, prc, wTime, srtLng,srtLtd, endLng, endLtd;

        public ViewHolder(View itemView) {
            super(itemView);

            stDateTime = (TextView) itemView.findViewById(R.id.stDateTime);
            edDateTime = (TextView) itemView.findViewById(R.id.edDateTime);
            dID = (TextView) itemView.findViewById(R.id.dID);
            prc = (TextView) itemView.findViewById(R.id.prc);
            wTime = (TextView) itemView.findViewById(R.id.wTime);
            srtLng = (TextView) itemView.findViewById(R.id.srtLng);
            srtLtd = (TextView) itemView.findViewById(R.id.srtLtd);
            endLng = (TextView) itemView.findViewById(R.id.endLng);
            endLtd = (TextView) itemView.findViewById(R.id.endLtd);
        }
    }
}
