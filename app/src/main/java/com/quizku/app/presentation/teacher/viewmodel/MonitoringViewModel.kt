package com.quizku.app.presentation.teacher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizku.app.data.local.entity.Participant
import com.quizku.app.data.local.entity.Question
import com.quizku.app.data.local.entity.QuizRoom
import com.quizku.app.data.local.repository.AnswerRepository
import com.quizku.app.data.local.repository.ParticipantRepository
import com.quizku.app.data.local.repository.QuestionRepository
import com.quizku.app.data.local.repository.QuizRoomRepository
import com.quizku.app.data.local.repository.UserRepository
import com.quizku.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonitoringViewModel(application: Application) : AndroidViewModel(application) {

    data class StudentReport(
        val name: String,
        val answeredCount: Int,
        val correctCount: Int,
        val totalQuestions: Int
    )

    private val _currentRoom = MutableLiveData<QuizRoom?>()
    val currentRoom: LiveData<QuizRoom?> = _currentRoom

    private val _participants = MutableLiveData<List<Participant>>()
    val participants: LiveData<List<Participant>> = _participants

    private val _participantCount = MutableLiveData<Int>()
    val participantCount: LiveData<Int> = _participantCount

    private val _questionCount = MutableLiveData<Int>()
    val questionCount: LiveData<Int> = _questionCount

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _participantProgress = MutableLiveData<Map<String, Pair<Int, Int>>>()
    val participantProgress: LiveData<Map<String, Pair<Int, Int>>> = _participantProgress

    private val _participantNames = MutableLiveData<Map<String, String>>()
    val participantNames: LiveData<Map<String, String>> = _participantNames

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long> = _timeRemaining

    private var refreshJob: kotlinx.coroutines.Job? = null

    fun loadRoom(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                val room = QuizRoomRepository.getRoomById(roomId)
                _currentRoom.postValue(room)

                val questionCount = QuestionRepository.getQuestionCount(roomId)
                _questionCount.postValue(questionCount)

                val questionList = QuestionRepository.getQuestionsByRoom(roomId)
                _questions.postValue(questionList)

                loadParticipants(roomId)

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal memuat data: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadParticipants(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val participants = ParticipantRepository.getParticipantsByRoom(roomId)
                _participants.postValue(participants)
                _participantCount.postValue(participants.size)

                val progressMap = mutableMapOf<String, Pair<Int, Int>>()
                val nameMap = mutableMapOf<String, String>()
                val totalQuestions = QuestionRepository.getQuestionCount(roomId)

                participants.forEach { participant ->
                    val answeredCount = AnswerRepository.getTotalAnswerCount(participant.id)
                    progressMap[participant.id] = Pair(answeredCount, totalQuestions)

                    val user = UserRepository.getUserById(participant.userId)
                    nameMap[participant.id] = user?.username ?: "Siswa"
                }

                _participantProgress.postValue(progressMap)
                _participantNames.postValue(nameMap)

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal memuat peserta: ${e.message}")
            }
        }
    }

    fun startAutoRefresh(roomId: String, intervalMs: Long = 5000) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                loadParticipants(roomId)
                updateTimer()
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private fun updateTimer() {
        val room = _currentRoom.value ?: return

        if (room.status != Constants.STATUS_ACTIVE || room.endsAt == null) {
            _timeRemaining.postValue(0)
            return
        }

        val now = System.currentTimeMillis()
        val remaining = room.endsAt - now

        if (remaining <= 0) {
            _timeRemaining.postValue(0)
            completeRoom(room.id)
        } else {
            _timeRemaining.postValue(remaining)
        }
    }

    private fun completeRoom(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                QuizRoomRepository.updateStatus(roomId, Constants.STATUS_COMPLETED)
                val room = QuizRoomRepository.getRoomById(roomId)
                _currentRoom.postValue(room)

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menyelesaikan room: ${e.message}")
            }
        }
    }

    fun stopQuiz(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                QuizRoomRepository.updateStatus(roomId, Constants.STATUS_COMPLETED)
                val room = QuizRoomRepository.getRoomById(roomId)
                _currentRoom.postValue(room)
                stopAutoRefresh()

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menghentikan kuis: ${e.message}")
            }
        }
    }

    fun startQuiz(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val room = QuizRoomRepository.getRoomById(roomId) ?: return@launch
                val startedAt = System.currentTimeMillis()
                val endsAt = startedAt + (room.durationMinutes * 60 * 1000L)

                QuizRoomRepository.updateStartTime(roomId, startedAt, endsAt)
                QuizRoomRepository.updateStatus(roomId, Constants.STATUS_ACTIVE)

                val updatedRoom = room.copy(
                    status = Constants.STATUS_ACTIVE,
                    startedAt = startedAt,
                    endsAt = endsAt
                )
                _currentRoom.postValue(updatedRoom)
                startAutoRefresh(roomId)

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal memulai kuis: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
    }

    suspend fun generateCsvData(roomId: String, roomTitle: String, roomCode: String): String = withContext(Dispatchers.IO) {
        val participants = ParticipantRepository.getParticipantsByRoom(roomId)
        val totalQuestions = QuestionRepository.getQuestionCount(roomId)

        val sb = StringBuilder()
        sb.appendLine("Laporan Kuis: $roomTitle")
        sb.appendLine("Kode Room: $roomCode")
        sb.appendLine("Total Soal: $totalQuestions")
        sb.appendLine()
        sb.appendLine("Nama,Jumlah Benar,Jumlah Salah,Tidak Dijawab,Total Soal")

        participants.forEach { participant ->
            val user = UserRepository.getUserById(participant.userId)
            val name = user?.username ?: "Siswa"
            val correct = AnswerRepository.getCorrectAnswerCount(participant.id)
            val answered = AnswerRepository.getTotalAnswerCount(participant.id)
            val wrong = answered - correct
            val unanswered = totalQuestions - answered
            sb.appendLine("$name,$correct,$wrong,$unanswered,$totalQuestions")
        }

        sb.toString()
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
