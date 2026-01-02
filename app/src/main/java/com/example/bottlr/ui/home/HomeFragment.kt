package com.example.bottlr.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bottlr.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * HomeFragment - App's main menu/landing screen
 *
 * Provides navigation to:
 * - Liquor Cabinet (Bottle Gallery)
 * - Cocktail Maker (Cocktail Gallery)
 * - Settings
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.homescreen, container, false)

        // Setup navigation buttons
        view.findViewById<View>(R.id.menu_liquorcab_button)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_gallery)
        }

        view.findViewById<View>(R.id.menu_cocktail_button)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cocktails)
        }

        view.findViewById<View>(R.id.menu_settings_button)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        return view
    }
}
