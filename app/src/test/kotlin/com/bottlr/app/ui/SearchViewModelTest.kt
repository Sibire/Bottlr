package com.bottlr.app.ui

import app.cash.turbine.test
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.ui.search.SearchViewModel
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SearchViewModel.
 *
 * Tests cover:
 * - Initial state
 * - Search field and query updates
 * - Search results based on field and query
 * - Mode switching (bottles vs cocktails)
 * - Clear search functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: BottleRepository

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Default stub for allBottles
        every { repository.allBottles } returns flowOf(emptyList())
    }

    // === INITIAL STATE TESTS ===

    @Test
    fun `initial search field is Name`() = runTest {
        viewModel = SearchViewModel(repository)
        assertEquals("Name", viewModel.searchField.value)
    }

    @Test
    fun `initial search query is empty`() = runTest {
        viewModel = SearchViewModel(repository)
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `initial mode is bottles (not cocktails)`() = runTest {
        viewModel = SearchViewModel(repository)
        assertFalse(viewModel.searchingCocktails.value)
    }

    // === SEARCH FUNCTION TESTS ===

    @Test
    fun `search updates field and query`() = runTest {
        // Given
        viewModel = SearchViewModel(repository)

        // When
        viewModel.search("Distillery", "Macallan")

        // Then
        assertEquals("Distillery", viewModel.searchField.value)
        assertEquals("Macallan", viewModel.searchQuery.value)
    }

    @Test
    fun `search with empty query returns all bottles`() = runTest {
        // Given
        val allBottles = TestFixtures.bottles(5)
        every { repository.allBottles } returns flowOf(allBottles)
        viewModel = SearchViewModel(repository)

        // When - no search query
        viewModel.search("Name", "")

        // Then
        viewModel.searchResults.test {
            val results = awaitItem()
            assertEquals(5, results.size)
        }
    }

    @Test
    fun `search with query returns filtered results`() = runTest {
        // Given
        val searchResults = listOf(TestFixtures.bottle(name = "Lagavulin 16"))
        every { repository.searchByField("Name", "Lagavulin") } returns flowOf(searchResults)
        viewModel = SearchViewModel(repository)

        // When - search and collect results (collection triggers the flow)
        viewModel.search("Name", "Lagavulin")

        // Then - verify results are returned
        viewModel.searchResults.test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Lagavulin 16", results[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search by different fields returns correct results`() = runTest {
        // Given
        val highlandBottles = listOf(
            TestFixtures.bottle(name = "Highland Park 12", distillery = "Highland Park")
        )
        every { repository.searchByField("Distillery", "Highland") } returns flowOf(highlandBottles)
        viewModel = SearchViewModel(repository)

        // When
        viewModel.search("Distillery", "Highland")

        // Then - verify results from distillery search
        viewModel.searchResults.test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Highland Park", results[0].distillery)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === MODE SWITCHING TESTS ===

    @Test
    fun `setSearchingCocktails switches to cocktail mode`() = runTest {
        // Given
        viewModel = SearchViewModel(repository)

        // When
        viewModel.setSearchingCocktails(true)

        // Then
        assertTrue(viewModel.searchingCocktails.value)
    }

    @Test
    fun `cocktail mode switches state correctly`() = runTest {
        // Given
        every { repository.allBottles } returns flowOf(TestFixtures.bottles(3))
        viewModel = SearchViewModel(repository)

        // When
        viewModel.setSearchingCocktails(true)

        // Then - verify state changed
        assertTrue(viewModel.searchingCocktails.value)
    }

    // === CLEAR SEARCH TESTS ===

    @Test
    fun `clearSearch resets query to empty`() = runTest {
        // Given
        viewModel = SearchViewModel(repository)
        viewModel.search("Name", "Test")
        assertEquals("Test", viewModel.searchQuery.value)

        // When
        viewModel.clearSearch()

        // Then
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `clearSearch preserves search field`() = runTest {
        // Given
        viewModel = SearchViewModel(repository)
        viewModel.search("Distillery", "Test")

        // When
        viewModel.clearSearch()

        // Then - field unchanged
        assertEquals("Distillery", viewModel.searchField.value)
    }
}
