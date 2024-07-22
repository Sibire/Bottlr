package com.example.bottlr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList;

    public LocationAdapter(List<Location> locationList) {
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_tag, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        holder.locationName.setText(location.getName());
        holder.locationCoordinates.setText(location.getGpsCoordinates());
        holder.locationTimestamp.setText(location.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        TextView locationCoordinates;
        TextView locationTimestamp;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            locationCoordinates = itemView.findViewById(R.id.locationCoordinates);
            locationTimestamp = itemView.findViewById(R.id.locationTimestamp);
        }
    }
}