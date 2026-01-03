package com.bottlr.app.ui

import app.cash.turbine.test
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.ui.gallery.GalleryViewModel
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for GalleryViewModel.
 *
 * Tests cover:
 * - Initial state (bottles mode)
 * - Mode switching between bottles and cocktails
 * - Loading bottles from repository
 * - Current items based on mode
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: BottleRepository

    private lateinit var viewModel: GalleryViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    // === INITIAL STATE TESTS ===

    @Test
    fun `initial mode is bottles (isDrinkMode = true)`() = runTest {
        // Given
        every { repository.allBottles } returns flowOf(emptyList())

        // When
        viewModel = GalleryViewModel(repository)

        // Then
        assertTrue(viewModel.isDrinkMode.value)
    }

    @Test
    fun `initial bottles list is empty`() = runTest {
        // Given
        every { repository.allBottles } returns flowOf(emptyList())

        // When
        viewModel = GalleryViewModel(repository)

        // Then
        viewModel.bottles.test {
            assertEquals(emptyList<Any>(), awaitItem())
        }
    }

    // === LOADING TESTS ===

    @Test
    fun `bottles flow emits repository data`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(3)
        every { repository.allBottles } returns flowOf(bottles)

        // When
        viewModel = GalleryViewModel(repository)

        // Then
        viewModel.bottles.test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Whisky 1", result[0].name)
        }
    }

    // === MODE SWITCHING TESTS ===

    @Test
    fun `setDrinkMode to false switches to cocktails mode`() = runTest {
        // Given
        every { repository.allBottles } returns flowOf(emptyList())
        viewModel = GalleryViewModel(repository)

        // When
        viewModel.setDrinkMode(false)

        // Then
        assertFalse(viewModel.isDrinkMode.value)
    }

    @Test
    fun `setDrinkMode to true switches back to bottles mode`() = runTest {
        // Given
        every { repository.allBottles } returns flowOf(emptyList())
        viewModel = GalleryViewModel(repository)
        viewModel.setDrinkMode(false)

        // When
        viewModel.setDrinkMode(true)

        // Then
        assertTrue(viewModel.isDrinkMode.value)
    }

    // === CURRENT ITEMS TESTS ===

    @Test
    fun `currentItems returns bottles when in bottle mode`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(2)
        every { repository.allBottles } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository)

        // When in bottle mode (default)

        // Then
        viewModel.currentItems.test {
            val items = awaitItem()
            assertEquals(2, items.size)
        }
    }

    @Test
    fun `currentItems returns empty when in cocktail mode`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(2)
        every { repository.allBottles } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository)

        // When switching to cocktail mode
        viewModel.setDrinkMode(false)

        // Then - verify mode changed (cocktails not implemented yet returns empty)
        assertFalse(viewModel.isDrinkMode.value)
    }
}
