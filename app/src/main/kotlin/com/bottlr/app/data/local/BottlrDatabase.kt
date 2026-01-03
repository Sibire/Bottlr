package com.bottlr.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.dao.CocktailDao
import com.bottlr.app.data.local.dao.LocationDao
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.data.local.entities.LocationEntity

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
