package com.quizku.app.data.local.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.quizku.app.data.local.entity.User
import com.quizku.app.util.ApiConfig
import kotlinx.coroutines.tasks.await

object UserRepository {

    private val users = ApiConfig.firestore.collection("users")

    suspend fun insertUser(user: User) {
        users.document(user.id).set(user.toMap()).await()
    }

    suspend fun getUserByUsername(username: String): User? {
        val snap = users.whereEqualTo("username", username).limit(1).get().await()
        return snap.documents.firstOrNull()?.data?.let { User.fromMap(it) }
    }

    suspend fun getUserById(userId: String): User? {
        val doc = users.document(userId).get().await()
        return doc.data?.let { User.fromMap(it) }
    }

    suspend fun getUsersByRole(role: String): List<User> {
        val snap = users.whereEqualTo("role", role).get().await()
        return snap.documents.mapNotNull { it.data?.let { d -> User.fromMap(d) } }
    }

    suspend fun deleteUser(userId: String) {
        users.document(userId).delete().await()
    }
}
