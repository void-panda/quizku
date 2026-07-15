package com.quizku.app.presentation.student.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizku.app.data.local.entity.Answer
import com.quizku.app.data.local.entity.Question
import com.quizku.app.data.local.repository.AnswerRepository
import com.quizku.app.data.local.repository.QuestionRepository
import com.quizku.app.data.local.repository.QuizRoomRepository
import com.quizku.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KuisViewModel(application: Application) : AndroidViewModel(application) {

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _currentQuestion = MutableLiveData<Question?>()
    val currentQuestion: LiveData<Question?> = _currentQuestion

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _selectedAnswers = MutableLiveData<Map<String, String>>()
    val selectedAnswers: LiveData<Map<String, String>> = _selectedAnswers

    private val _totalQuestions = MutableLiveData(0)
    val totalQuestions: LiveData<Int> = _totalQuestions

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long> = _timeRemaining

    private val _isQuizFinished = MutableLiveData(false)
    val isQuizFinished: LiveData<Boolean> = _isQuizFinished

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _correctCount = MutableLiveData(0)
    val correctCount: LiveData<Int> = _correctCount

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var countDownTimer: CountDownTimer? = null
    private var roomId: String = ""
    private var participantId: String = ""
    private var roomStatus: String = ""
    private var roomEndsAt: Long = -1L
    private var questionList: List<Question> = emptyList()

    fun initialize(roomId: String, participantId: String, roomStatus: String, roomEndsAt: Long) {
        this.roomId = roomId
        this.participantId = participantId
        this.roomStatus = roomStatus
        this.roomEndsAt = roomEndsAt
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                questionList = QuestionRepository.getQuestionsByRoom(roomId)
                val rng = java.util.Random(participantId.hashCode().toLong())
                questionList = questionList.shuffled(rng)
                _questions.postValue(questionList)
                _totalQuestions.postValue(questionList.size)

                val existingAnswers = AnswerRepository.getAnswersByParticipant(participantId)
                val answerMap = existingAnswers.associate { it.questionId to it.selectedAnswer }
                _selectedAnswers.postValue(answerMap)

                if (roomStatus == Constants.STATUS_LOCKED || roomStatus == Constants.STATUS_COMPLETED) {
                    _isQuizFinished.postValue(true)
                } else if (roomStatus == Constants.STATUS_ACTIVE && roomEndsAt > 0 && roomEndsAt < System.currentTimeMillis()) {
                    _isQuizFinished.postValue(true)
                } else if (roomEndsAt > 0) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        startTimer(roomEndsAt)
                    }
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    showQuestion(0)
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal memuat soal: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun showQuestion(index: Int) {
        if (index < 0 || index >= questionList.size) return
        _currentIndex.value = index
        _currentQuestion.value = questionList[index]
    }

    fun selectAnswer(questionId: String, answer: String) {
        val currentMap = _selectedAnswers.value?.toMutableMap() ?: mutableMapOf()
        currentMap[questionId] = answer
        _selectedAnswers.value = currentMap

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val question = questionList.find { it.id == questionId }
                val isCorrect = question?.correctAnswer == answer

                val existingAnswer = AnswerRepository.getAnswerByQuestion(participantId, questionId)
                if (existingAnswer != null) {
                    AnswerRepository.updateAnswer(existingAnswer.copy(selectedAnswer = answer, isCorrect = isCorrect))
                } else {
                    val newAnswer = Answer(
                        participantId = participantId,
                        questionId = questionId,
                        selectedAnswer = answer,
                        isCorrect = isCorrect,
                        answeredAt = System.currentTimeMillis()
                    )
                    AnswerRepository.insertAnswer(newAnswer)
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menyimpan jawaban: ${e.message}")
            }
        }
    }

    fun goToNext() {
        val current = _currentIndex.value ?: 0
        if (current < questionList.size - 1) {
            showQuestion(current + 1)
        }
    }

    fun goToPrevious() {
        val current = _currentIndex.value ?: 0
        if (current > 0) {
            showQuestion(current - 1)
        }
    }

    fun finishQuiz() {
        countDownTimer?.cancel()
        calculateScore()
    }

    private fun startTimer(endsAt: Long) {
        val now = System.currentTimeMillis()
        val remaining = endsAt - now

        if (remaining <= 0) {
            _timeRemaining.value = 0
            finishQuiz()
            return
        }

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
            }

            override fun onFinish() {
                _timeRemaining.value = 0
                autoCompleteRoom()
            }
        }.start()
    }

    private fun autoCompleteRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                QuizRoomRepository.updateStatus(roomId, Constants.STATUS_COMPLETED)
                calculateScore()
            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menyelesaikan room: ${e.message}")
            }
        }
    }

    private fun calculateScore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val total = questionList.size
                val correct = AnswerRepository.getCorrectAnswerCount(participantId)

                _correctCount.postValue(correct)
                _score.postValue(if (total > 0) (correct * 100 / total) else 0)
                _isQuizFinished.postValue(true)

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menghitung skor: ${e.message}")
            }
        }
    }

    fun getTimeRemainingFormatted(): String {
        val millis = _timeRemaining.value ?: 0
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getAnsweredCount(): Int {
        return _selectedAnswers.value?.size ?: 0
    }

    fun getProgressPercent(): Int {
        val answered = getAnsweredCount()
        val total = _totalQuestions.value ?: 1
        return if (total > 0) (answered * 100 / total) else 0
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
