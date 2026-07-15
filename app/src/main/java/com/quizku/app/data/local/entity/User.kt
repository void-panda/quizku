package com.quizku.app.data.local.entity

data class User(
    val id: String = "",
    val username: String = "",
    val role: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "username" to username,
        "role" to role,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>) = User(
            id = map["id"] as? String ?: "",
            username = map["username"] as? String ?: "",
            role = map["role"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
