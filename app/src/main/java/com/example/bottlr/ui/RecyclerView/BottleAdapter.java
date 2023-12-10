package com.example.bottlr.ui.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bottlr.Bottle; // Replace with your actual Bottle class package
import com.example.bottlr.R;

import java.util.List;

public class BottleAdapter extends RecyclerView.Adapter<BottleAdapter.BottleViewHolder> {
    private List<Bottle> bottles;

    // Constructor to initialize the bottles list
    public BottleAdapter(List<Bottle> bottles) {
        this.bottles = bottles;
    }

    @Override
    public BottleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottlelabel, parent, false);
        return new BottleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BottleViewHolder holder, int position) {
        Bottle bottle = bottles.get(position);
        holder.textViewBottleName.setText(bottle.getName());
        holder.textViewDistillery.setText(bottle.getDistillery());
        // Set other fields and image
    }

    @Override
    public int getItemCount() {
        return bottles.size();
    }

    public static class BottleViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewBottle;
        public TextView textViewBottleName, textViewDistillery;
        // Other views

        public BottleViewHolder(View itemView) {
            super(itemView);
            imageViewBottle = itemView.findViewById(R.id.imageViewBottle);
            textViewBottleName = itemView.findViewById(R.id.textViewBottleName);
            textViewDistillery = itemView.findViewById(R.id.textViewDistillery);
            // Initialize other views
        }
    }
    public void setBottles(List<Bottle> newBottles) {
        this.bottles = newBottles;
    }
}
