package com.quizku.app.util

import com.quizku.app.data.local.entity.Question
import java.io.InputStream
import java.util.UUID
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.CellType

object ExcelParser {
    
    data class ParseResult(
        val success: Boolean,
        val questions: List<Question> = emptyList(),
        val errorMessage: String? = null
    )
    
    fun parseQuestions(inputStream: InputStream, roomId: String): ParseResult {
        return try {
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            
            // Validate header row
            val headerRow = sheet.getRow(0)
            if (!validateHeader(headerRow)) {
                return ParseResult(
                    success = false,
                    errorMessage = "Format kolom tidak sesuai. Pastikan header: SOAL, OPSI_A, OPSI_B, OPSI_C, OPSI_D, OPSI_E, JAWABAN"
                )
            }
            
            // Validate row count
            val rowCount = sheet.lastRowNum
            if (rowCount < Constants.MIN_QUESTIONS) {
                return ParseResult(
                    success = false,
                    errorMessage = "Minimal ${Constants.MIN_QUESTIONS} soal"
                )
            }
            if (rowCount > Constants.MAX_QUESTIONS) {
                return ParseResult(
                    success = false,
                    errorMessage = "Maksimal ${Constants.MAX_QUESTIONS} soal"
                )
            }
            
            // Parse questions
            val questions = mutableListOf<Question>()
            
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i)
                if (row == null) {
                    return ParseResult(
                        success = false,
                        errorMessage = "Baris ${i + 1} kosong"
                    )
                }
                
                // Validate cells
                val soal = getCellStringValue(row.getCell(0))
                val opsiA = getCellStringValue(row.getCell(1))
                val opsiB = getCellStringValue(row.getCell(2))
                val opsiC = getCellStringValue(row.getCell(3))
                val opsiD = getCellStringValue(row.getCell(4))
                val opsiE = getCellStringValue(row.getCell(5))
                val jawaban = getCellStringValue(row.getCell(6))
                
                if (soal.isBlank() || opsiA.isBlank() || opsiB.isBlank() || 
                    opsiC.isBlank() || opsiD.isBlank() || opsiE.isBlank() || jawaban.isBlank()) {
                    return ParseResult(
                        success = false,
                        errorMessage = "Baris ${i + 1} ada sel yang kosong"
                    )
                }
                
                if (soal.length > Constants.MAX_QUESTION_LENGTH) {
                    return ParseResult(
                        success = false,
                        errorMessage = "Baris ${i + 1}: Soal melebihi ${Constants.MAX_QUESTION_LENGTH} karakter"
                    )
                }
                
                if (opsiA.length > Constants.MAX_OPTION_LENGTH || opsiB.length > Constants.MAX_OPTION_LENGTH ||
                    opsiC.length > Constants.MAX_OPTION_LENGTH || opsiD.length > Constants.MAX_OPTION_LENGTH ||
                    opsiE.length > Constants.MAX_OPTION_LENGTH) {
                    return ParseResult(
                        success = false,
                        errorMessage = "Baris ${i + 1}: Opsi melebihi ${Constants.MAX_OPTION_LENGTH} karakter"
                    )
                }
                
                val jawabanUpper = jawaban.uppercase()
                if (jawabanUpper !in Constants.VALID_ANSWERS) {
                    return ParseResult(
                        success = false,
                        errorMessage = "Baris ${i + 1}: Jawaban tidak valid (harus A-E)"
                    )
                }
                
                questions.add(
                    Question(
                        id = UUID.randomUUID().toString(),
                        roomId = roomId,
                        questionText = soal,
                        optionA = opsiA,
                        optionB = opsiB,
                        optionC = opsiC,
                        optionD = opsiD,
                        optionE = opsiE,
                        correctAnswer = jawabanUpper,
                        orderNumber = i
                    )
                )
            }
            
            workbook.close()
            
            ParseResult(
                success = true,
                questions = questions
            )
            
        } catch (e: Exception) {
            ParseResult(
                success = false,
                errorMessage = "Gagal membaca file: ${e.message}"
            )
        }
    }
    
    private fun validateHeader(headerRow: org.apache.poi.ss.usermodel.Row?): Boolean {
        if (headerRow == null) return false
        
        val expectedHeaders = listOf(
            Constants.EXCEL_COLUMN_SOAL,
            Constants.EXCEL_COLUMN_OPSI_A,
            Constants.EXCEL_COLUMN_OPSI_B,
            Constants.EXCEL_COLUMN_OPSI_C,
            Constants.EXCEL_COLUMN_OPSI_D,
            Constants.EXCEL_COLUMN_OPSI_E,
            Constants.EXCEL_COLUMN_JAWABAN
        )
        
        for (i in expectedHeaders.indices) {
            val cell = headerRow.getCell(i)
            val cellValue = getCellStringValue(cell).uppercase()
            if (cellValue != expectedHeaders[i]) {
                return false
            }
        }
        
        return true
    }
    
    private fun getCellStringValue(cell: org.apache.poi.ss.usermodel.Cell?): String {
        if (cell == null) return ""
        
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }
}
