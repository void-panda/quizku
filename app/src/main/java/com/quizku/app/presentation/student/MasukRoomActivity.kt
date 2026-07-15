package com.quizku.app.presentation.student

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.quizku.app.R
import com.quizku.app.presentation.student.viewmodel.MasukRoomViewModel
import com.quizku.app.util.Constants

class MasukRoomActivity : AppCompatActivity() {

    private val viewModel: MasukRoomViewModel by viewModels()
    private lateinit var etUsername: EditText
    private lateinit var etRoomCode: EditText
    private lateinit var btnJoin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masuk_room)

        initViews()
        setupViewModel()
    }

    private fun initViews() {
        findViewById<MaterialToolbar>(R.id.toolbar).setOnClickListener { finish() }
        etUsername = findViewById(R.id.etUsername)
        etRoomCode = findViewById(R.id.etRoomCode)
        btnJoin = findViewById(R.id.btnJoin)

        btnJoin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val roomCode = etRoomCode.text.toString().trim()
            viewModel.joinRoom(username, roomCode)
        }
    }

    private fun setupViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            val overlay = findViewById<android.widget.FrameLayout>(R.id.loadingOverlay)
            overlay.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
            btnJoin.isEnabled = !loading
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        viewModel.joinedRoom.observe(this) { room ->
            room?.let {
                val participantId = viewModel.joinedParticipantId.value ?: return@let

                val intent = Intent(this, KuisActivity::class.java).apply {
                    putExtra("ROOM_ID", it.id)
                    putExtra("PARTICIPANT_ID", participantId)
                    putExtra("ROOM_STATUS", it.status)
                    putExtra("ROOM_ENDS_AT", it.endsAt ?: -1L)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
