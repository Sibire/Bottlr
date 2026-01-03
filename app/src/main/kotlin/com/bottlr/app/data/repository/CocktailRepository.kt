package com.bottlr.app.data.repository

import android.net.Uri
import com.bottlr.app.data.local.dao.CocktailDao
import com.bottlr.app.data.local.entities.CocktailEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CocktailRepository @Inject constructor(
    private val cocktailDao: CocktailDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    val allCocktails: Flow<List<CocktailEntity>> = cocktailDao.getAllCocktails()

    fun getCocktailById(id: Long): Flow<CocktailEntity?> = cocktailDao.getCocktailById(id)

    fun searchByField(field: String, query: String): Flow<List<CocktailEntity>> {
        return when (field) {
            "Name" -> cocktailDao.searchByName(query)
            "Base" -> cocktailDao.searchByBase(query)
            "Mixer" -> cocktailDao.searchByMixer(query)
            "Keywords" -> cocktailDao.searchByKeywords(query)
            else -> allCocktails
        }
    }

    suspend fun insert(cocktail: CocktailEntity): Long {
        return cocktailDao.insert(cocktail)
    }

    suspend fun update(cocktail: CocktailEntity) {
        cocktailDao.update(cocktail.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(cocktail: CocktailEntity) {
        cocktailDao.delete(cocktail)
        cocktail.firestoreId?.let { deleteFromFirestore(it) }
    }

    // Firestore sync
    suspend fun syncToFirestore(cocktailId: Long) {
        val cocktail = cocktailDao.getCocktailById(cocktailId).first() ?: return
        val userId = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "name" to cocktail.name,
            "base" to cocktail.base,
            "mixer" to cocktail.mixer,
            "juice" to cocktail.juice,
            "liqueur" to cocktail.liqueur,
            "garnish" to cocktail.garnish,
            "extra" to cocktail.extra,
            "notes" to cocktail.notes,
            "keywords" to cocktail.keywords,
            "rating" to cocktail.rating,
            "updatedAt" to cocktail.updatedAt
        )

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .document(cocktail.firestoreId ?: UUID.randomUUID().toString())

        docRef.set(data).await()
        cocktailDao.markSynced(cocktailId, docRef.id)

        cocktail.photoUri?.let { uploadPhoto(cocktailId, it) }
    }

    suspend fun syncAllToFirestore() {
        val cocktails = allCocktails.first()
        cocktails.forEach { cocktail ->
            syncToFirestore(cocktail.id)
        }
    }

    suspend fun syncFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            val cocktail = CocktailEntity(
                name = doc.getString("name") ?: "",
                base = doc.getString("base") ?: "",
                mixer = doc.getString("mixer") ?: "",
                juice = doc.getString("juice") ?: "",
                liqueur = doc.getString("liqueur") ?: "",
                garnish = doc.getString("garnish") ?: "",
                extra = doc.getString("extra") ?: "",
                notes = doc.getString("notes") ?: "",
                keywords = doc.getString("keywords") ?: "",
                rating = doc.getDouble("rating")?.toFloat(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                firestoreId = doc.id,
                firebaseSynced = true
            )
            cocktailDao.insert(cocktail)
        }
    }

    private suspend fun uploadPhoto(cocktailId: Long, photoUri: String) {
        val userId = auth.currentUser?.uid ?: return
        val uri = Uri.parse(photoUri)
        val ref = storage.reference
            .child("users/$userId/cocktails/$cocktailId.jpg")
        ref.putFile(uri).await()
    }

    private suspend fun deleteFromFirestore(firestoreId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .document(firestoreId)
            .delete()
            .await()
    }
}
