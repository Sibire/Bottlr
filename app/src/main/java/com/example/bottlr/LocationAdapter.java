package com.example.bottlr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private List<Location> locations;

    public LocationAdapter(List<Location> locations) {
        this.locations = locations;
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewCoordinates, textViewTimestamp;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCoordinates = itemView.findViewById(R.id.textViewCoordinates);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
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
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }
}