package com.bottlr.app.data.repository

import app.cash.turbine.test
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
 * Unit tests for BottleRepository.
 *
 * Tests cover:
 * - CRUD operations delegation to DAO
 * - Search field routing
 * - Firestore sync operations
 * - Photo upload handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BottleRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var dao: BottleDao

    @MockK
    private lateinit var firestore: FirebaseFirestore

    @MockK
    private lateinit var storage: FirebaseStorage

    @MockK
    private lateinit var auth: FirebaseAuth

    private lateinit var repository: BottleRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Default stub for allBottles (called in constructor)
        every { dao.getAllBottles() } returns flowOf(emptyList())
        repository = BottleRepository(dao, firestore, storage, auth)
    }

    // === CRUD TESTS ===

    @Test
    fun `insert delegates to DAO`() = runTest {
        // Given
        val bottle = TestFixtures.bottle()
        coEvery { dao.insert(bottle) } returns 42L

        // When
        val id = repository.insert(bottle)

        // Then
        assertEquals(42L, id)
        coVerify { dao.insert(bottle) }
    }

    @Test
    fun `update delegates to DAO with updated timestamp`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        coEvery { dao.update(any()) } just Runs

        // When
        repository.update(bottle)

        // Then
        coVerify { dao.update(match { it.id == 1L && it.updatedAt >= bottle.updatedAt }) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        coEvery { dao.delete(bottle) } just Runs

        // When
        repository.delete(bottle)

        // Then
        coVerify { dao.delete(bottle) }
    }

    @Test
    fun `delete without firestoreId does not call Firestore`() = runTest {
        // Given - bottle not synced to Firestore
        val bottle = TestFixtures.bottle(id = 1L).copy(
            firestoreId = null,
            firebaseSynced = false
        )
        coEvery { dao.delete(bottle) } just Runs

        // When
        repository.delete(bottle)

        // Then - only DAO called, no Firestore
        coVerify { dao.delete(bottle) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `getBottleById delegates to DAO`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 42L)
        every { dao.getBottleById(42L) } returns flowOf(bottle)

        // When/Then
        repository.getBottleById(42L).test {
            assertEquals(bottle, awaitItem())
            awaitComplete()
        }
    }

    // === SEARCH ROUTING TESTS ===

    @Test
    fun `searchByField routes Name to searchByName`() = runTest {
        // Given
        val bottles = listOf(TestFixtures.bottle(name = "Lagavulin"))
        every { dao.searchByName("Lagavulin") } returns flowOf(bottles)

        // When/Then
        repository.searchByField("Name", "Lagavulin").test {
            assertEquals(bottles, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `searchByField routes Distillery to searchByDistillery`() = runTest {
        // Given
        val bottles = listOf(TestFixtures.bottle(distillery = "Macallan"))
        every { dao.searchByDistillery("Macallan") } returns flowOf(bottles)

        // When/Then
        repository.searchByField("Distillery", "Macallan").test {
            assertEquals(bottles, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `searchByField routes Type to searchByType`() = runTest {
        // Given
        val bottles = listOf(TestFixtures.bottle())
        every { dao.searchByType("Single Malt") } returns flowOf(bottles)

        // When/Then
        repository.searchByField("Type", "Single Malt").test {
            assertEquals(bottles, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `searchByField routes Region to searchByRegion`() = runTest {
        // Given
        val bottles = listOf(TestFixtures.bottle())
        every { dao.searchByRegion("Islay") } returns flowOf(bottles)

        // When/Then
        repository.searchByField("Region", "Islay").test {
            assertEquals(bottles, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `searchByField routes Keywords to searchByKeywords`() = runTest {
        // Given
        val bottles = listOf(TestFixtures.bottle())
        every { dao.searchByKeywords("peaty") } returns flowOf(bottles)

        // When/Then
        repository.searchByField("Keywords", "peaty").test {
            assertEquals(bottles, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `searchByField with unknown field returns all bottles`() = runTest {
        // Given - recreate repository with stubbed data
        val bottles = TestFixtures.bottles(3)
        every { dao.getAllBottles() } returns flowOf(bottles)
        val testRepository = BottleRepository(dao, firestore, storage, auth)

        // When/Then
        testRepository.searchByField("Unknown", "query").test {
            assertEquals(3, awaitItem().size)
            awaitComplete()
        }
    }

    // === SYNC TESTS ===

    @Test
    fun `syncToFirestore does nothing when user not logged in`() = runTest {
        // Given
        val bottle = TestFixtures.bottle(id = 1L)
        every { dao.getBottleById(1L) } returns flowOf(bottle)
        every { auth.currentUser } returns null

        // When
        repository.syncToFirestore(1L)

        // Then - no Firestore calls
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `syncToFirestore does nothing when bottle not found`() = runTest {
        // Given
        every { dao.getBottleById(999L) } returns flowOf(null)

        // When
        repository.syncToFirestore(999L)

        // Then - no auth check needed
        verify(exactly = 0) { auth.currentUser }
    }

    @Test
    fun `allBottles delegates to DAO`() = runTest {
        // Given - recreate repository with stubbed data
        val bottles = TestFixtures.bottles(5)
        every { dao.getAllBottles() } returns flowOf(bottles)
        val testRepository = BottleRepository(dao, firestore, storage, auth)

        // When/Then
        testRepository.allBottles.test {
            assertEquals(5, awaitItem().size)
            awaitComplete()
        }
    }
}
