package com.bottlr.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bottles")
data class BottleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val distillery: String = "",
    val type: String = "",
    val abv: Float? = null,
    val age: Int? = null,
    val photoUri: String? = null,
    val notes: String = "",
    val region: String = "",
    val keywords: String = "",
    val rating: Float? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Firestore sync tracking
    val firestoreId: String? = null,
    val firebaseSynced: Boolean = false
)
