package com.bottlr.app.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.ui.editor.EditorViewModel
import com.bottlr.app.ui.editor.SaveStatus
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for EditorViewModel.
 *
 * Tests cover:
 * - New bottle creation (insert path)
 * - Existing bottle editing (update path)
 * - Photo URI handling
 * - Save status state transitions
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: BottleRepository

    private lateinit var viewModel: EditorViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    // Helper to create ViewModel with specific bottle ID
    private fun createViewModel(bottleId: Long = -1L): EditorViewModel {
        val savedState = SavedStateHandle(mapOf("bottleId" to bottleId))
        return EditorViewModel(repository, savedState)
    }

    // === NEW BOTTLE TESTS ===

    @Test
    fun `new bottle - isEditMode is false`() {
        viewModel = createViewModel(bottleId = -1L)

        assertFalse(viewModel.isEditMode)
    }

    @Test
    fun `new bottle - initial state is Idle`() {
        viewModel = createViewModel()

        assertEquals(SaveStatus.Idle, viewModel.saveStatus.value)
    }

    @Test
    fun `saveBottle inserts new bottle and syncs to Firestore`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 42L
        coEvery { repository.syncToFirestore(42L) } just Runs
        viewModel = createViewModel()

        // When
        viewModel.saveBottle(
            name = "Lagavulin 16",
            distillery = "Lagavulin",
            type = "Single Malt",
            abv = 43f,
            age = 16,
            notes = "Smoky and peaty",
            region = "Islay",
            keywords = "peaty, smoky",
            rating = 9.0f
        )

        // Then
        viewModel.saveStatus.test {
            assertEquals(SaveStatus.Success, awaitItem())
        }
        coVerify { repository.insert(match { it.name == "Lagavulin 16" }) }
        coVerify { repository.syncToFirestore(42L) }
    }

    @Test
    fun `saveBottle ends in Success state`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.syncToFirestore(any()) } just Runs
        viewModel = createViewModel()

        // When
        viewModel.saveBottle(
            name = "Test", distillery = "", type = "",
            abv = null, age = null, notes = "", region = "",
            keywords = "", rating = null
        )

        // Then - verify final state is Success
        assertEquals(SaveStatus.Success, viewModel.saveStatus.value)
    }

    @Test
    fun `saveBottle with error sets Error status`() = runTest {
        // Given
        coEvery { repository.insert(any()) } throws RuntimeException("Database error")
        viewModel = createViewModel()

        // When
        viewModel.saveBottle(
            name = "Test", distillery = "", type = "",
            abv = null, age = null, notes = "", region = "",
            keywords = "", rating = null
        )

        // Then
        viewModel.saveStatus.test {
            val status = awaitItem()
            assertTrue(status is SaveStatus.Error)
            assertEquals("Database error", (status as SaveStatus.Error).message)
        }
    }

    // === EDIT MODE TESTS ===

    @Test
    fun `edit mode - isEditMode is true`() = runTest {
        // Given
        every { repository.getBottleById(42L) } returns flowOf(TestFixtures.bottle(id = 42L))

        // When
        viewModel = createViewModel(bottleId = 42L)

        // Then
        assertTrue(viewModel.isEditMode)
    }

    @Test
    fun `edit mode - loads existing bottle`() = runTest {
        // Given
        val existingBottle = TestFixtures.bottle(id = 42L, name = "Existing Whisky")
        every { repository.getBottleById(42L) } returns flowOf(existingBottle)

        // When
        viewModel = createViewModel(bottleId = 42L)

        // Then
        viewModel.bottle.test {
            val loaded = awaitItem()
            assertEquals("Existing Whisky", loaded?.name)
        }
    }

    @Test
    fun `edit mode - updates existing bottle`() = runTest {
        // Given
        val existingBottle = TestFixtures.bottle(id = 42L)
        every { repository.getBottleById(42L) } returns flowOf(existingBottle)
        coEvery { repository.update(any()) } just Runs
        coEvery { repository.syncToFirestore(42L) } just Runs

        viewModel = createViewModel(bottleId = 42L)

        // When
        viewModel.saveBottle(
            name = "Updated Name", distillery = "Updated Distillery",
            type = "Bourbon", abv = 50f, age = 10, notes = "Updated",
            region = "Kentucky", keywords = "updated", rating = 8.0f
        )

        // Then
        coVerify { repository.update(match { it.id == 42L && it.name == "Updated Name" }) }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    // === PHOTO URI TESTS ===

    @Test
    fun `setPhotoUri updates photoUri state`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>()

        // When
        viewModel.setPhotoUri(testUri)

        // Then
        assertEquals(testUri, viewModel.photoUri.value)
    }

    @Test
    fun `saveBottle includes photo URI`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.syncToFirestore(any()) } just Runs
        viewModel = createViewModel()

        val testUri = mockk<Uri>()
        every { testUri.toString() } returns "content://test/photo.jpg"
        viewModel.setPhotoUri(testUri)

        // When
        viewModel.saveBottle(
            name = "With Photo", distillery = "", type = "",
            abv = null, age = null, notes = "", region = "",
            keywords = "", rating = null
        )

        // Then
        coVerify {
            repository.insert(match { it.photoUri == "content://test/photo.jpg" })
        }
    }

    // === UTILITY TESTS ===

    @Test
    fun `clearSaveStatus resets to Idle`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.syncToFirestore(any()) } just Runs
        viewModel = createViewModel()

        viewModel.saveBottle(
            name = "Test", distillery = "", type = "",
            abv = null, age = null, notes = "", region = "",
            keywords = "", rating = null
        )

        // Verify we're at Success
        assertEquals(SaveStatus.Success, viewModel.saveStatus.value)

        // When
        viewModel.clearSaveStatus()

        // Then
        assertEquals(SaveStatus.Idle, viewModel.saveStatus.value)
    }
}
