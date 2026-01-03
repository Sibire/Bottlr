package com.bottlr.app.data.repository

import android.net.Uri
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.entities.BottleEntity
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
class BottleRepository @Inject constructor(
    private val bottleDao: BottleDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    val allBottles: Flow<List<BottleEntity>> = bottleDao.getAllBottles()

    fun getBottleById(id: Long): Flow<BottleEntity?> = bottleDao.getBottleById(id)

    fun searchByField(field: String, query: String): Flow<List<BottleEntity>> {
        return when (field) {
            "Name" -> bottleDao.searchByName(query)
            "Distillery" -> bottleDao.searchByDistillery(query)
            "Type" -> bottleDao.searchByType(query)
            "Region" -> bottleDao.searchByRegion(query)
            "Keywords" -> bottleDao.searchByKeywords(query)
            else -> allBottles
        }
    }

    suspend fun insert(bottle: BottleEntity): Long {
        return bottleDao.insert(bottle)
    }

    suspend fun update(bottle: BottleEntity) {
        bottleDao.update(bottle.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(bottle: BottleEntity) {
        bottleDao.delete(bottle)
        // Delete from Firestore if synced
        bottle.firestoreId?.let { deleteFromFirestore(it) }
    }

    // Firestore sync methods
    suspend fun syncToFirestore(bottleId: Long) {
        val bottle = bottleDao.getBottleById(bottleId).first() ?: return
        val userId = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "name" to bottle.name,
            "distillery" to bottle.distillery,
            "type" to bottle.type,
            "abv" to bottle.abv,
            "age" to bottle.age,
            "notes" to bottle.notes,
            "region" to bottle.region,
            "keywords" to bottle.keywords,
            "rating" to bottle.rating,
            "updatedAt" to bottle.updatedAt
        )

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .document(bottle.firestoreId ?: UUID.randomUUID().toString())

        docRef.set(data).await()
        bottleDao.markSynced(bottleId, docRef.id)

        // Upload photo if exists
        bottle.photoUri?.let { uploadPhoto(bottleId, it) }
    }

    suspend fun syncAllToFirestore() {
        val bottles = allBottles.first()
        bottles.forEach { bottle ->
            syncToFirestore(bottle.id)
        }
    }

    suspend fun syncFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            val bottle = BottleEntity(
                name = doc.getString("name") ?: "",
                distillery = doc.getString("distillery") ?: "",
                type = doc.getString("type") ?: "",
                abv = doc.getDouble("abv")?.toFloat(),
                age = doc.getLong("age")?.toInt(),
                notes = doc.getString("notes") ?: "",
                region = doc.getString("region") ?: "",
                keywords = doc.getString("keywords") ?: "",
                rating = doc.getDouble("rating")?.toFloat(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                firestoreId = doc.id,
                firebaseSynced = true
            )
            bottleDao.insert(bottle)
        }
    }

    private suspend fun uploadPhoto(bottleId: Long, photoUri: String) {
        val userId = auth.currentUser?.uid ?: return
        val uri = Uri.parse(photoUri)
        val ref = storage.reference
            .child("users/$userId/bottles/$bottleId.jpg")
        ref.putFile(uri).await()
    }

    private suspend fun deleteFromFirestore(firestoreId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .document(firestoreId)
            .delete()
            .await()
    }
}
