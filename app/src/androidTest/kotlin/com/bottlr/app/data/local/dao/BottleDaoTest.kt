package com.bottlr.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bottlr.app.data.local.BottlrDatabase
import com.bottlr.app.data.local.entities.BottleEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for BottleDao.
 *
 * Uses an in-memory Room database for fast, isolated tests.
 * Must run on a device/emulator because Room requires Android's SQLite.
 *
 * Tests cover:
 * - Insert, update, delete operations
 * - Query operations (get by ID, get all)
 * - Search operations (by name, distillery, type, region, keywords)
 * - Sync operations (mark synced, get unsynced)
 */
@RunWith(AndroidJUnit4::class)
class BottleDaoTest {

    private lateinit var database: BottlrDatabase
    private lateinit var dao: BottleDao

    @Before
    fun setup() {
        // In-memory database for testing - destroyed after test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BottlrDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.bottleDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // Helper to create test bottles
    private fun testBottle(
        id: Long = 0L,
        name: String = "Test Whisky",
        distillery: String = "Test Distillery",
        type: String = "Single Malt",
        region: String = "Islay",
        keywords: String = "peaty, smoky"
    ) = BottleEntity(
        id = id,
        name = name,
        distillery = distillery,
        type = type,
        abv = 46.0f,
        age = 12,
        photoUri = null,
        notes = "Test notes",
        region = region,
        keywords = keywords,
        rating = 8.5f
    )

    // === INSERT TESTS ===

    @Test
    fun insert_returnsGeneratedId() = runTest {
        val bottle = testBottle()
        val id = dao.insert(bottle)
        assertTrue(id > 0)
    }

    @Test
    fun insert_replacesOnConflict() = runTest {
        val bottle1 = testBottle(name = "Original")
        val id = dao.insert(bottle1)

        val bottle2 = testBottle(id = id, name = "Replaced")
        dao.insert(bottle2)

        dao.getBottleById(id).test {
            assertEquals("Replaced", awaitItem()?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === GET TESTS ===

    @Test
    fun getBottleById_returnsBottle() = runTest {
        val id = dao.insert(testBottle(name = "Find Me"))

        dao.getBottleById(id).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Find Me", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBottleById_returnsNullWhenNotFound() = runTest {
        dao.getBottleById(999L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllBottles_returnsSortedByName() = runTest {
        dao.insert(testBottle(name = "Zebra"))
        dao.insert(testBottle(name = "Apple"))
        dao.insert(testBottle(name = "Mango"))

        dao.getAllBottles().test {
            val results = awaitItem()
            assertEquals(3, results.size)
            assertEquals("Apple", results[0].name)
            assertEquals("Mango", results[1].name)
            assertEquals("Zebra", results[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === UPDATE TESTS ===

    @Test
    fun update_modifiesExistingBottle() = runTest {
        val id = dao.insert(testBottle(name = "Before"))

        dao.getBottleById(id).test {
            val bottle = awaitItem()!!
            dao.update(bottle.copy(name = "After"))
            cancelAndIgnoreRemainingEvents()
        }

        dao.getBottleById(id).test {
            assertEquals("After", awaitItem()?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === DELETE TESTS ===

    @Test
    fun delete_removesBottle() = runTest {
        val id = dao.insert(testBottle())

        dao.getBottleById(id).test {
            val bottle = awaitItem()!!
            dao.delete(bottle)
            cancelAndIgnoreRemainingEvents()
        }

        dao.getBottleById(id).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === SEARCH TESTS ===

    @Test
    fun searchByName_findsPartialMatch() = runTest {
        dao.insert(testBottle(name = "Lagavulin 16"))
        dao.insert(testBottle(name = "Ardbeg 10"))
        dao.insert(testBottle(name = "Laphroaig Quarter Cask"))

        dao.searchByName("%laga%").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Lagavulin 16", results[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByName_isCaseInsensitive() = runTest {
        dao.insert(testBottle(name = "MACALLAN"))

        dao.searchByName("%macallan%").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByDistillery_findsMatches() = runTest {
        dao.insert(testBottle(distillery = "The Macallan"))
        dao.insert(testBottle(distillery = "Highland Park"))
        dao.insert(testBottle(distillery = "Macallan Distillers"))

        dao.searchByDistillery("%macallan%").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByType_findsMatches() = runTest {
        dao.insert(testBottle(type = "Single Malt"))
        dao.insert(testBottle(type = "Blended Malt"))
        dao.insert(testBottle(type = "Bourbon"))

        dao.searchByType("%malt%").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByRegion_findsMatches() = runTest {
        dao.insert(testBottle(region = "Islay"))
        dao.insert(testBottle(region = "Highland"))
        dao.insert(testBottle(region = "Speyside"))

        dao.searchByRegion("%islay%").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Islay", results[0].region)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByKeywords_findsMatches() = runTest {
        dao.insert(testBottle(keywords = "peaty, smoky, maritime"))
        dao.insert(testBottle(keywords = "fruity, sweet"))
        dao.insert(testBottle(keywords = "smoky, bold"))

        dao.searchByKeywords("%smoky%").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === SYNC TESTS ===

    @Test
    fun getUnsyncedBottles_returnsOnlyUnsynced() = runTest {
        dao.insert(testBottle(name = "Synced").copy(firebaseSynced = true))
        dao.insert(testBottle(name = "Unsynced 1"))
        dao.insert(testBottle(name = "Unsynced 2"))

        val unsynced = dao.getUnsyncedBottles()
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.all { !it.firebaseSynced })
    }

    @Test
    fun markSynced_updatesBottle() = runTest {
        val id = dao.insert(testBottle())

        dao.markSynced(id, "firestore-123")

        dao.getBottleById(id).test {
            val bottle = awaitItem()!!
            assertTrue(bottle.firebaseSynced)
            assertEquals("firestore-123", bottle.firestoreId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
