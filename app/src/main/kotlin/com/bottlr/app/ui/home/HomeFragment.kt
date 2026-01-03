package com.bottlr.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bottlr.app.R
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

    private var navWindow: View? = null
    private var isNavOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.homescreen, container, false)

        navWindow = view.findViewById(R.id.nav_window)

        // Menu button opens/closes nav drawer
        view.findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            toggleNavWindow()
        }

        // Exit button closes nav drawer
        view.findViewById<View>(R.id.exit_nav_button)?.setOnClickListener {
            closeNavWindow()
        }

        // Home button in nav
        view.findViewById<View>(R.id.menu_home_button)?.setOnClickListener {
            closeNavWindow()
        }

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

        // FAB navigates to add bottle
        view.findViewById<View>(R.id.fab)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_gallery)
        }

        return view
    }

    private fun toggleNavWindow() {
        if (isNavOpen) closeNavWindow() else openNavWindow()
    }

    private fun openNavWindow() {
        navWindow?.animate()
            ?.translationX(0f)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = true
    }

    private fun closeNavWindow() {
        navWindow?.animate()
            ?.translationX(-420f * resources.displayMetrics.density)
            ?.setDuration(300)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.start()
        isNavOpen = false
    }
}
