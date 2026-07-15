package com.quizku.app.data.local.entity

data class Participant(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val joinedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "roomId" to roomId,
        "userId" to userId,
        "joinedAt" to joinedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>) = Participant(
            id = map["id"] as? String ?: "",
            roomId = map["roomId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
