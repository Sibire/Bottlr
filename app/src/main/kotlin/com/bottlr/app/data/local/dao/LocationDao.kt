package com.bottlr.app.data.local.dao

import androidx.room.*
import com.bottlr.app.data.local.entities.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY createdAt DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    fun getLocationById(id: Long): Flow<LocationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)
}
