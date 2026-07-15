package com.quizku.app.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quizku.app.data.local.entity.QuizRoom
import com.quizku.app.util.ApiConfig
import kotlinx.coroutines.tasks.await

object QuizRoomRepository {

    private val rooms = ApiConfig.firestore.collection("rooms")

    suspend fun insertRoom(quizRoom: QuizRoom) {
        rooms.document(quizRoom.id).set(quizRoom.toMap()).await()
    }

    suspend fun updateRoom(quizRoom: QuizRoom) {
        rooms.document(quizRoom.id).set(quizRoom.toMap()).await()
    }

    suspend fun getRoomByCode(roomCode: String): QuizRoom? {
        val snap = rooms.whereEqualTo("roomCode", roomCode).limit(1).get().await()
        return snap.documents.firstOrNull()?.data?.let { QuizRoom.fromMap(it) }
    }

    suspend fun getRoomById(roomId: String): QuizRoom? {
        val doc = rooms.document(roomId).get().await()
        return doc.data?.let { QuizRoom.fromMap(it) }
    }

    fun getRoomByIdLiveData(roomId: String): LiveData<QuizRoom?> {
        val liveData = MutableLiveData<QuizRoom?>()
        rooms.document(roomId).addSnapshotListener { snapshot, _ ->
            liveData.postValue(snapshot?.data?.let { QuizRoom.fromMap(it) })
        }
        return liveData
    }

    suspend fun getRoomsByTeacher(teacherId: String): List<QuizRoom> {
        val snap = rooms.whereEqualTo("teacherId", teacherId).get().await()
        return snap.documents.mapNotNull { it.data?.let { d -> QuizRoom.fromMap(d) } }
            .sortedByDescending { it.createdAt }
    }

    fun getRoomsByTeacherLiveData(teacherId: String): LiveData<List<QuizRoom>> {
        val liveData = MutableLiveData<List<QuizRoom>>()
        rooms.whereEqualTo("teacherId", teacherId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.data?.let { d -> QuizRoom.fromMap(d) } }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                liveData.postValue(list)
            }
        return liveData
    }

    suspend fun updateStatus(roomId: String, status: String) {
        rooms.document(roomId).update("status", status).await()
    }

    suspend fun updateStartTime(roomId: String, startedAt: Long, endsAt: Long) {
        rooms.document(roomId).update(
            mapOf("startedAt" to startedAt, "endsAt" to endsAt)
        ).await()
    }

    suspend fun deleteRoom(roomId: String) {
        rooms.document(roomId).delete().await()
    }

    suspend fun getRoomsByStatus(status: String): List<QuizRoom> {
        val snap = rooms.whereEqualTo("status", status).get().await()
        return snap.documents.mapNotNull { it.data?.let { d -> QuizRoom.fromMap(d) } }
            .sortedByDescending { it.createdAt }
    }
}
