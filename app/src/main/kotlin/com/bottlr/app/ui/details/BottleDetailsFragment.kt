package com.bottlr.app.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bottlr.app.R
import com.bottlr.app.data.local.entities.BottleEntity
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

    // UI elements - matching description_screen.xml IDs
    private lateinit var bottleImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var distilleryText: TextView
    private lateinit var detailsText: TextView  // Combined type, region, age, ABV
    private lateinit var ratingText: TextView
    private lateinit var notesText: TextView
    private lateinit var keywordsText: TextView
    private lateinit var editButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var buyButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var nfcButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.description_screen, container, false)

        // Initialize UI elements with correct IDs from description_screen.xml
        bottleImage = view.findViewById(R.id.detailImageView)
        nameText = view.findViewById(R.id.tvBottleName)
        distilleryText = view.findViewById(R.id.tvDistillery)
        detailsText = view.findViewById(R.id.tvBottleDetails)
        ratingText = view.findViewById(R.id.tvRating)
        notesText = view.findViewById(R.id.tvNotes)
        keywordsText = view.findViewById(R.id.tvKeywords)
        editButton = view.findViewById(R.id.editButton)
        shareButton = view.findViewById(R.id.shareButton)
        buyButton = view.findViewById(R.id.buyButton)
        backButton = view.findViewById(R.id.backButton)
        nfcButton = view.findViewById(R.id.nfcButton)

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
                        editButton.isEnabled = false
                    }
                    is DeleteStatus.Success -> {
                        Snackbar.make(requireView(), "Bottle deleted", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is DeleteStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                        editButton.isEnabled = true
                    }
                    is DeleteStatus.Idle -> {
                        editButton.isEnabled = true
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

        shareButton.setOnClickListener {
            shareBottle()
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        buyButton.setOnClickListener {
            searchForBottle()
        }

        nfcButton.setOnClickListener {
            Snackbar.make(requireView(), "NFC sharing coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun searchForBottle() {
        viewModel.bottle.value?.let { bottle ->
            val query = "${bottle.name} ${bottle.distillery}".trim()
            val searchUri = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}+buy")
            startActivity(Intent(Intent.ACTION_VIEW, searchUri))
        }
    }

    private fun displayBottle(bottle: BottleEntity) {
        nameText.text = bottle.name.ifEmpty { "Unknown" }
        distilleryText.text = bottle.distillery.ifEmpty { "No distillery" }

        // Build combined details string matching the original layout format
        val type = bottle.type.ifEmpty { "No Type" }
        val region = bottle.region.ifEmpty { "No Region" }
        val age = bottle.age?.let { "$it Year" } ?: "No"
        val abv = bottle.abv?.let { "$it%" } ?: "N/A"
        detailsText.text = "$type, $region, $age, $abv ABV"

        ratingText.text = bottle.rating?.let { "$it / 10" } ?: "Not rated"
        notesText.text = bottle.notes.ifEmpty { "No notes" }
        keywordsText.text = "Keywords: ${bottle.keywords.ifEmpty { "None" }}"

        // Load photo
        bottle.photoUri?.let { uri ->
            Glide.with(this)
                .load(Uri.parse(uri))
                .placeholder(R.drawable.nodrinkimg)
                .error(R.drawable.nodrinkimg)
                .into(bottleImage)
        } ?: bottleImage.setImageResource(R.drawable.nodrinkimg)
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
