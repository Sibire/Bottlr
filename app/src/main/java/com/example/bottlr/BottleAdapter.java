package com.example.bottlr;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BottleAdapter extends RecyclerView.Adapter<BottleAdapter.BottleViewHolder> {
    public List<Bottle> bottles;
    public List<Bottle> allBottles;
    interface OnBottleCheckListener {
        void onButtonClick(String bottleName, String bottleId, String bottleDistillery, String bottleType, String bottleABV, String bottleAge,
                           Uri bottlePhoto, String bottleNotes, String bottleRegion, String bottleRating, Set<String> bottleKeywords);
    }
    @NonNull
    private final OnBottleCheckListener onBottleClick;
    public BottleAdapter(List<Bottle> bottles, List<Bottle> allBottles, @NonNull OnBottleCheckListener onBottleCheckListener) {
        this.bottles = bottles != null ? bottles : new ArrayList<>(); // Ensure bottles is not null
        this.allBottles = allBottles != null ? allBottles : new ArrayList<>(); // Ensure allBottles is not null
        this.onBottleClick = onBottleCheckListener;
    }
    public static class BottleViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewBottle;
        TextView textViewBottleName, textViewDistillery;
        Button bottleButton;
        public BottleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewBottle = itemView.findViewById(R.id.imageViewBottle);
            textViewBottleName = itemView.findViewById(R.id.textViewBottleName);
            textViewDistillery = itemView.findViewById(R.id.textViewDistillery);
            bottleButton = itemView.findViewById(R.id.bottlesinglebutton);
        }
    }
    @NonNull
    @Override
    public BottleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bottlelabel, viewGroup, false);
        return new BottleViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final BottleViewHolder holder, int position) {
        Bottle bottle = bottles.get(position);
        holder.textViewBottleName.setText(bottle.getName());
        holder.textViewDistillery.setText(bottle.getDistillery());
        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(bottle.getPhotoUri().toString()))
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageViewBottle);
        } else {
            holder.imageViewBottle.setImageResource(R.drawable.nodrinkimg);
        }
        (holder).bottleButton.setOnClickListener(v -> onBottleClick.onButtonClick(bottle.getName(), bottle.getBottleID(), bottle.getDistillery(), bottle.getType(),
                bottle.getAbv(), bottle.getAge(), bottle.getPhotoUri(), bottle.getNotes(), bottle.getRegion(), bottle.getRating(), bottle.getKeywords()));
    }
    @Override
    public int getItemCount() { return bottles.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void setBottles(List<Bottle> bottles) {
        this.bottles = new ArrayList<>(bottles);
        this.allBottles.clear();
        this.allBottles.addAll(bottles);
        notifyDataSetChanged();
    }
}
