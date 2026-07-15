package com.quizku.app.data.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quizku.app.data.local.entity.Answer
import com.quizku.app.util.ApiConfig
import kotlinx.coroutines.tasks.await

object AnswerRepository {

    private val answers = ApiConfig.firestore.collection("answers")

    suspend fun insertAnswer(answer: Answer) {
        answers.add(answer.toMap()).await()
    }

    suspend fun updateAnswer(answer: Answer) {
        answers.document(answer.id).set(answer.toMap()).await()
    }

    suspend fun getAnswersByParticipant(participantId: String): List<Answer> {
        val snap = answers.whereEqualTo("participantId", participantId).get().await()
        return snap.documents.mapNotNull { doc ->
            doc.data?.let { Answer.fromMap(it).copy(id = doc.id) }
        }
    }

    fun getAnswersByParticipantLiveData(participantId: String): LiveData<List<Answer>> {
        val liveData = MutableLiveData<List<Answer>>()
        answers.whereEqualTo("participantId", participantId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Answer.fromMap(it).copy(id = doc.id) }
                } ?: emptyList()
                liveData.postValue(list)
            }
        return liveData
    }

    suspend fun getAnswerByQuestion(participantId: String, questionId: String): Answer? {
        val snap = answers.whereEqualTo("participantId", participantId)
            .whereEqualTo("questionId", questionId)
            .limit(1).get().await()
        return snap.documents.firstOrNull()?.let { doc ->
            doc.data?.let { Answer.fromMap(it).copy(id = doc.id) }
        }
    }

    fun getAnswerByQuestionLiveData(participantId: String, questionId: String): LiveData<Answer?> {
        val liveData = MutableLiveData<Answer?>()
        answers.whereEqualTo("participantId", participantId)
            .whereEqualTo("questionId", questionId)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                val doc = snapshot?.documents?.firstOrNull()
                liveData.postValue(doc?.data?.let { Answer.fromMap(it).copy(id = doc.id) })
            }
        return liveData
    }

    suspend fun getCorrectAnswerCount(participantId: String): Int {
        val snap = answers.whereEqualTo("participantId", participantId)
            .whereEqualTo("isCorrect", true).get().await()
        return snap.size()
    }

    fun getCorrectAnswerCountLiveData(participantId: String): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        answers.whereEqualTo("participantId", participantId)
            .whereEqualTo("isCorrect", true)
            .addSnapshotListener { snapshot, _ ->
                liveData.postValue(snapshot?.size() ?: 0)
            }
        return liveData
    }

    suspend fun getTotalAnswerCount(participantId: String): Int {
        val snap = answers.whereEqualTo("participantId", participantId).get().await()
        return snap.size()
    }

    fun getTotalAnswerCountLiveData(participantId: String): LiveData<Int> {
        val liveData = MutableLiveData<Int>()
        answers.whereEqualTo("participantId", participantId)
            .addSnapshotListener { snapshot, _ ->
                liveData.postValue(snapshot?.size() ?: 0)
            }
        return liveData
    }

    suspend fun deleteAnswersByParticipant(participantId: String) {
        val snap = answers.whereEqualTo("participantId", participantId).get().await()
        val batch = ApiConfig.firestore.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}
