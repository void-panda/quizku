package com.quizku.app.presentation.teacher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizku.app.data.local.entity.QuizRoom
import com.quizku.app.data.local.repository.QuestionRepository
import com.quizku.app.data.local.repository.QuizRoomRepository
import com.quizku.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _rooms = MutableLiveData<List<QuizRoom>>()
    val rooms: LiveData<List<QuizRoom>> = _rooms

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _teacherName = MutableLiveData("Guru")
    val teacherName: LiveData<String> = _teacherName

    private val _totalRooms = MutableLiveData(0)
    val totalRooms: LiveData<Int> = _totalRooms

    private val _activeRooms = MutableLiveData(0)
    val activeRooms: LiveData<Int> = _activeRooms

    private val _completedRooms = MutableLiveData(0)
    val completedRooms: LiveData<Int> = _completedRooms

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                _teacherName.postValue("Guru")
                val rooms = QuizRoomRepository.getRoomsByTeacher(Constants.TEACHER_DEMO_ID)
                _rooms.postValue(rooms)
                _totalRooms.postValue(rooms.size)
                _activeRooms.postValue(rooms.count { it.status == Constants.STATUS_ACTIVE })
                _completedRooms.postValue(rooms.count { it.status == Constants.STATUS_COMPLETED || it.status == Constants.STATUS_LOCKED })

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal memuat data: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    suspend fun getQuestionCount(roomId: String): Int {
        return QuestionRepository.getQuestionCount(roomId)
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                QuestionRepository.deleteQuestionsByRoom(roomId)
                QuizRoomRepository.deleteRoom(roomId)
                loadRooms()

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal menghapus room: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
    }
}
