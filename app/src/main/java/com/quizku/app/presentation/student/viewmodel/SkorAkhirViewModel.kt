package com.quizku.app.presentation.student.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizku.app.data.local.repository.AnswerRepository
import com.quizku.app.data.local.repository.QuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkorAkhirViewModel(application: Application) : AndroidViewModel(application) {

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _correctCount = MutableLiveData(0)
    val correctCount: LiveData<Int> = _correctCount

    private val _totalQuestions = MutableLiveData(0)
    val totalQuestions: LiveData<Int> = _totalQuestions

    private val _unansweredCount = MutableLiveData(0)
    val unansweredCount: LiveData<Int> = _unansweredCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    data class WrongDetail(val questionText: String, val studentAnswer: String, val correctAnswer: String)

    private val _wrongDetails = MutableLiveData<List<WrongDetail>>()
    val wrongDetails: LiveData<List<WrongDetail>> = _wrongDetails

    fun loadResult(participantId: String, roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                val total = QuestionRepository.getQuestionCount(roomId)
                val correct = AnswerRepository.getCorrectAnswerCount(participantId)
                val answered = AnswerRepository.getTotalAnswerCount(participantId)

                _totalQuestions.postValue(total)
                _correctCount.postValue(correct)
                _unansweredCount.postValue(total - answered)
                _score.postValue(if (total > 0) (correct * 100 / total) else 0)

                val allAnswers = AnswerRepository.getAnswersByParticipant(participantId)
                val wrongDetails = allAnswers
                    .filter { !it.isCorrect }
                    .mapNotNull { answer ->
                        val question = QuestionRepository.getQuestionById(answer.questionId)
                        question?.let { WrongDetail(it.questionText, answer.selectedAnswer, it.correctAnswer) }
                    }
                _wrongDetails.postValue(wrongDetails)

            } catch (e: Exception) {
                // silently fail, show 0
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
