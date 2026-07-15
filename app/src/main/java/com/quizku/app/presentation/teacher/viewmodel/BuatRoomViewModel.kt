package com.quizku.app.presentation.teacher.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizku.app.QuizKuApplication
import com.quizku.app.data.local.entity.Question
import com.quizku.app.data.local.entity.QuizRoom
import com.quizku.app.data.local.repository.QuestionRepository
import com.quizku.app.data.local.repository.QuizRoomRepository
import com.quizku.app.util.Constants
import com.quizku.app.util.ExcelParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

class BuatRoomViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentRoom = MutableLiveData<QuizRoom?>()
    val currentRoom: LiveData<QuizRoom?> = _currentRoom

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _parseResult = MutableLiveData<ExcelParser.ParseResult?>()
    val parseResult: LiveData<ExcelParser.ParseResult?> = _parseResult

    private val currentTeacherId: String = Constants.TEACHER_DEMO_ID

    fun createRoom(title: String, durationMinutes: Int) {
        if (title.isBlank()) {
            _errorMessage.value = "Judul kuis tidak boleh kosong"
            return
        }

        if (durationMinutes <= 0) {
            _errorMessage.value = "Durasi harus lebih dari 0 menit"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                withTimeout(15_000L) {
                    val roomCode = generateRoomCode()

                    val quizRoom = QuizRoom(
                        id = UUID.randomUUID().toString(),
                        roomCode = roomCode,
                        teacherId = currentTeacherId,
                        title = title,
                        status = Constants.STATUS_DRAFT,
                        durationMinutes = durationMinutes,
                        createdAt = System.currentTimeMillis()
                    )

                    QuizRoomRepository.insertRoom(quizRoom)
                    _currentRoom.postValue(quizRoom)
                    _successMessage.postValue("Room berhasil dibuat dengan kode: $roomCode")
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal membuat room: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun parseExcelFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                val context = getApplication<QuizKuApplication>()
                val inputStream = context.contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    _errorMessage.postValue("Gagal membaca file")
                    _isLoading.postValue(false)
                    return@launch
                }

                val roomId = _currentRoom.value?.id ?: run {
                    _errorMessage.postValue("Buat room terlebih dahulu")
                    _isLoading.postValue(false)
                    return@launch
                }

                val result = ExcelParser.parseQuestions(inputStream, roomId)
                inputStream.close()

                _parseResult.postValue(result)

                if (result.success) {
                    _questions.postValue(result.questions)
                    _successMessage.postValue("Berhasil parse ${result.questions.size} soal")
                } else {
                    _errorMessage.postValue(result.errorMessage)
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal parse Excel: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveQuestions() {
        val currentQuestions = _questions.value
        val roomId = _currentRoom.value?.id

        if (currentQuestions.isNullOrEmpty() || roomId == null) {
            _errorMessage.value = "Tidak ada soal untuk disimpan"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                withTimeout(15_000L) {
                    QuestionRepository.insertAllQuestions(currentQuestions)
                }
                _successMessage.postValue("Berhasil menyimpan ${currentQuestions.size} soal")

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menyimpan soal: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun startQuiz() {
        val room = _currentRoom.value ?: run {
            _errorMessage.value = "Room tidak ditemukan"
            return
        }

        val questionCount = _questions.value?.size ?: 0
        if (questionCount == 0) {
            _errorMessage.value = "Upload soal terlebih dahulu"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                withTimeout(15_000L) {
                    val startedAt = System.currentTimeMillis()
                    val endsAt = startedAt + (room.durationMinutes * 60 * 1000L)

                    QuizRoomRepository.updateStartTime(room.id, startedAt, endsAt)
                    QuizRoomRepository.updateStatus(room.id, Constants.STATUS_ACTIVE)

                    val updatedRoom = room.copy(
                        status = Constants.STATUS_ACTIVE,
                        startedAt = startedAt,
                        endsAt = endsAt
                    )
                    _currentRoom.postValue(updatedRoom)
                }
                _successMessage.postValue("Kuis berhasil dimulai!")

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal memulai kuis: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    private fun generateRoomCode(): String {
        return (1..Constants.ROOM_CODE_LENGTH)
            .map { Constants.ROOM_CODE_CHARS.random() }
            .joinToString("")
    }
}
