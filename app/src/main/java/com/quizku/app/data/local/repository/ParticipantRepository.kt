package com.quizku.app.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quizku.app.data.local.entity.Participant
import com.quizku.app.util.ApiConfig
import kotlinx.coroutines.tasks.await

object ParticipantRepository {

    private val participants = ApiConfig.firestore.collection("participants")

    suspend fun insertParticipant(participant: Participant) {
        participants.document(participant.id).set(participant.toMap()).await()
    }

    suspend fun getParticipantsByRoom(roomId: String): List<Participant> {
        val snap = participants.whereEqualTo("roomId", roomId).get().await()
        return snap.documents.mapNotNull { it.data?.let { d -> Participant.fromMap(d) } }
    }

    fun getParticipantsByRoomLiveData(roomId: String): LiveData<List<Participant>> {
        val liveData = MutableLiveData<List<Participant>>()
        participants.whereEqualTo("roomId", roomId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.data?.let { d -> Participant.fromMap(d) } } ?: emptyList()
                liveData.postValue(list)
            }
        return liveData
    }

    suspend fun getParticipant(roomId: String, userId: String): Participant? {
        val snap = participants.whereEqualTo("roomId", roomId)
            .whereEqualTo("userId", userId)
            .limit(1).get().await()
        return snap.documents.firstOrNull()?.data?.let { Participant.fromMap(it) }
    }

    fun getParticipantLiveData(roomId: String, userId: String): LiveData<Participant?> {
        val liveData = MutableLiveData<Participant?>()
        participants.whereEqualTo("roomId", roomId)
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                liveData.postValue(snapshot?.documents?.firstOrNull()?.data?.let { Participant.fromMap(it) })
            }
        return liveData
    }

    suspend fun getParticipantCount(roomId: String): Int {
        val snap = participants.whereEqualTo("roomId", roomId).get().await()
        return snap.size()
    }

    fun getParticipantCountLiveData(roomId: String): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        participants.whereEqualTo("roomId", roomId)
            .addSnapshotListener { snapshot, _ ->
                liveData.postValue(snapshot?.size() ?: 0)
            }
        return liveData
    }

    suspend fun deleteParticipant(participantId: String) {
        participants.document(participantId).delete().await()
    }

    suspend fun getActiveParticipantCount(roomId: String): Int {
        return getParticipantCount(roomId)
    }
}
