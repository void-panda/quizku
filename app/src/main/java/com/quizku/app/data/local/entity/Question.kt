package com.quizku.app.data.local.entity

data class Question(
    val id: String = "",
    val roomId: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val optionE: String = "",
    val correctAnswer: String = "",
    val orderNumber: Int = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "roomId" to roomId,
        "questionText" to questionText,
        "optionA" to optionA,
        "optionB" to optionB,
        "optionC" to optionC,
        "optionD" to optionD,
        "optionE" to optionE,
        "correctAnswer" to correctAnswer,
        "orderNumber" to orderNumber
    )

    companion object {
        fun fromMap(map: Map<String, Any?>) = Question(
            id = map["id"] as? String ?: "",
            roomId = map["roomId"] as? String ?: "",
            questionText = map["questionText"] as? String ?: "",
            optionA = map["optionA"] as? String ?: "",
            optionB = map["optionB"] as? String ?: "",
            optionC = map["optionC"] as? String ?: "",
            optionD = map["optionD"] as? String ?: "",
            optionE = map["optionE"] as? String ?: "",
            correctAnswer = map["correctAnswer"] as? String ?: "",
            orderNumber = (map["orderNumber"] as? Number)?.toInt() ?: 0
        )
    }
}
