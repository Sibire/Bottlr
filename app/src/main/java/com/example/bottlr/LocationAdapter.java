package com.example.bottlr;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private List<Location> locations;
    private List<Location> allLocations;
    interface OnLocationCheckListener {
        void onButtonClick(String name, String coordinates, String date);
    }

    @NonNull
    private final OnLocationCheckListener onLocationClick;
    public LocationAdapter(List<Location> locations, List<Location> allLocations, @NonNull OnLocationCheckListener onLocationCheckListener) {
        this.locations = locations;
        this.allLocations = allLocations;
        this.onLocationClick = onLocationCheckListener;
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewCoordinates, textViewTimestamp;
        Button locationButton;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCoordinates = itemView.findViewById(R.id.textViewCoordinates);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            locationButton = itemView.findViewById(R.id.locationsinglebutton);
        }
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locations.get(position);
        holder.textViewName.setText(location.getName());
        holder.textViewCoordinates.setText(location.getGpsCoordinates());
        holder.textViewTimestamp.setText(location.getTimeDateAdded());
        (holder).locationButton.setOnClickListener(v -> onLocationClick.onButtonClick(location.getName(), location.getGpsCoordinates(), location.getTimeDateAdded()));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }
}