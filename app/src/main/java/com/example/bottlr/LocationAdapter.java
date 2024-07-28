package com.example.bottlr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private List<Location> locations;
    private List<Location> allLocations;

    public LocationAdapter(List<Location> locations) {
        this.locations = locations != null ? locations : new ArrayList<>(); // Ensure locations is not null
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName, locationCoordinates, locationTimestamp;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            locationCoordinates = itemView.findViewById(R.id.locationCoordinates);
            locationTimestamp = itemView.findViewById(R.id.locationTimestamp);
        }
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_tag, viewGroup, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LocationViewHolder holder, int position) {
        Location location = locations.get(position);
        holder.locationName.setText(location.getName());
        holder.locationCoordinates.setText(location.getGpsCoordinates());
        holder.locationTimestamp.setText(location.getTimeDateAdded());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void setLocations(List<Location> locations) {
        this.locations = new ArrayList<>(locations);
        notifyDataSetChanged();
    }
}