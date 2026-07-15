package com.quizku.app.presentation.student.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizku.app.data.local.entity.Participant
import com.quizku.app.data.local.entity.QuizRoom
import com.quizku.app.data.local.entity.User
import com.quizku.app.data.local.repository.ParticipantRepository
import com.quizku.app.data.local.repository.QuizRoomRepository
import com.quizku.app.data.local.repository.UserRepository
import com.quizku.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class MasukRoomViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _joinedRoom = MutableLiveData<QuizRoom?>()
    val joinedRoom: LiveData<QuizRoom?> = _joinedRoom

    private val _joinedParticipantId = MutableLiveData<String?>()
    val joinedParticipantId: LiveData<String?> = _joinedParticipantId

    fun joinRoom(username: String, roomCode: String) {
        if (username.isBlank()) {
            _errorMessage.value = "Username tidak boleh kosong"
            return
        }
        if (roomCode.isBlank()) {
            _errorMessage.value = "Kode room tidak boleh kosong"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                val room = QuizRoomRepository.getRoomByCode(roomCode.uppercase())

                if (room == null) {
                    _errorMessage.postValue("Room tidak ditemukan")
                    _isLoading.postValue(false)
                    return@launch
                }

                if (room.status != Constants.STATUS_ACTIVE && room.status != Constants.STATUS_DRAFT) {
                    _errorMessage.postValue("Room sudah tidak aktif")
                    _isLoading.postValue(false)
                    return@launch
                }

                if (room.status == Constants.STATUS_ACTIVE && room.endsAt != null && room.endsAt < System.currentTimeMillis()) {
                    _errorMessage.postValue("Kuis sudah berakhir")
                    _isLoading.postValue(false)
                    return@launch
                }

                var user = UserRepository.getUserByUsername(username)
                if (user == null) {
                    user = User(
                        id = UUID.randomUUID().toString(),
                        username = username,
                        role = Constants.ROLE_STUDENT,
                        createdAt = System.currentTimeMillis()
                    )
                    UserRepository.insertUser(user)
                }

                val existingParticipant = ParticipantRepository.getParticipant(room.id, user.id)
                if (existingParticipant != null) {
                    _joinedParticipantId.postValue(existingParticipant.id)
                    _joinedRoom.postValue(room)
                    _successMessage.postValue("Anda sudah bergabung di room ini")
                    _isLoading.postValue(false)
                    return@launch
                }

                val participant = Participant(
                    id = UUID.randomUUID().toString(),
                    roomId = room.id,
                    userId = user.id,
                    joinedAt = System.currentTimeMillis()
                )
                ParticipantRepository.insertParticipant(participant)

                _joinedParticipantId.postValue(participant.id)
                _joinedRoom.postValue(room)
                _successMessage.postValue("Berhasil bergabung!")

            } catch (e: Exception) {
                _errorMessage.postValue("Gagal bergabung: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
