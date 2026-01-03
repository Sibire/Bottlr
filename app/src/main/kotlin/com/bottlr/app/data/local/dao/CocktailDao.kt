package com.bottlr.app.data.local.dao

import androidx.room.*
import com.bottlr.app.data.local.entities.CocktailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CocktailDao {
    @Query("SELECT * FROM cocktails ORDER BY name ASC")
    fun getAllCocktails(): Flow<List<CocktailEntity>>

    @Query("SELECT * FROM cocktails WHERE id = :id")
    fun getCocktailById(id: Long): Flow<CocktailEntity?>

    @Query("SELECT * FROM cocktails WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<CocktailEntity>>

    @Query("SELECT * FROM cocktails WHERE base LIKE '%' || :query || '%'")
    fun searchByBase(query: String): Flow<List<CocktailEntity>>

    @Query("SELECT * FROM cocktails WHERE mixer LIKE '%' || :query || '%'")
    fun searchByMixer(query: String): Flow<List<CocktailEntity>>

    @Query("SELECT * FROM cocktails WHERE keywords LIKE '%' || :query || '%'")
    fun searchByKeywords(query: String): Flow<List<CocktailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cocktail: CocktailEntity): Long

    @Update
    suspend fun update(cocktail: CocktailEntity)

    @Delete
    suspend fun delete(cocktail: CocktailEntity)

    @Query("SELECT * FROM cocktails WHERE firebaseSynced = 0")
    suspend fun getUnsyncedCocktails(): List<CocktailEntity>

    @Query("UPDATE cocktails SET firebaseSynced = 1, firestoreId = :firestoreId WHERE id = :id")
    suspend fun markSynced(id: Long, firestoreId: String)
}
