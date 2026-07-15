package com.quizku.app.presentation.teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.card.MaterialCardView
import com.quizku.app.R
import com.quizku.app.presentation.teacher.adapter.StudentAdapter
import com.quizku.app.presentation.teacher.viewmodel.MonitoringViewModel
import com.quizku.app.util.Constants
import kotlinx.coroutines.launch
import java.io.File

class MonitoringRoomActivity : AppCompatActivity() {
    
    private lateinit var viewModel: MonitoringViewModel
    private lateinit var adapter: StudentAdapter
    private lateinit var tvRoomCode: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvTimerStatus: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var tvQuestionCount: TextView
    private lateinit var rvStudents: RecyclerView
    private lateinit var layoutQuestions: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var btnStartQuiz: MaterialButton
    private lateinit var btnCopyCode: MaterialButton
    private lateinit var loadingOverlay: android.widget.FrameLayout
    private lateinit var paginationRow: LinearLayout
    private lateinit var btnPrevPage: ImageView
    private lateinit var btnNextPage: ImageView
    private lateinit var tvPageInfo: TextView
    
    private val PAGE_SIZE = 5
    private var currentPage = 0
    private var allQuestions = listOf<com.quizku.app.data.local.entity.Question>()
    
    private var roomId: String? = null
    private var countDownTimer: CountDownTimer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring_room)
        
        roomId = intent.getStringExtra("ROOM_ID")
        
        initViews()
        setupRecyclerView()
        setupViewModel()
        setupClickListeners()
        
        roomId?.let { viewModel.loadRoom(it) }
    }
    
    override fun onResume() {
        super.onResume()
        roomId?.let { viewModel.startAutoRefresh(it) }
    }
    
    override fun onPause() {
        super.onPause()
        viewModel.stopAutoRefresh()
        countDownTimer?.cancel()
    }
    
    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvTimer = findViewById(R.id.tvTimer)
        tvTimerStatus = findViewById(R.id.tvTimerStatus)
        tvStudentCount = findViewById(R.id.tvStudentCount)
        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        rvStudents = findViewById(R.id.rvStudents)
        layoutQuestions = findViewById(R.id.layoutQuestions)
        emptyState = findViewById(R.id.emptyState)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        btnCopyCode = findViewById(R.id.btnCopyCode)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        paginationRow = findViewById(R.id.paginationRow)
        btnPrevPage = findViewById(R.id.btnPrevPage)
        btnNextPage = findViewById(R.id.btnNextPage)
        tvPageInfo = findViewById(R.id.tvPageInfo)
    }
    
    private fun setupRecyclerView() {
        adapter = StudentAdapter()
        adapter.onItemClick = { participantId, participantName ->
            val room = viewModel.currentRoom.value
            if (room != null) {
                val bottomSheet = StudentDetailBottomSheet.newInstance(participantId, participantName, room.id)
                bottomSheet.show(supportFragmentManager, "student_detail")
            }
        }
        rvStudents.apply {
            layoutManager = LinearLayoutManager(this@MonitoringRoomActivity)
            adapter = this@MonitoringRoomActivity.adapter
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MonitoringViewModel::class.java]
        
        viewModel.currentRoom.observe(this) { room ->
            room?.let {
                tvRoomCode.text = it.roomCode
                updateUIBasedOnStatus(it.status)
                updateTimer(it.endsAt)
            }
        }
        
        viewModel.participants.observe(this) { participants ->
            adapter.submitList(participants)
            
            if (participants.isEmpty()) {
                rvStudents.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            } else {
                rvStudents.visibility = View.VISIBLE
                emptyState.visibility = View.GONE
            }
        }
        
        viewModel.participantCount.observe(this) { count ->
            tvStudentCount.text = "$count siswa"
        }
        
        viewModel.questions.observe(this) { questions ->
            allQuestions = questions
            currentPage = 0
            updateQuestionPage()
        }
        
        viewModel.participantProgress.observe(this) { progressMap ->
            adapter.updateProgress(progressMap)
        }
        
        viewModel.participantNames.observe(this) { nameMap ->
            adapter.updateNames(nameMap)
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
    }
    
    private fun setupClickListeners() {
        btnStartQuiz.setOnClickListener {
            roomId?.let { id ->
                val room = viewModel.currentRoom.value
                val status = room?.status
                
                if (status == Constants.STATUS_DRAFT) {
                    viewModel.startQuiz(id)
                } else if (status == Constants.STATUS_ACTIVE) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Akhiri Kuis?")
                        .setMessage("Semua siswa tidak akan bisa menjawab soal lagi. Yakin ingin mengakhiri?")
                        .setPositiveButton("Ya, Akhiri") { _, _ -> viewModel.stopQuiz(id) }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }
        }
        
        btnCopyCode.setOnClickListener {
            val roomCode = tvRoomCode.text.toString()
            if (roomCode != "-") {
                copyToClipboard(roomCode)
            }
        }
        
        val btnExportCsv = findViewById<MaterialButton>(R.id.btnExportCsv)
        btnExportCsv.setOnClickListener {
            exportCsv()
        }

        btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateQuestionPage()
            }
        }

        btnNextPage.setOnClickListener {
            if ((currentPage + 1) * PAGE_SIZE < allQuestions.size) {
                currentPage++
                updateQuestionPage()
            }
        }
    }
    
    private fun updateQuestionPage() {
        val total = allQuestions.size
        if (total == 0) {
            paginationRow.visibility = View.GONE
            layoutQuestions.removeAllViews()
            tvQuestionCount.text = "0 soal"
            return
        }

        val totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE
        if (total <= PAGE_SIZE) {
            paginationRow.visibility = View.GONE
        } else {
            paginationRow.visibility = View.VISIBLE
            val start = currentPage * PAGE_SIZE + 1
            val end = minOf((currentPage + 1) * PAGE_SIZE, total)
            tvPageInfo.text = "$start-$end dari $total soal"
            btnPrevPage.alpha = if (currentPage > 0) 1f else 0.3f
            btnNextPage.alpha = if ((currentPage + 1) * PAGE_SIZE < total) 1f else 0.3f
            btnPrevPage.isEnabled = currentPage > 0
            btnNextPage.isEnabled = (currentPage + 1) * PAGE_SIZE < total
        }

        val start = currentPage * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, total)
        layoutQuestions.removeAllViews()
        for (i in start until end) {
            val question = allQuestions[i]
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_question_monitoring, layoutQuestions, false)
            itemView.findViewById<TextView>(R.id.tvNumber).text = question.orderNumber.toString()
            itemView.findViewById<TextView>(R.id.tvQuestionText).text = question.questionText
            itemView.findViewById<TextView>(R.id.tvCorrectAnswer).text = "Jawaban: ${question.correctAnswer}"
            layoutQuestions.addView(itemView)
        }
        tvQuestionCount.text = "Halaman ${currentPage + 1}/$totalPages"
    }
    
    private fun exportCsv() {
        val room = viewModel.currentRoom.value ?: return
        lifecycleScope.launch {
            try {
                val csvData = viewModel.generateCsvData(room.id, room.title, room.roomCode)
                val fileName = "laporan_${room.roomCode}.csv"
                val file = File(cacheDir, fileName)
                file.writeText(csvData)
                
                val uri = FileProvider.getUriForFile(
                    this@MonitoringRoomActivity,
                    "${packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Laporan Kuis: ${room.title}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan"))
            } catch (e: Exception) {
                Toast.makeText(this@MonitoringRoomActivity, "Gagal export: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateUIBasedOnStatus(status: String) {
        when (status) {
            Constants.STATUS_DRAFT -> {
                btnStartQuiz.text = "Mulai"
                btnStartQuiz.isEnabled = true
                btnStartQuiz.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(this, R.color.primary)
                tvTimerStatus.text = "Belum dimulai"
            }
            Constants.STATUS_ACTIVE -> {
                btnStartQuiz.text = "Akhiri"
                btnStartQuiz.isEnabled = true
                btnStartQuiz.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(this, R.color.error)
                tvTimerStatus.text = "Kuis berlangsung"
            }
            Constants.STATUS_LOCKED -> {
                btnStartQuiz.text = "TERKUNCI"
                btnStartQuiz.isEnabled = false
                btnStartQuiz.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(this, R.color.surface_container_highest)
                tvTimerStatus.text = "Kuis sudah selesai"
            }
            Constants.STATUS_COMPLETED -> {
                btnStartQuiz.text = "SELESAI"
                btnStartQuiz.isEnabled = false
                btnStartQuiz.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(this, R.color.surface_container_highest)
                tvTimerStatus.text = "Kuis selesai"
            }
        }
    }
    
    private fun updateTimer(endsAt: Long?) {
        countDownTimer?.cancel()
        
        val room = viewModel.currentRoom.value
        if (room?.status != Constants.STATUS_ACTIVE) {
            tvTimer.text = "00:00"
            return
        }
        
        if (endsAt == null) {
            tvTimer.text = "00:00"
            return
        }
        
        val now = System.currentTimeMillis()
        val remaining = endsAt - now
        
        if (remaining <= 0) {
            tvTimer.text = "00:00"
            tvTimerStatus.text = "Waktu habis"
            return
        }
        
        countDownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {
                tvTimer.text = "00:00"
                tvTimerStatus.text = "Waktu habis"
                roomId?.let { viewModel.loadRoom(it) }
            }
        }.start()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Room Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Kode room disalin: $text", Toast.LENGTH_SHORT).show()
    }
}
