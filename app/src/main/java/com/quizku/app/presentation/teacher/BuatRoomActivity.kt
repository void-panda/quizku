package com.quizku.app.presentation.teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.quizku.app.R
import com.quizku.app.presentation.teacher.viewmodel.BuatRoomViewModel

class BuatRoomActivity : AppCompatActivity() {
    
    private lateinit var viewModel: BuatRoomViewModel
    private lateinit var etTitle: EditText
    private lateinit var etDuration: EditText
    private lateinit var tvFileName: TextView
    private lateinit var cardSummary: MaterialCardView
    private lateinit var cardRoomCode: MaterialCardView
    private lateinit var tvRoomCode: TextView
    private lateinit var tvSummaryRoomCode: TextView
    private lateinit var tvQuestionCount: TextView
    private lateinit var btnGenerateRoom: MaterialButton
    private lateinit var btnStartQuiz: MaterialButton
    private lateinit var btnNextStep: MaterialButton
    private lateinit var btnPrevStep: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    
    private lateinit var step1Container: LinearLayout
    private lateinit var step2Container: LinearLayout
    private lateinit var step1Circle: View
    private lateinit var step2Circle: View
    private lateinit var step1Label: TextView
    private lateinit var step2Label: TextView
    private lateinit var stepConnector: View
    
    private val pickExcel = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleExcelFile(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buat_room)
        
        initViews()
        setupViewModel()
        setupClickListeners()
    }
    
    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        etTitle = findViewById(R.id.etTitle)
        etDuration = findViewById(R.id.etDuration)
        tvFileName = findViewById(R.id.tvFileName)
        cardSummary = findViewById(R.id.cardSummary)
        cardRoomCode = findViewById(R.id.cardRoomCode)
        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvSummaryRoomCode = findViewById(R.id.tvSummaryRoomCode)
        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        btnGenerateRoom = findViewById(R.id.btnGenerateRoom)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        btnNextStep = findViewById(R.id.btnNextStep)
        btnPrevStep = findViewById(R.id.btnPrevStep)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        
        step1Container = findViewById(R.id.step1Container)
        step2Container = findViewById(R.id.step2Container)
        step1Circle = findViewById(R.id.step1Circle)
        step2Circle = findViewById(R.id.step2Circle)
        step1Label = findViewById(R.id.step1Label)
        step2Label = findViewById(R.id.step2Label)
        stepConnector = findViewById(R.id.stepConnector)
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[BuatRoomViewModel::class.java]
        
        viewModel.currentRoom.observe(this) { room ->
            room?.let {
                cardRoomCode.visibility = View.VISIBLE
                tvRoomCode.text = it.roomCode
                tvSummaryRoomCode.text = it.roomCode
                btnGenerateRoom.text = "SALIN KODE"
                btnNextStep.visibility = View.VISIBLE
                btnStartQuiz.isEnabled = viewModel.questions.value?.isNotEmpty() == true
            }
        }
        
        viewModel.questions.observe(this) { questions ->
            tvQuestionCount.text = "${questions.size} soal"
            btnStartQuiz.isEnabled = viewModel.currentRoom.value != null
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
        
        viewModel.successMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }
    
    private fun setupClickListeners() {
        btnGenerateRoom.setOnClickListener {
            if (viewModel.currentRoom.value != null) {
                val roomCode = viewModel.currentRoom.value?.roomCode ?: return@setOnClickListener
                copyToClipboard(roomCode)
            } else {
                val title = etTitle.text.toString().trim()
                val duration = etDuration.text.toString().toIntOrNull() ?: 30
                viewModel.createRoom(title, duration)
            }
        }
        
        btnNextStep.setOnClickListener {
            showStep(2)
        }
        
        btnPrevStep.setOnClickListener {
            showStep(1)
        }
        
        btnStartQuiz.setOnClickListener {
            viewModel.saveQuestions()
            viewModel.startQuiz()
            
            val room = viewModel.currentRoom.value
            room?.let {
                val intent = Intent(this, MonitoringRoomActivity::class.java).apply {
                    putExtra("ROOM_ID", it.id)
                }
                startActivity(intent)
                finish()
            }
        }
        
        val cardUpload = findViewById<MaterialCardView>(R.id.cardUpload)
        cardUpload.setOnClickListener {
            pickExcel.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }
    }
    
    private fun showStep(step: Int) {
        if (step == 1) {
            step1Container.visibility = View.VISIBLE
            step2Container.visibility = View.GONE
            step1Circle.setBackgroundResource(R.drawable.bg_step_circle_active)
            step2Circle.setBackgroundResource(R.drawable.bg_step_circle_inactive)
            step1Label.setTextColor(getColor(R.color.primary))
            step2Label.setTextColor(getColor(R.color.on_surface_variant))
        } else {
            step1Container.visibility = View.GONE
            step2Container.visibility = View.VISIBLE
            step1Circle.setBackgroundResource(R.drawable.bg_step_circle_inactive)
            step2Circle.setBackgroundResource(R.drawable.bg_step_circle_active)
            step1Label.setTextColor(getColor(R.color.on_surface_variant))
            step2Label.setTextColor(getColor(R.color.primary))
        }
    }
    
    private fun handleExcelFile(uri: Uri) {
        val fileName = getFileName(uri)
        tvFileName.text = fileName
        tvFileName.visibility = View.VISIBLE
        viewModel.parseExcelFile(uri)
    }
    
    private fun getFileName(uri: Uri): String {
        var fileName = "unknown.xlsx"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Room Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Kode room disalin: $text", Toast.LENGTH_SHORT).show()
    }
}
