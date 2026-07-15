package com.quizku.app.data.local.repository

import com.quizku.app.data.local.entity.Question
import com.quizku.app.util.ApiConfig
import kotlinx.coroutines.tasks.await

object QuestionRepository {

    private val questions = ApiConfig.firestore.collection("questions")

    suspend fun insertAllQuestions(questionsList: List<Question>) {
        val batch = ApiConfig.firestore.batch()
        questionsList.forEach { q ->
            batch.set(questions.document(q.id), q.toMap())
        }
        batch.commit().await()
    }

    suspend fun getQuestionsByRoom(roomId: String): List<Question> {
        val snap = questions.whereEqualTo("roomId", roomId).get().await()
        return snap.documents.mapNotNull { it.data?.let { d -> Question.fromMap(d) } }
            .sortedBy { it.orderNumber }
    }

    suspend fun getQuestionByOrder(roomId: String, orderNumber: Int): Question? {
        val snap = questions.whereEqualTo("roomId", roomId)
            .whereEqualTo("orderNumber", orderNumber)
            .limit(1).get().await()
        return snap.documents.firstOrNull()?.data?.let { Question.fromMap(it) }
    }

    suspend fun getQuestionCount(roomId: String): Int {
        val snap = questions.whereEqualTo("roomId", roomId).get().await()
        return snap.size()
    }

    suspend fun getQuestionById(questionId: String): Question? {
        val doc = questions.document(questionId).get().await()
        return doc.data?.let { Question.fromMap(it) }
    }

    suspend fun deleteQuestionsByRoom(roomId: String) {
        val snap = questions.whereEqualTo("roomId", roomId).get().await()
        val batch = ApiConfig.firestore.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}
