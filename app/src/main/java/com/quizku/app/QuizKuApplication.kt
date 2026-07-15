package com.quizku.app

import android.app.Application
import com.quizku.app.data.local.entity.User
import com.quizku.app.util.ApiConfig
import com.quizku.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuizKuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (ApiConfig.auth.currentUser == null) {
            ApiConfig.auth.signInAnonymously()
        }

        seedDefaultUsers()
    }

    private fun seedDefaultUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = ApiConfig.firestore.collection("users")
                    .document(Constants.TEACHER_DEMO_ID).get().await()
                if (!existing.exists()) {
                    val guru = User(
                        id = Constants.TEACHER_DEMO_ID,
                        username = "guru_demo",
                        role = Constants.ROLE_TEACHER,
                        createdAt = System.currentTimeMillis()
                    )
                    ApiConfig.firestore.collection("users").document(guru.id).set(guru.toMap()).await()

                    val siswa = User(
                        id = UUID.randomUUID().toString(),
                        username = "siswa_demo",
                        role = Constants.ROLE_STUDENT,
                        createdAt = System.currentTimeMillis()
                    )
                    ApiConfig.firestore.collection("users").document(siswa.id).set(siswa.toMap()).await()
                }
            } catch (_: Exception) { }
        }
    }

    companion object {
        lateinit var instance: QuizKuApplication
            private set
    }
}
