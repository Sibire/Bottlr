package com.example.bottlr.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bottlr.R
import com.example.bottlr.data.local.entities.BottleEntity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * BottleDetailsFragment - Display full bottle information
 *
 * Uses DetailsViewModel for:
 * - Reactive bottle data (auto-updates!)
 * - Delete functionality with Firestore sync
 */
@AndroidEntryPoint
class BottleDetailsFragment : Fragment() {

    private val viewModel: DetailsViewModel by viewModels()

    // UI elements
    private lateinit var bottleImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var distilleryText: TextView
    private lateinit var typeText: TextView
    private lateinit var abvText: TextView
    private lateinit var ageText: TextView
    private lateinit var regionText: TextView
    private lateinit var ratingText: TextView
    private lateinit var notesText: TextView
    private lateinit var keywordsText: TextView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var shareButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.description_screen, container, false)

        // Initialize UI elements
        bottleImage = view.findViewById(R.id.bottleimage)
        nameText = view.findViewById(R.id.bottlename)
        distilleryText = view.findViewById(R.id.distiller)
        typeText = view.findViewById(R.id.spirittype)
        abvText = view.findViewById(R.id.abv)
        ageText = view.findViewById(R.id.age)
        regionText = view.findViewById(R.id.region)
        ratingText = view.findViewById(R.id.rating)
        notesText = view.findViewById(R.id.notes)
        keywordsText = view.findViewById(R.id.keywords)
        editButton = view.findViewById(R.id.editButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        shareButton = view.findViewById(R.id.shareButton)

        setupObservers()
        setupClickListeners()

        return view
    }

    private fun setupObservers() {
        // Observe bottle data (reactive!)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottle.collectLatest { bottle ->
                bottle?.let { displayBottle(it) }
            }
        }

        // Observe delete status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteStatus.collectLatest { status ->
                when (status) {
                    is DeleteStatus.Deleting -> {
                        deleteButton.isEnabled = false
                        deleteButton.text = "Deleting..."
                    }
                    is DeleteStatus.Success -> {
                        Snackbar.make(requireView(), "Bottle deleted", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is DeleteStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                        deleteButton.isEnabled = true
                        deleteButton.text = "Delete"
                    }
                    is DeleteStatus.Idle -> {
                        deleteButton.isEnabled = true
                        deleteButton.text = "Delete"
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        editButton.setOnClickListener {
            viewModel.bottle.value?.let { bottle ->
                findNavController().navigate(
                    R.id.action_details_to_editor,
                    bundleOf("bottleId" to bottle.id)
                )
            }
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }

        shareButton.setOnClickListener {
            shareBottle()
        }
    }

    private fun displayBottle(bottle: BottleEntity) {
        nameText.text = bottle.name.ifEmpty { "Unknown" }
        distilleryText.text = bottle.distillery.ifEmpty { "No distillery" }
        typeText.text = bottle.type.ifEmpty { "No type" }
        abvText.text = bottle.abv?.let { "${it}%" } ?: "N/A"
        ageText.text = bottle.age?.let { "$it years" } ?: "N/A"
        regionText.text = bottle.region.ifEmpty { "No region" }
        ratingText.text = bottle.rating?.toString() ?: "Not rated"
        notesText.text = bottle.notes.ifEmpty { "No notes" }
        keywordsText.text = bottle.keywords.ifEmpty { "No keywords" }

        // Load photo
        bottle.photoUri?.let { uri ->
            Glide.with(this)
                .load(Uri.parse(uri))
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(bottleImage)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Bottle")
            .setMessage("Are you sure you want to delete this bottle? This will also remove it from cloud storage.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteBottle()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareBottle() {
        viewModel.bottle.value?.let { bottle ->
            val text = buildString {
                append("Check out this bottle!\n\n")
                append("${bottle.name}\n")
                append("Distillery: ${bottle.distillery}\n")
                bottle.abv?.let { append("ABV: $it%\n") }
                bottle.age?.let { append("Age: $it years\n") }
                bottle.rating?.let { append("Rating: $it/10\n") }
                if (bottle.notes.isNotEmpty()) {
                    append("\nNotes: ${bottle.notes}")
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(intent, "Share Bottle"))
        }
    }
}
