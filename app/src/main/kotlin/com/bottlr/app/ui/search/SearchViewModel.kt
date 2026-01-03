package com.bottlr.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.repository.BottleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bottleRepository: BottleRepository
    // TODO: Add CocktailRepository when created
) : ViewModel() {

    // Search state
    private val _searchingCocktails = MutableStateFlow(false)
    val searchingCocktails: StateFlow<Boolean> = _searchingCocktails.asStateFlow()

    private val _searchField = MutableStateFlow("Name")
    val searchField: StateFlow<String> = _searchField.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Reactive search results
    val searchResults: StateFlow<List<BottleEntity>> = combine(
        _searchField,
        _searchQuery,
        _searchingCocktails
    ) { field, query, isCocktail ->
        Triple(field, query, isCocktail)
    }.flatMapLatest { (field, query, isCocktail) ->
        if (isCocktail) {
            // TODO: Search cocktails when repository is ready
            flowOf(emptyList())
        } else {
            if (query.isBlank()) {
                bottleRepository.allBottles
            } else {
                bottleRepository.searchByField(field, query)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun search(field: String, query: String) {
        _searchField.value = field
        _searchQuery.value = query
    }

    fun setSearchingCocktails(value: Boolean) {
        _searchingCocktails.value = value
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
