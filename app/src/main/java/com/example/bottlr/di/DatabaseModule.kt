package com.example.bottlr.di

import android.content.Context
import androidx.room.Room
import com.example.bottlr.data.local.BottlrDatabase
import com.example.bottlr.data.local.dao.BottleDao
import com.example.bottlr.data.local.dao.CocktailDao
import com.example.bottlr.data.local.dao.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BottlrDatabase {
        return Room.databaseBuilder(
            context,
            BottlrDatabase::class.java,
            "bottlr_database"
        ).build()
    }

    @Provides
    fun provideBottleDao(database: BottlrDatabase): BottleDao {
        return database.bottleDao()
    }

    @Provides
    fun provideCocktailDao(database: BottlrDatabase): CocktailDao {
        return database.cocktailDao()
    }

    @Provides
    fun provideLocationDao(database: BottlrDatabase): LocationDao {
        return database.locationDao()
    }
}
