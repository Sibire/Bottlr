package com.example.bottlr.ui.RecyclerView;

//region Imports
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
//endregion

// Bottle adapter code for the recycler

public class BottleAdapter extends RecyclerView.Adapter<BottleAdapter.BottleViewHolder> {
    private List<Bottle> bottles;
    private OnBottleListener onBottleListener;

    public BottleAdapter(List<Bottle> bottles, OnBottleListener onBottleListener) {
        this.bottles = bottles;
        this.onBottleListener = onBottleListener;
        // For handling taps on bottles
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

        if (bottle.getPhotoUri() != null && !bottle.getPhotoUri().toString().equals("No photo")) {
            Uri imageUri = Uri.parse(bottle.getPhotoUri().toString());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .error(R.drawable.nodrinkimg) // Using default image for none uploaded
                    .into(holder.imageViewBottle);
        } else {
            holder.imageViewBottle.setImageResource(R.drawable.nodrinkimg);
        }
    }

    // Counting bottles
    @Override
    public int getItemCount() {
        return bottles.size();
    }

    // Added more bottle methods
    // Double-check these later
    public void setBottles(List<Bottle> bottles) {
        this.bottles = bottles;
    }

    public Bottle getBottle(int position) {
        if (position >= 0 && position < bottles.size()) {
            return bottles.get(position);
        }
        return null;
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

        // Code for handling taps
        @Override
        public void onClick(View view) {
            onBottleListener.onBottleClick(getAdapterPosition());
        }
    }

    // Code for handling taps
    public interface OnBottleListener {
        void onBottleClick(int position);
    }

}
