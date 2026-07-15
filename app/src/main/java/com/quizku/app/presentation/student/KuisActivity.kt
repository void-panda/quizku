package com.quizku.app.presentation.student

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.quizku.app.R
import com.quizku.app.data.local.entity.Question
import com.quizku.app.presentation.student.viewmodel.KuisViewModel
import com.quizku.app.util.Constants

class KuisActivity : AppCompatActivity() {

    private val viewModel: KuisViewModel by viewModels()
    private lateinit var tvTimer: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvAnsweredCount: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var btnOptionA: MaterialButton
    private lateinit var btnOptionB: MaterialButton
    private lateinit var btnOptionC: MaterialButton
    private lateinit var btnOptionD: MaterialButton
    private lateinit var btnOptionE: MaterialButton
    private lateinit var btnPrev: MaterialButton
    private lateinit var btnNext: MaterialButton

    private var roomId: String = ""
    private var participantId: String = ""
    private var isLocked = false

    private val optionButtons = mutableListOf<MaterialButton>()
    private lateinit var lockOverlay: android.widget.FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuis)

        roomId = intent.getStringExtra("ROOM_ID") ?: ""
        participantId = intent.getStringExtra("PARTICIPANT_ID") ?: ""
        val roomStatus = intent.getStringExtra("ROOM_STATUS") ?: ""
        val roomEndsAt = intent.getLongExtra("ROOM_ENDS_AT", -1L)

        initViews()
        setupViewModel()
        viewModel.initialize(roomId, participantId, roomStatus, roomEndsAt)
    }

    override fun onBackPressed() {
        if (!isLocked) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Keluar Kuis?")
                .setMessage("Jawaban yang belum dikirim akan hilang.")
                .setPositiveButton("Ya") { _, _ -> finish() }
                .setNegativeButton("Tidak", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_question_list -> { showQuestionSheet(); true }
                else -> false
            }
        }

        tvTimer = findViewById(R.id.tvTimer)
        tvProgress = findViewById(R.id.tvProgress)
        tvAnsweredCount = findViewById(R.id.tvAnsweredCount)
        tvQuestion = findViewById(R.id.tvQuestion)
        progressBar = findViewById(R.id.progressBar)

        btnOptionA = findViewById<MaterialButton>(R.id.btnOptionA).also { optionButtons.add(it) }
        btnOptionB = findViewById<MaterialButton>(R.id.btnOptionB).also { optionButtons.add(it) }
        btnOptionC = findViewById<MaterialButton>(R.id.btnOptionC).also { optionButtons.add(it) }
        btnOptionD = findViewById<MaterialButton>(R.id.btnOptionD).also { optionButtons.add(it) }
        btnOptionE = findViewById<MaterialButton>(R.id.btnOptionE).also { optionButtons.add(it) }

        lockOverlay = findViewById(R.id.lockScreenOverlay)
        val lockModal = findViewById<com.google.android.material.card.MaterialCardView>(R.id.lockModal)

        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        btnPrev.setOnClickListener { viewModel.goToPrevious() }
        btnNext.setOnClickListener {
            val current = viewModel.currentIndex.value ?: 0
            val total = viewModel.totalQuestions.value ?: 0
            if (current >= total - 1) {
                if (isLocked) {
                    navigateToResult()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Selesaikan Kuis?")
                        .setMessage("Pastikan semua soal sudah terjawab. Kamu tidak bisa mengubah jawaban setelah menyelesaikan kuis.")
                        .setPositiveButton("Ya, Selesaikan") { _, _ -> viewModel.finishQuiz() }
                        .setNegativeButton("Periksa Lagi", null)
                        .show()
                }
            } else {
                viewModel.goToNext()
            }
        }

        lockModal.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewResult)
            .setOnClickListener { navigateToResult() }

        // Option click listeners
        btnOptionA.setOnClickListener { if (!isLocked) selectOption("A") }
        btnOptionB.setOnClickListener { if (!isLocked) selectOption("B") }
        btnOptionC.setOnClickListener { if (!isLocked) selectOption("C") }
        btnOptionD.setOnClickListener { if (!isLocked) selectOption("D") }
        btnOptionE.setOnClickListener { if (!isLocked) selectOption("E") }
    }

    private fun setupViewModel() {
        viewModel.currentQuestion.observe(this) { question ->
            question?.let { displayQuestion(it) }
        }

        viewModel.currentIndex.observe(this) { index ->
            updateNavigationButtons(index)
        }

        viewModel.selectedAnswers.observe(this) { answers ->
            highlightSelected()
            updateAnsweredCount()
        }

        viewModel.totalQuestions.observe(this) { total ->
            tvAnsweredCount.text = "0/$total"
            val menu = findViewById<MaterialToolbar>(R.id.toolbar).menu
            menu?.findItem(R.id.action_question_list)?.isEnabled = total > 0
        }

        viewModel.isQuizFinished.observe(this) { finished ->
            if (finished) showLockOverlay()
        }

        viewModel.isLoading.observe(this) { loading ->
            findViewById<android.widget.FrameLayout>(R.id.loadingOverlay).visibility =
                if (loading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Real-time timer update
        viewModel.timeRemaining.observe(this) { millis ->
            updateTimerDisplay(millis)
        }
    }

    private fun updateTimerDisplay(millis: Long) {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)

        if (millis < 60000) {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.error))
        } else {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.primary))
        }
    }

    private fun showLockOverlay() {
        isLocked = true
        lockOverlay.visibility = android.view.View.VISIBLE
        lockOverlay.alpha = 0f
        lockOverlay.animate().alpha(1f).duration = 500

        // Disable all inputs
        optionButtons.forEach { it.isEnabled = false }
        btnPrev.isEnabled = false
        btnNext.isEnabled = false

        tvTimer.text = "00:00"
        tvTimer.setTextColor(ContextCompat.getColor(this, R.color.error))
    }

    private fun displayQuestion(question: Question) {
        tvQuestion.text = question.questionText
        btnOptionA.text = "A. ${question.optionA}"
        btnOptionB.text = "B. ${question.optionB}"
        btnOptionC.text = "C. ${question.optionC}"
        btnOptionD.text = "D. ${question.optionD}"
        btnOptionE.text = "E. ${question.optionE}"

        val current = viewModel.currentIndex.value ?: 0
        val total = viewModel.totalQuestions.value ?: 0
        tvProgress.text = "Soal ${current + 1}/$total"

        highlightSelected()
    }

    private fun selectOption(answer: String) {
        val question = viewModel.currentQuestion.value ?: return
        viewModel.selectAnswer(question.id, answer)
    }

    private fun highlightSelected() {
        val question = viewModel.currentQuestion.value ?: return
        val selected = viewModel.selectedAnswers.value?.get(question.id) ?: ""

        optionButtons.forEach { btn ->
            val option = when (btn) {
                btnOptionA -> "A"
                btnOptionB -> "B"
                btnOptionC -> "C"
                btnOptionD -> "D"
                btnOptionE -> "E"
                else -> ""
            }

            if (option == selected) {
                btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary)
                btn.setTextColor(ContextCompat.getColor(this, R.color.on_primary))
                btn.strokeWidth = 0
            } else {
                btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.background)
                btn.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))
                btn.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_default)
            }
        }
    }

    private fun updateAnsweredCount() {
        val answered = viewModel.getAnsweredCount()
        val total = viewModel.totalQuestions.value ?: 0
        tvAnsweredCount.text = "$answered/$total"
        progressBar.progress = viewModel.getProgressPercent()
    }

    private fun updateNavigationButtons(index: Int) {
        btnPrev.isEnabled = index > 0
        val total = viewModel.totalQuestions.value ?: 0
        val isLast = index >= total - 1
        btnNext.text = if (isLast) "SELESAI" else "Selanjutnya"
    }

    private fun showQuestionSheet() {
        val total = viewModel.totalQuestions.value ?: 0
        if (total == 0) {
            Toast.makeText(this, "Soal belum dimuat, coba lagi", Toast.LENGTH_SHORT).show()
            return
        }
        val answered = viewModel.selectedAnswers.value?.keys ?: emptySet()
        val current = viewModel.currentIndex.value ?: 0

        val answeredQuestions = mutableSetOf<Int>()
        viewModel.questions.value?.forEachIndexed { index, q ->
            if (answered.contains(q.id)) {
                answeredQuestions.add(index)
            }
        }

        val sheet = QuestionListBottomSheet(
            totalQuestions = total,
            answeredQuestions = answeredQuestions,
            currentIndex = current,
            onQuestionClick = { index ->
                viewModel.showQuestion(index)
            }
        )
        if (!isFinishing) {
            sheet.show(supportFragmentManager, "question_list")
        }
    }

    private fun navigateToResult() {
        val intent = Intent(this, SkorAkhirActivity::class.java).apply {
            putExtra("ROOM_ID", roomId)
            putExtra("PARTICIPANT_ID", participantId)
            putExtra("SCORE", viewModel.score.value ?: 0)
            putExtra("CORRECT_COUNT", viewModel.correctCount.value ?: 0)
            putExtra("TOTAL_QUESTIONS", viewModel.totalQuestions.value ?: 0)
        }
        startActivity(intent)
        finish()
    }
}
