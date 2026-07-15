package com.quizku.app.util

import com.quizku.app.data.local.entity.Question
import java.io.File
import java.io.FileOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object ExcelTemplateGenerator {
    
    fun generateSampleTemplate(context: android.content.Context): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Soal Quiz")
        
        // Create header row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue(Constants.EXCEL_COLUMN_SOAL)
        headerRow.createCell(1).setCellValue(Constants.EXCEL_COLUMN_OPSI_A)
        headerRow.createCell(2).setCellValue(Constants.EXCEL_COLUMN_OPSI_B)
        headerRow.createCell(3).setCellValue(Constants.EXCEL_COLUMN_OPSI_C)
        headerRow.createCell(4).setCellValue(Constants.EXCEL_COLUMN_OPSI_D)
        headerRow.createCell(5).setCellValue(Constants.EXCEL_COLUMN_OPSI_E)
        headerRow.createCell(6).setCellValue(Constants.EXCEL_COLUMN_JAWABAN)
        
        // Create sample questions
        val sampleQuestions = listOf(
            listOf("Apa ibukota Indonesia?", "Jakarta", "Bandung", "Surabaya", "Yogyakarta", "Semarang", "A"),
            listOf("2 + 2 = ?", "3", "4", "5", "6", "7", "B"),
            listOf("Bahasa pemrograman Android?", "Java", "Kotlin", "Python", "C++", "JavaScript", "B"),
            listOf("Apa kepanjangan HTML?", "Hyper Text Markup Language", "High Tech Modern Language", "Hyper Transfer Markup Language", "Home Tool Markup Language", "None of the above", "A"),
            listOf("Planet terdekat dari Matahari?", "Venus", "Mars", "Merkurius", "Jupiter", "Saturnus", "C"),
            listOf("Apa fungsi RAM?", "Menyimpan data permanen", "Memproses data", "Menyimpan data sementara", "Menghubungkan ke internet", "Menampilkan grafis", "C"),
            listOf("Siapa penemu komputer?", "Bill Gates", "Steve Jobs", "Charles Babbage", "Alan Turing", "Mark Zuckerberg", "C"),
            listOf("Apa itu API?", "Application Programming Interface", "Advanced Programming Interface", "Application Process Interface", "Advanced Process Interface", "None of the above", "A"),
            listOf("Bahasa database?", "HTML", "CSS", "SQL", "Java", "Python", "C"),
            listOf("Apa itu Git?", "Bahasa pemrograman", "Sistem kontrol versi", "Database", "Framework", "Operating System", "B"),
            listOf("CSS digunakan untuk?", "Memformat halaman web", "Membuat database", "Mengolah data", "Membuat game", "Membuat aplikasi mobile", "A"),
            listOf("Apa kepanjangan CSS?", "Cascading Style Sheets", "Computer Style Sheets", "Creative Style Sheets", "Colorful Style Sheets", "None of the above", "A")
        )
        
        sampleQuestions.forEachIndexed { index, question ->
            val row = sheet.createRow(index + 1)
            question.forEachIndexed { cellIndex, value ->
                row.createCell(cellIndex).setCellValue(value)
            }
        }
        
        // Save to cache directory
        val cacheDir = context.cacheDir
        val templateFile = File(cacheDir, "template_soal_quiz.xlsx")
        
        FileOutputStream(templateFile).use { outputStream ->
            workbook.write(outputStream)
        }
        
        workbook.close()
        
        return templateFile
    }
    
    fun generateSampleQuestions(roomId: String, count: Int = 10): List<Question> {
        val sampleQuestions = listOf(
            "Apa ibukota Indonesia?",
            "2 + 2 = ?",
            "Bahasa pemrograman Android?",
            "Apa kepanjangan HTML?",
            "Planet terdekat dari Matahari?",
            "Apa fungsi RAM?",
            "Siapa penemu komputer?",
            "Apa itu API?",
            "Bahasa database?",
            "Apa itu Git?",
            "CSS digunakan untuk?",
            "Apa kepanjangan CSS?"
        )
        
        val sampleOptions = listOf(
            listOf("Jakarta", "Bandung", "Surabaya", "Yogyakarta", "Semarang"),
            listOf("3", "4", "5", "6", "7"),
            listOf("Java", "Kotlin", "Python", "C++", "JavaScript"),
            listOf("Hyper Text Markup Language", "High Tech Modern Language", "Hyper Transfer Markup Language", "Home Tool Markup Language", "None of the above"),
            listOf("Venus", "Mars", "Merkurius", "Jupiter", "Saturnus"),
            listOf("Menyimpan data permanen", "Memproses data", "Menyimpan data sementara", "Menghubungkan ke internet", "Menampilkan grafis"),
            listOf("Bill Gates", "Steve Jobs", "Charles Babbage", "Alan Turing", "Mark Zuckerberg"),
            listOf("Application Programming Interface", "Advanced Programming Interface", "Application Process Interface", "Advanced Process Interface", "None of the above"),
            listOf("HTML", "CSS", "SQL", "Java", "Python"),
            listOf("Bahasa pemrograman", "Sistem kontrol versi", "Database", "Framework", "Operating System"),
            listOf("Memformat halaman web", "Membuat database", "Mengolah data", "Membuat game", "Membuat aplikasi mobile"),
            listOf("Cascading Style Sheets", "Computer Style Sheets", "Creative Style Sheets", "Colorful Style Sheets", "None of the above")
        )
        
        val sampleAnswers = listOf("A", "B", "B", "A", "C", "C", "C", "A", "C", "B", "A", "A")
        
        return (0 until minOf(count, sampleQuestions.size)).map { index ->
            Question(
                id = java.util.UUID.randomUUID().toString(),
                roomId = roomId,
                questionText = sampleQuestions[index],
                optionA = sampleOptions[index][0],
                optionB = sampleOptions[index][1],
                optionC = sampleOptions[index][2],
                optionD = sampleOptions[index][3],
                optionE = sampleOptions[index][4],
                correctAnswer = sampleAnswers[index],
                orderNumber = index + 1
            )
        }
    }
}
