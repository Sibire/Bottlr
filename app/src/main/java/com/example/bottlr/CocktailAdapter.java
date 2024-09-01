package com.example.bottlr;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
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

public class CocktailAdapter extends RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder> {
    private List<Cocktail> cocktails;
    private List<Cocktail> allCocktails;

    public interface OnCocktailCheckListener {
        void onButtonClick(Cocktail cocktail);
    }

    @NonNull
    private final OnCocktailCheckListener onCocktailClick;

    public CocktailAdapter(List<Cocktail> cocktails, List<Cocktail> allCocktails, @NonNull OnCocktailCheckListener onCocktailCheckListener) {
        this.cocktails = cocktails != null ? cocktails : new ArrayList<>(); // Ensure cocktails is not null
        this.allCocktails = allCocktails != null ? allCocktails : new ArrayList<>(); // Ensure allCocktails is not null
        this.onCocktailClick = onCocktailCheckListener;
    }

    public static class CocktailViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCocktail;
        TextView textViewCocktailName, textViewBase;
        Button cocktailButton;

        public CocktailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCocktail = itemView.findViewById(R.id.imageViewCocktail);
            textViewCocktailName = itemView.findViewById(R.id.textViewCocktailName);
            textViewBase = itemView.findViewById(R.id.textViewBase);
            cocktailButton = itemView.findViewById(R.id.cocktailsinglebutton);
        }
    }

    @NonNull
    @Override
    public CocktailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cocktaillabel, viewGroup, false);
        return new CocktailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CocktailViewHolder holder, int position) {
        Cocktail cocktail = cocktails.get(position);
        holder.textViewCocktailName.setText(cocktail.getName());
        holder.textViewBase.setText(cocktail.getBase());
        if (cocktail.getPhotoUri() != null && !cocktail.getPhotoUri().toString().equals("No photo")) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(cocktail.getPhotoUri().toString()))
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageViewCocktail);
        } else {
            holder.imageViewCocktail.setImageResource(R.drawable.nodrinkimg);
        }
        holder.cocktailButton.setOnClickListener(v -> onCocktailClick.onButtonClick(cocktail));
    }

    @Override
    public int getItemCount() {
        return cocktails.size();
    }

    public void updateData(List<Cocktail> newCocktails) {
        Log.d("Search", "Updating cocktail adapter with " + newCocktails.size() + " items");
        this.cocktails = newCocktails;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCocktails(List<Cocktail> cocktails) {
        this.cocktails = new ArrayList<>(cocktails);
        this.allCocktails.clear();
        this.allCocktails.addAll(cocktails);
        notifyDataSetChanged();
    }
}