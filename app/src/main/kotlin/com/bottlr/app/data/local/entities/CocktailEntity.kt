package com.bottlr.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cocktails")
data class CocktailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val base: String = "",
    val mixer: String = "",
    val juice: String = "",
    val liqueur: String = "",
    val garnish: String = "",
    val extra: String = "",
    val photoUri: String? = null,
    val notes: String = "",
    val keywords: String = "",
    val rating: Float? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val firestoreId: String? = null,
    val firebaseSynced: Boolean = false
)
