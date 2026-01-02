package com.example.bottlr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bottlr.data.local.dao.BottleDao
import com.example.bottlr.data.local.dao.CocktailDao
import com.example.bottlr.data.local.dao.LocationDao
import com.example.bottlr.data.local.entities.BottleEntity
import com.example.bottlr.data.local.entities.CocktailEntity
import com.example.bottlr.data.local.entities.LocationEntity

@Database(
    entities = [BottleEntity::class, CocktailEntity::class, LocationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BottlrDatabase : RoomDatabase() {
    abstract fun bottleDao(): BottleDao
    abstract fun cocktailDao(): CocktailDao
    abstract fun locationDao(): LocationDao
}
