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

public class CocktailAdapter extends RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder> {
    public List<Cocktail> cocktails;
    public List<Cocktail> allCocktails;
    interface OnCocktailCheckListener {
        void onButtonClick(String cocktailName, String cocktailBase, String cocktailMixer, String cocktailJuice, String cocktailLiqueur,
                           String cocktailGarnish, String cocktailExtra, Uri cocktailPhoto, String cocktailNotes, String cocktailRating, String cocktailKeywords);
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
        (holder).cocktailButton.setOnClickListener(v -> onCocktailClick.onButtonClick(cocktail.getName(), cocktail.getBase(), cocktail.getMixer(), cocktail.getJuice(),
                cocktail.getLiqueur(), cocktail.getGarnish(), cocktail.getExtra(), cocktail.getPhotoUri(), cocktail.getNotes(), cocktail.getRating(), cocktail.getKeywords()));
    }
    @Override
    public int getItemCount() { return cocktails.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void setCocktails(List<Cocktail> cocktails) {
        this.cocktails = new ArrayList<>(cocktails);
        this.allCocktails.clear();
        this.allCocktails.addAll(cocktails);
        notifyDataSetChanged();
    }
}
