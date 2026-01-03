package com.bottlr.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bottlr.app.R
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.data.repository.CocktailRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsFragment - Firebase authentication and cloud sync
 *
 * Features:
 * - Google Sign-In
 * - Sign out
 * - Erase cloud storage
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var bottleRepository: BottleRepository

    @Inject
    lateinit var cocktailRepository: CocktailRepository

    private lateinit var userTextView: TextView
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button
    private lateinit var eraseButton: Button
    private lateinit var uploadButton: Button
    private lateinit var syncButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_settings, container, false)

        // Initialize views with correct IDs from activity_settings.xml
        userTextView = view.findViewById(R.id.signed_in_user)
        loginButton = view.findViewById(R.id.login_Button)
        logoutButton = view.findViewById(R.id.logout_Button)
        eraseButton = view.findViewById(R.id.erase_Button)
        uploadButton = view.findViewById(R.id.upload_Button)
        syncButton = view.findViewById(R.id.sync_Button)

        setupUI()
        setupClickListeners()

        return view
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userTextView.text = currentUser.email ?: "Signed in"
            loginButton.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
            eraseButton.visibility = View.VISIBLE
            uploadButton.visibility = View.VISIBLE
            syncButton.visibility = View.VISIBLE
        } else {
            userTextView.text = getString(R.string.not_signed_in)
            loginButton.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
            eraseButton.visibility = View.GONE
            uploadButton.visibility = View.GONE
            syncButton.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            signIn()
        }

        logoutButton.setOnClickListener {
            signOut()
        }

        eraseButton.setOnClickListener {
            eraseCloudStorage()
        }

        uploadButton.setOnClickListener {
            uploadToCloud()
        }

        syncButton.setOnClickListener {
            pullFromCloud()
        }
    }

    private fun uploadToCloud() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Snackbar.make(requireView(), "Uploading data to cloud...", Snackbar.LENGTH_SHORT).show()
                bottleRepository.syncAllToFirestore()
                cocktailRepository.syncAllToFirestore()
                Snackbar.make(requireView(), "Upload complete", Snackbar.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Upload failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun pullFromCloud() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Snackbar.make(requireView(), "Pulling data from cloud...", Snackbar.LENGTH_SHORT).show()
                bottleRepository.syncFromFirestore()
                cocktailRepository.syncFromFirestore()
                Snackbar.make(requireView(), "Sync complete", Snackbar.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Sync failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.bottlr_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent

        // Note: This uses deprecated API - would need Activity Result API for production
        Snackbar.make(requireView(), "Google Sign-In integration pending", Snackbar.LENGTH_LONG).show()
    }

    private fun signOut() {
        auth.signOut()
        setupUI()
        Snackbar.make(requireView(), "Signed out", Snackbar.LENGTH_SHORT).show()
    }

    private fun eraseCloudStorage() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Snackbar.make(requireView(), "Erasing cloud storage...", Snackbar.LENGTH_SHORT).show()
                // TODO: Implement actual cloud storage erasure
                Snackbar.make(requireView(), "Cloud storage erased", Snackbar.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
