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

class BottleAdapter(
    private var bottles: MutableList<Bottle> = mutableListOf(),
    private var allBottles: MutableList<Bottle> = mutableListOf(),
    private val onBottleClick: OnBottleCheckListener
) : RecyclerView.Adapter<BottleAdapter.BottleViewHolder>() {

    fun interface OnBottleCheckListener {
        fun onButtonClick(
            bottleName: String,
            bottleId: String?,
            bottleDistillery: String,
            bottleType: String,
            bottleABV: String,
            bottleAge: String,
            bottlePhoto: Uri?,
            bottleNotes: String,
            bottleRegion: String,
            bottleRating: String,
            bottleKeywords: String
        )
    }

    class BottleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewBottle: ImageView = itemView.findViewById(R.id.imageViewBottle)
        val textViewBottleName: TextView = itemView.findViewById(R.id.textViewBottleName)
        val textViewDistillery: TextView = itemView.findViewById(R.id.textViewDistillery)
        val bottleButton: Button = itemView.findViewById(R.id.bottlesinglebutton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bottlelabel, parent, false)
        return BottleViewHolder(view)
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        val bottle = bottles[position]
        holder.textViewBottleName.text = bottle.name
        holder.textViewDistillery.text = bottle.distillery
        bottle.photoUri?.let {
            if (it.toString().isNotEmpty() && it.toString() != "No photo") {
                Glide.with(holder.itemView.context)
                    .load(it)
                    .error(R.drawable.nodrinkimg)
                    .into(holder.imageViewBottle)
            } else {
                holder.imageViewBottle.setImageResource(R.drawable.nodrinkimg)
            }
        } ?: holder.imageViewBottle.setImageResource(R.drawable.nodrinkimg)

        holder.bottleButton.setOnClickListener {
            onBottleClick.onButtonClick(
                bottle.name,
                bottle.bottleID,
                bottle.distillery,
                bottle.type,
                bottle.abv,
                bottle.age,
                bottle.photoUri,
                bottle.notes,
                bottle.region,
                bottle.rating,
                bottle.keywords
            )
        }
    }

    override fun getItemCount(): Int = bottles.size

    fun updateData(newBottles: List<Bottle>) {
        Log.d("Search", "Updating bottle adapter with ${newBottles.size} items")
        this.bottles = newBottles.toMutableList()
        notifyDataSetChanged()
    }

    fun setBottles(bottles: List<Bottle>) {
        this.bottles = bottles.toMutableList()
        this.allBottles.clear()
        this.allBottles.addAll(bottles)
        notifyDataSetChanged()
    }
}
