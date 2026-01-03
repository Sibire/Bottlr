package com.bottlr.app.data.local.dao

import androidx.room.*
import com.bottlr.app.data.local.entities.BottleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BottleDao {
    @Query("SELECT * FROM bottles ORDER BY name ASC")
    fun getAllBottles(): Flow<List<BottleEntity>>

    @Query("SELECT * FROM bottles WHERE id = :id")
    fun getBottleById(id: Long): Flow<BottleEntity?>

    @Query("SELECT * FROM bottles WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<BottleEntity>>

    @Query("SELECT * FROM bottles WHERE distillery LIKE '%' || :query || '%'")
    fun searchByDistillery(query: String): Flow<List<BottleEntity>>

    @Query("SELECT * FROM bottles WHERE type LIKE '%' || :query || '%'")
    fun searchByType(query: String): Flow<List<BottleEntity>>

    @Query("SELECT * FROM bottles WHERE region LIKE '%' || :query || '%'")
    fun searchByRegion(query: String): Flow<List<BottleEntity>>

    @Query("SELECT * FROM bottles WHERE keywords LIKE '%' || :query || '%'")
    fun searchByKeywords(query: String): Flow<List<BottleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bottle: BottleEntity): Long

    @Update
    suspend fun update(bottle: BottleEntity)

    @Delete
    suspend fun delete(bottle: BottleEntity)

    @Query("SELECT * FROM bottles WHERE firebaseSynced = 0")
    suspend fun getUnsyncedBottles(): List<BottleEntity>

    @Query("UPDATE bottles SET firebaseSynced = 1, firestoreId = :firestoreId WHERE id = :id")
    suspend fun markSynced(id: Long, firestoreId: String)
}
