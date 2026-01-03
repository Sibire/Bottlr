package com.bottlr.app.ui.details

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
class DetailsViewModel @Inject constructor(
    private val bottleRepository: BottleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bottleId: Long = savedStateHandle.get<Long>("bottleId") ?: -1L

    // Reactive bottle data from Room
    val bottle: StateFlow<BottleEntity?> = bottleRepository.getBottleById(bottleId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Delete status
    private val _deleteStatus = MutableStateFlow<DeleteStatus>(DeleteStatus.Idle)
    val deleteStatus: StateFlow<DeleteStatus> = _deleteStatus.asStateFlow()

    fun deleteBottle() {
        viewModelScope.launch {
            try {
                _deleteStatus.value = DeleteStatus.Deleting
                bottle.value?.let {
                    bottleRepository.delete(it)
                    _deleteStatus.value = DeleteStatus.Success
                }
            } catch (e: Exception) {
                _deleteStatus.value = DeleteStatus.Error(e.message ?: "Failed to delete")
            }
        }
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = DeleteStatus.Idle
    }
}

sealed class DeleteStatus {
    object Idle : DeleteStatus()
    object Deleting : DeleteStatus()
    object Success : DeleteStatus()
    data class Error(val message: String) : DeleteStatus()
}
