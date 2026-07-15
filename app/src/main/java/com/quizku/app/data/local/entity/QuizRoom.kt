package com.quizku.app.data.local.entity

data class QuizRoom(
    val id: String = "",
    val roomCode: String = "",
    val teacherId: String = "",
    val title: String = "",
    val status: String = "",
    val durationMinutes: Int = 0,
    val startedAt: Long? = null,
    val endsAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "roomCode" to roomCode,
        "teacherId" to teacherId,
        "title" to title,
        "status" to status,
        "durationMinutes" to durationMinutes,
        "startedAt" to startedAt,
        "endsAt" to endsAt,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>) = QuizRoom(
            id = map["id"] as? String ?: "",
            roomCode = map["roomCode"] as? String ?: "",
            teacherId = map["teacherId"] as? String ?: "",
            title = map["title"] as? String ?: "",
            status = map["status"] as? String ?: "",
            durationMinutes = (map["durationMinutes"] as? Number)?.toInt() ?: 0,
            startedAt = (map["startedAt"] as? Number)?.toLong(),
            endsAt = (map["endsAt"] as? Number)?.toLong(),
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
