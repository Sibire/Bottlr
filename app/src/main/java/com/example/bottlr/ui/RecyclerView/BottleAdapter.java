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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BottleAdapter extends RecyclerView.Adapter<BottleAdapter.BottleViewHolder> {
    private List<Bottle> bottles;
    private final List<Bottle> allBottles;
    private OnBottleListener onBottleListener;

    public BottleAdapter(List<Bottle> bottles, OnBottleListener onBottleListener) {
        this.bottles = new ArrayList<>(bottles);
        this.allBottles = new ArrayList<>(bottles); // Initialize with a copy of the bottles
        this.onBottleListener = onBottleListener;
    }

    @Override
    public BottleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottlelabel, parent, false);
        return new BottleViewHolder(view, onBottleListener);
    }

    @Override
    public void onBindViewHolder(BottleViewHolder holder, int position) {
        Bottle bottle = bottles.get(position);
        holder.textViewBottleName.setText(bottle.getName());
        holder.textViewDistillery.setText(bottle.getDistillery());
        // Any other fields as necessary in the future

        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(bottle.getPhotoUri().toString()))
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageViewBottle);
        } else {
            holder.imageViewBottle.setImageResource(R.drawable.nodrinkimg);
        }
    }

    @Override
    public int getItemCount() {
        return bottles.size();
    }

    public void setBottles(List<Bottle> bottles) {
        this.bottles = new ArrayList<>(bottles);
        this.allBottles.clear();
        this.allBottles.addAll(bottles);
        notifyDataSetChanged();
    }

    public Bottle getBottle(int position) {
        if (position >= 0 && position < bottles.size()) {
            return bottles.get(position);
        }
        return null; // Null if ouut of bounds
    }

    public void filter(String name, String distillery, String type, String abv, String age, String notes, String region, String rating, Set<String> keywords) {
        List<Bottle> filteredBottles = allBottles.stream()
                .filter(bottle -> bottle.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(bottle -> bottle.getDistillery().toLowerCase().contains(distillery.toLowerCase()))
                .filter(bottle -> bottle.getType().toLowerCase().contains(type.toLowerCase()))
                .filter(bottle -> bottle.getAbv().toLowerCase().contains(abv.toLowerCase()))
                .filter(bottle -> bottle.getAge().toLowerCase().contains(age.toLowerCase()))
                .filter(bottle -> bottle.getNotes().toLowerCase().contains(notes.toLowerCase()))
                .filter(bottle -> bottle.getRegion().toLowerCase().contains(region.toLowerCase()))
                .filter(bottle -> bottle.getRating().toLowerCase().contains(rating.toLowerCase()))
                .filter(bottle -> keywords.isEmpty() || bottle.getKeywords().stream().anyMatch(k -> keywords.contains(k.toLowerCase())))
                .collect(Collectors.toList());

        this.bottles = filteredBottles;
        notifyDataSetChanged();
    }

    public static class BottleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageViewBottle;
        TextView textViewBottleName, textViewDistillery;
        OnBottleListener onBottleListener;

        public BottleViewHolder(View itemView, OnBottleListener onBottleListener) {
            super(itemView);
            imageViewBottle = itemView.findViewById(R.id.imageViewBottle);
            textViewBottleName = itemView.findViewById(R.id.textViewBottleName);
            textViewDistillery = itemView.findViewById(R.id.textViewDistillery);
            this.onBottleListener = onBottleListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onBottleListener.onBottleClick(getAdapterPosition());
        }
    }

    public interface OnBottleListener {
        void onBottleClick(int position);
    }
}
