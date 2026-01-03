package com.bottlr.app.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.repository.BottleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val bottleRepository: BottleRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get bottleId from navigation args (-1 means new bottle)
    private val bottleId: Long = savedStateHandle.get<Long>("bottleId") ?: -1L

    // Photo URI state (survives configuration changes!)
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    // Current bottle being edited (null for new bottle)
    private val _bottle = MutableStateFlow<BottleEntity?>(null)
    val bottle: StateFlow<BottleEntity?> = _bottle.asStateFlow()

    // Save status for UI feedback
    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    val isEditMode: Boolean = bottleId != -1L

    init {
        if (isEditMode) {
            viewModelScope.launch {
                bottleRepository.getBottleById(bottleId).collectLatest { bottleEntity ->
                    _bottle.value = bottleEntity
                    // Restore photo URI if exists
                    bottleEntity?.photoUri?.let { uri ->
                        _photoUri.value = Uri.parse(uri)
                    }
                }
            }
        }
    }

    fun setPhotoUri(uri: Uri) {
        _photoUri.value = uri
    }

    fun saveBottle(
        name: String,
        distillery: String,
        type: String,
        abv: Float?,
        age: Int?,
        notes: String,
        region: String,
        keywords: String,
        rating: Float?
    ) {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving

                val bottle = BottleEntity(
                    id = if (isEditMode) bottleId else 0,
                    name = name,
                    distillery = distillery,
                    type = type,
                    abv = abv,
                    age = age,
                    photoUri = _photoUri.value?.toString(),
                    notes = notes,
                    region = region,
                    keywords = keywords,
                    rating = rating
                )

                val id = if (isEditMode) {
                    bottleRepository.update(bottle)
                    bottleId
                } else {
                    bottleRepository.insert(bottle)
                }

                _saveStatus.value = SaveStatus.Success

                // Sync to Firestore in background (non-blocking)
                try {
                    bottleRepository.syncToFirestore(id)
                } catch (e: Exception) {
                    // Sync failure shouldn't block local save
                }
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Failed to save bottle")
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = SaveStatus.Idle
    }
}

sealed class SaveStatus {
    object Idle : SaveStatus()
    object Saving : SaveStatus()
    object Success : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}
