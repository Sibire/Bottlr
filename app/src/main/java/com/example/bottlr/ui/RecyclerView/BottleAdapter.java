package com.example.bottlr.ui.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bottlr.Bottle;
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

        // Check if the photo URI is not null and not a placeholder string
        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Uri imageUri = Uri.parse(bottle.getPhotoUri().toString());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .error(R.drawable.nodrinkimg) // Set a default image in case of error
                    .into(holder.imageViewBottle);
        } else {
            holder.imageViewBottle.setImageResource(R.drawable.ic_launcher_background); // Default image
        }
    }

    @Override
    public int getItemCount() {
        return bottles.size();
    }

    public static class BottleViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewBottle;
        public TextView textViewBottleName, textViewDistillery;

        public BottleViewHolder(View itemView) {
            super(itemView);
            imageViewBottle = itemView.findViewById(R.id.imageViewBottle);
            textViewBottleName = itemView.findViewById(R.id.textViewBottleName);
            textViewDistillery = itemView.findViewById(R.id.textViewDistillery);
        }
    }

    public void setBottles(List<Bottle> newBottles) {
        this.bottles = newBottles;
    }
}
