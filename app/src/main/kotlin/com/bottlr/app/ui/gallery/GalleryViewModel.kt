package com.bottlr.app.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.data.repository.BottleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val bottleRepository: BottleRepository
    // TODO: Inject CocktailRepository when created
) : ViewModel() {

    // State: Are we viewing bottles (true) or cocktails (false)?
    private val _isDrinkMode = MutableStateFlow(true)
    val isDrinkMode: StateFlow<Boolean> = _isDrinkMode.asStateFlow()

    // Bottles from Room database (reactive with Flow)
    val bottles: StateFlow<List<BottleEntity>> = bottleRepository.allBottles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // TODO: Add cocktails flow when CocktailRepository is created
    // val cocktails: StateFlow<List<CocktailEntity>> = cocktailRepository.allCocktails
    //     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDrinkMode(isBottle: Boolean) {
        _isDrinkMode.value = isBottle
    }

    // Computed property: current list to display based on mode
    val currentItems: StateFlow<List<Any>> = combine(
        _isDrinkMode,
        bottles
        // TODO: Add cocktails flow here
    ) { isBottle, bottlesList ->
        if (isBottle) bottlesList else emptyList() // TODO: return cocktailsList when ready
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
