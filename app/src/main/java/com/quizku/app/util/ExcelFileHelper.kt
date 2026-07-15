package com.quizku.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object ExcelFileHelper {
    
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "unknown.xlsx"
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        
        return fileName
    }
    
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    fun isValidExcelFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName)
        return extension in listOf("xlsx", "xls")
    }
    
    fun isValidFileSize(context: Context, uri: Uri, maxSizeMB: Int = 5): Boolean {
        var fileSize = 0L
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }
        
        val maxSizeBytes = maxSizeMB * 1024 * 1024L
        return fileSize <= maxSizeBytes
    }
}
