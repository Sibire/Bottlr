package com.bottlr.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(
    private var locations: List<Location>,
    private var allLocations: List<Location>,
    private val onLocationClick: OnLocationCheckListener
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    fun interface OnLocationCheckListener {
        fun onButtonClick(name: String?, coordinates: String?, date: String?)
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewCoordinates: TextView = itemView.findViewById(R.id.textViewCoordinates)
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val locationButton: Button = itemView.findViewById(R.id.locationsinglebutton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.textViewName.text = location.name
        holder.textViewCoordinates.text = location.gpsCoordinates
        holder.textViewTimestamp.text = location.timeDateAdded
        holder.locationButton.setOnClickListener {
            onLocationClick.onButtonClick(location.name, location.gpsCoordinates, location.timeDateAdded)
        }
    }

    override fun getItemCount(): Int = locations.size
}
