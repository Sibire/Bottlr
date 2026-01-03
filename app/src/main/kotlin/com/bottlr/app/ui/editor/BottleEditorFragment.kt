package com.bottlr.app.ui.editor

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bottlr.app.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * BottleEditorFragment - Add or edit a bottle
 *
 * Uses EditorViewModel which:
 * - Preserves state across configuration changes (rotation!)
 * - Handles photo URI persistence
 * - Manages save/update operations
 * - Syncs to Firestore automatically
 */
@AndroidEntryPoint
class BottleEditorFragment : Fragment() {

    private val viewModel: EditorViewModel by viewModels()

    // UI fields
    private lateinit var nameField: EditText
    private lateinit var distilleryField: EditText
    private lateinit var typeField: EditText
    private lateinit var abvField: EditText
    private lateinit var ageField: EditText
    private lateinit var notesField: EditText
    private lateinit var regionField: EditText
    private lateinit var keywordsField: EditText
    private lateinit var ratingField: EditText
    private lateinit var photoPreview: ImageView
    private lateinit var saveButton: Button
    private lateinit var addPhotoButton: Button

    private var tempPhotoUri: Uri? = null

    // Modern Activity Result API (replaces deprecated startActivityForResult)
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setPhotoUri(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.setPhotoUri(tempPhotoUri!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.addbottlewindow, container, false)

        // Initialize UI fields
        nameField = view.findViewById(R.id.bottleNameField)
        distilleryField = view.findViewById(R.id.distillerField)
        typeField = view.findViewById(R.id.spiritTypeField)
        abvField = view.findViewById(R.id.abvField)
        ageField = view.findViewById(R.id.ageField)
        notesField = view.findViewById(R.id.tastingNotesField)
        regionField = view.findViewById(R.id.regionField)
        keywordsField = view.findViewById(R.id.keywordsField)
        ratingField = view.findViewById(R.id.ratingField)
        photoPreview = view.findViewById(R.id.imagePreview)
        saveButton = view.findViewById(R.id.saveButton)
        addPhotoButton = view.findViewById(R.id.addPhotoButton)

        setupObservers()
        setupClickListeners()

        return view
    }

    private fun setupObservers() {
        // Observe bottle if editing (auto-populates fields!)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bottle.collectLatest { bottle ->
                bottle?.let { populateFields(it) }
            }
        }

        // Observe photo URI (survives rotation!)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.photoUri.collectLatest { uri ->
                uri?.let {
                    Glide.with(this@BottleEditorFragment)
                        .load(it)
                        .into(photoPreview)
                }
            }
        }

        // Observe save status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveStatus.collectLatest { status ->
                when (status) {
                    is SaveStatus.Saving -> {
                        saveButton.isEnabled = false
                        saveButton.text = "Saving..."
                    }
                    is SaveStatus.Success -> {
                        Snackbar.make(requireView(), "Bottle saved successfully!", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is SaveStatus.Error -> {
                        Snackbar.make(requireView(), "Error: ${status.message}", Snackbar.LENGTH_LONG).show()
                        saveButton.isEnabled = true
                        saveButton.text = "Save"
                    }
                    is SaveStatus.Idle -> {
                        saveButton.isEnabled = true
                        saveButton.text = "Save"
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveBottle()
        }

        addPhotoButton.setOnClickListener {
            showPhotoDialog()
        }
    }

    private fun saveBottle() {
        val name = nameField.text.toString().trim()
        if (name.isEmpty()) {
            Snackbar.make(requireView(), "Name is required", Snackbar.LENGTH_SHORT).show()
            return
        }

        viewModel.saveBottle(
            name = name,
            distillery = distilleryField.text.toString(),
            type = typeField.text.toString(),
            abv = abvField.text.toString().toFloatOrNull(),
            age = ageField.text.toString().toIntOrNull(),
            notes = notesField.text.toString(),
            region = regionField.text.toString(),
            keywords = keywordsField.text.toString(),
            rating = ratingField.text.toString().toFloatOrNull()
        )
    }

    private fun showPhotoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(arrayOf("Take Photo", "Choose from Gallery", "Cancel")) { dialog, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "bottle_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        tempPhotoUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        tempPhotoUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    private fun populateFields(bottle: com.bottlr.app.data.local.entities.BottleEntity) {
        nameField.setText(bottle.name)
        distilleryField.setText(bottle.distillery)
        typeField.setText(bottle.type)
        abvField.setText(bottle.abv?.toString() ?: "")
        ageField.setText(bottle.age?.toString() ?: "")
        notesField.setText(bottle.notes)
        regionField.setText(bottle.region)
        keywordsField.setText(bottle.keywords)
        ratingField.setText(bottle.rating?.toString() ?: "")
    }
}
