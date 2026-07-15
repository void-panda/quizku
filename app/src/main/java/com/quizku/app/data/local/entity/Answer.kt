package com.quizku.app.data.local.entity

data class Answer(
    val id: String = "",
    val participantId: String = "",
    val questionId: String = "",
    val selectedAnswer: String = "",
    val isCorrect: Boolean = false,
    val answeredAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "participantId" to participantId,
        "questionId" to questionId,
        "selectedAnswer" to selectedAnswer,
        "isCorrect" to isCorrect,
        "answeredAt" to answeredAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>) = Answer(
            id = map["id"] as? String ?: "",
            participantId = map["participantId"] as? String ?: "",
            questionId = map["questionId"] as? String ?: "",
            selectedAnswer = map["selectedAnswer"] as? String ?: "",
            isCorrect = map["isCorrect"] as? Boolean ?: false,
            answeredAt = (map["answeredAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
