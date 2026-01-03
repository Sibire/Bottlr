package com.bottlr.app

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CocktailAdapter(
    private var cocktails: MutableList<Cocktail> = mutableListOf(),
    private var allCocktails: MutableList<Cocktail> = mutableListOf(),
    private val onCocktailClick: OnCocktailCheckListener
) : RecyclerView.Adapter<CocktailAdapter.CocktailViewHolder>() {

    fun interface OnCocktailCheckListener {
        fun onButtonClick(cocktail: Cocktail)
    }

    class CocktailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCocktail: ImageView = itemView.findViewById(R.id.imageViewCocktail)
        val textViewCocktailName: TextView = itemView.findViewById(R.id.textViewCocktailName)
        val textViewBase: TextView = itemView.findViewById(R.id.textViewBase)
        val cocktailButton: Button = itemView.findViewById(R.id.cocktailsinglebutton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocktailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cocktaillabel, parent, false)
        return CocktailViewHolder(view)
    }

    override fun onBindViewHolder(holder: CocktailViewHolder, position: Int) {
        val cocktail = cocktails[position]
        holder.textViewCocktailName.text = cocktail.name
        holder.textViewBase.text = cocktail.base
        cocktail.photoUri?.let {
            if (it.toString().isNotEmpty() && it.toString() != "No photo") {
                Glide.with(holder.itemView.context)
                    .load(it)
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageViewCocktail)
            } else {
                holder.imageViewCocktail.setImageResource(R.drawable.nodrinkimg)
            }
        } ?: holder.imageViewCocktail.setImageResource(R.drawable.nodrinkimg)

        holder.cocktailButton.setOnClickListener { onCocktailClick.onButtonClick(cocktail) }
    }

    override fun getItemCount(): Int = cocktails.size

    fun updateData(newCocktails: List<Cocktail>) {
        Log.d("Search", "Updating cocktail adapter with ${newCocktails.size} items")
        this.cocktails = newCocktails.toMutableList()
        notifyDataSetChanged()
    }

    fun setCocktails(cocktails: List<Cocktail>) {
        this.cocktails = cocktails.toMutableList()
        this.allCocktails.clear()
        this.allCocktails.addAll(cocktails)
        notifyDataSetChanged()
    }
}
