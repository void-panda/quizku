package com.quizku.app.util

object Constants {
    // Database
    const val DATABASE_NAME = "quizku_database"
    
    // Room Status
    const val STATUS_DRAFT = "DRAFT"
    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_LOCKED = "LOCKED"
    const val STATUS_COMPLETED = "COMPLETED"
    
    // User Roles
    const val ROLE_TEACHER = "TEACHER"
    const val ROLE_STUDENT = "STUDENT"
    const val TEACHER_DEMO_ID = "teacher_demo_001"
    
    // Excel Columns
    const val EXCEL_COLUMN_SOAL = "SOAL"
    const val EXCEL_COLUMN_OPSI_A = "OPSI_A"
    const val EXCEL_COLUMN_OPSI_B = "OPSI_B"
    const val EXCEL_COLUMN_OPSI_C = "OPSI_C"
    const val EXCEL_COLUMN_OPSI_D = "OPSI_D"
    const val EXCEL_COLUMN_OPSI_E = "OPSI_E"
    const val EXCEL_COLUMN_JAWABAN = "JAWABAN"
    
    // Excel Validation
    const val MIN_QUESTIONS = 10
    const val MAX_QUESTIONS = 100
    const val MAX_QUESTION_LENGTH = 500
    const val MAX_OPTION_LENGTH = 100
    
    // Room Code
    const val ROOM_CODE_LENGTH = 6
    const val ROOM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    
    // Valid Answers
    val VALID_ANSWERS = listOf("A", "B", "C", "D", "E")
}
