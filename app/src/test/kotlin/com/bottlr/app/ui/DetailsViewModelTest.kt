package com.bottlr.app.ui

import androidx.lifecycle.SavedStateHandle
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.ui.details.DeleteStatus
import com.bottlr.app.ui.details.DetailsViewModel
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for DetailsViewModel.
 *
 * Tests cover:
 * - Loading bottle by ID
 * - Delete operations
 * - Delete status state transitions
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: BottleRepository

    private lateinit var viewModel: DetailsViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createViewModel(bottleId: Long = 1L): DetailsViewModel {
        val savedState = SavedStateHandle(mapOf("bottleId" to bottleId))
        return DetailsViewModel(repository, savedState)
    }

    // === LOADING TESTS ===

    @Test
    fun `loads bottle from repository`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 42L, name = "Test Bottle")
        every { repository.getBottleById(42L) } returns flowOf(bottle)

        // When
        viewModel = createViewModel(bottleId = 42L)
        advanceUntilIdle()

        // Then - collect first value
        val loaded = viewModel.bottle.first()
        assertEquals("Test Bottle", loaded?.name)
    }

    @Test
    fun `bottle is null when ID not found`() = runTest {
        // Given
        every { repository.getBottleById(999L) } returns flowOf(null)

        // When
        viewModel = createViewModel(bottleId = 999L)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.bottle.value)
    }

    // === DELETE TESTS ===

    @Test
    fun `initial delete status is Idle`() = runTest {
        // Given
        every { repository.getBottleById(any()) } returns flowOf(null)

        // When
        viewModel = createViewModel()

        // Then
        assertEquals(DeleteStatus.Idle, viewModel.deleteStatus.value)
    }

    @Test
    fun `deleteBottle calls repository delete`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        every { repository.getBottleById(1L) } returns flowOf(bottle)
        coEvery { repository.delete(any()) } just Runs

        viewModel = createViewModel(bottleId = 1L)

        // Subscribe to bottle to trigger the StateFlow
        val loadedBottle = viewModel.bottle.first { it != null }
        assertNotNull(loadedBottle)

        // When
        viewModel.deleteBottle()
        advanceUntilIdle()

        // Then
        coVerify { repository.delete(match { it.id == 1L }) }
    }

    @Test
    fun `deleteBottle sets Success status`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        every { repository.getBottleById(1L) } returns flowOf(bottle)
        coEvery { repository.delete(any()) } just Runs

        viewModel = createViewModel(bottleId = 1L)

        // Subscribe to bottle to trigger the StateFlow
        viewModel.bottle.first { it != null }

        // When
        viewModel.deleteBottle()
        advanceUntilIdle()

        // Then
        assertEquals(DeleteStatus.Success, viewModel.deleteStatus.value)
    }

    @Test
    fun `deleteBottle with error sets Error status`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        every { repository.getBottleById(1L) } returns flowOf(bottle)
        coEvery { repository.delete(any()) } throws RuntimeException("Delete failed")

        viewModel = createViewModel(bottleId = 1L)

        // Subscribe to bottle to trigger the StateFlow
        viewModel.bottle.first { it != null }

        // When
        viewModel.deleteBottle()
        advanceUntilIdle()

        // Then
        val status = viewModel.deleteStatus.value
        assertTrue(status is DeleteStatus.Error)
        assertEquals("Delete failed", (status as DeleteStatus.Error).message)
    }

    @Test
    fun `deleteBottle does nothing when bottle is null`() = runTest {
        // Given
        every { repository.getBottleById(999L) } returns flowOf(null)
        viewModel = createViewModel(bottleId = 999L)
        advanceUntilIdle()

        // When
        viewModel.deleteBottle()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { repository.delete(any()) }
    }

    @Test
    fun `resetDeleteStatus sets status to Idle`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        every { repository.getBottleById(1L) } returns flowOf(bottle)
        coEvery { repository.delete(any()) } just Runs

        viewModel = createViewModel(bottleId = 1L)

        // Subscribe to bottle to trigger the StateFlow
        viewModel.bottle.first { it != null }

        viewModel.deleteBottle()
        advanceUntilIdle()
        assertEquals(DeleteStatus.Success, viewModel.deleteStatus.value)

        // When
        viewModel.resetDeleteStatus()

        // Then
        assertEquals(DeleteStatus.Idle, viewModel.deleteStatus.value)
    }
}
