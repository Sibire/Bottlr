package com.bottlr.app.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bottlr.app.Bottle
import com.bottlr.app.BottleAdapter
import com.bottlr.app.R
import com.bottlr.app.data.local.entities.BottleEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * BottleGalleryFragment - Displays the liquor cabinet
 *
 * Uses GalleryViewModel for reactive data
 * Shows bottles in a grid layout
 * Navigates to details when bottle clicked
 * Navigates to editor when FAB clicked
 */
@AndroidEntryPoint
class BottleGalleryFragment : Fragment() {

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var adapter: BottleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var searchButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.liquorRecycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Setup adapter with click listener
        adapter = BottleAdapter(
            mutableListOf(),
            mutableListOf()
        ) { bottleName, _, _, _, _, _, _, _, _, _, _ ->
            // Navigate to details with bottle name
            navigateToDetails(bottleName)
        }
        recyclerView.adapter = adapter

        // Setup FAB to add new bottle
        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            findNavController().navigate(
                R.id.action_gallery_to_editor,
                bundleOf("bottleId" to -1L) // -1 = new bottle
            )
        }

        // Setup search button
        searchButton = view.findViewById(R.id.search_liquor_button)
        searchButton.setOnClickListener {
            findNavController().navigate(R.id.action_gallery_to_search)
        }

        // Observe bottles from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottles.collectLatest { bottleEntities ->
                // Convert BottleEntity to legacy Bottle for adapter
                val bottles = bottleEntities.map { convertToLegacyBottle(it) }
                adapter.updateData(bottles)
            }
        }

        return view
    }

    private fun navigateToDetails(bottleName: String) {
        // For now, navigate with name - ideally we'd pass the ID
        // This is a temporary solution until we update the adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottles.value.find { it.name == bottleName }?.let { bottle ->
                findNavController().navigate(
                    R.id.action_gallery_to_details,
                    bundleOf("bottleId" to bottle.id)
                )
            }
        }
    }

    /**
     * Convert new BottleEntity to legacy Bottle for existing adapter
     * TODO: Update adapter to work directly with BottleEntity
     */
    private fun convertToLegacyBottle(entity: BottleEntity): Bottle {
        return Bottle(
            entity.name,
            entity.distillery,
            entity.type,
            entity.abv?.toString() ?: "",
            entity.age?.toString() ?: "",
            entity.photoUri?.let { Uri.parse(it) },
            entity.notes,
            entity.region,
            entity.keywords,
            entity.rating?.toString() ?: ""
        ).apply {
            bottleID = entity.id.toString()
        }
    }
}
