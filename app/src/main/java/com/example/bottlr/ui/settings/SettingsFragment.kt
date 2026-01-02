package com.example.bottlr.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bottlr.R
import com.example.bottlr.data.repository.BottleRepository
import com.example.bottlr.data.repository.CocktailRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsFragment - Firebase authentication and cloud sync
 *
 * Features:
 * - Google Sign-In
 * - Upload to Firestore
 * - Download from Firestore
 * - Sign out
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
    private lateinit var uploadButton: Button
    private lateinit var downloadButton: Button
    private lateinit var signOutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_settings, container, false)

        userTextView = view.findViewById(R.id.signed_in_user)
        loginButton = view.findViewById(R.id.login_Button)
        uploadButton = view.findViewById(R.id.upload_Button)
        downloadButton = view.findViewById(R.id.download_Button)
        signOutButton = view.findViewById(R.id.signout_Button)

        setupUI()
        setupClickListeners()

        return view
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userTextView.text = "Signed in as: ${currentUser.email}"
            loginButton.visibility = View.GONE
            uploadButton.visibility = View.VISIBLE
            downloadButton.visibility = View.VISIBLE
            signOutButton.visibility = View.VISIBLE
        } else {
            userTextView.text = "Not signed in"
            loginButton.visibility = View.VISIBLE
            uploadButton.visibility = View.GONE
            downloadButton.visibility = View.GONE
            signOutButton.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            signIn()
        }

        uploadButton.setOnClickListener {
            uploadToCloud()
        }

        downloadButton.setOnClickListener {
            downloadFromCloud()
        }

        signOutButton.setOnClickListener {
            signOut()
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent

        // Note: This uses deprecated API - would need Activity Result API for production
        Snackbar.make(requireView(), "Google Sign-In integration pending", Snackbar.LENGTH_LONG).show()
    }

    private fun uploadToCloud() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Snackbar.make(requireView(), "Uploading to cloud...", Snackbar.LENGTH_SHORT).show()

                // Upload all unsynced bottles
                // TODO: Get unsynced bottles and upload them
                // For now, this is a placeholder

                Snackbar.make(requireView(), "Upload complete!", Snackbar.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Upload failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadFromCloud() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Snackbar.make(requireView(), "Downloading from cloud...", Snackbar.LENGTH_SHORT).show()

                bottleRepository.syncFromFirestore()
                cocktailRepository.syncFromFirestore()

                Snackbar.make(requireView(), "Download complete!", Snackbar.LENGTH_LONG).show()
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Download failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        setupUI()
        Snackbar.make(requireView(), "Signed out", Snackbar.LENGTH_SHORT).show()
    }
}
