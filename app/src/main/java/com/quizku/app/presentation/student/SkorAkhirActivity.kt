package com.quizku.app.presentation.student

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.quizku.app.R
import com.quizku.app.presentation.student.viewmodel.SkorAkhirViewModel

class SkorAkhirActivity : AppCompatActivity() {

    private val viewModel: SkorAkhirViewModel by viewModels()
    private lateinit var tvScore: TextView
    private lateinit var tvCorrectCount: TextView
    private lateinit var tvTotalQuestions: TextView
    private lateinit var tvCongratulation: TextView
    private lateinit var tvWrongHeader: TextView
    private lateinit var layoutWrongAnswers: LinearLayout
    private lateinit var tvUnanswered: TextView
    private lateinit var loadingOverlay: View

    private var participantId: String = ""
    private var roomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skor_akhir)

        participantId = intent.getStringExtra("PARTICIPANT_ID") ?: ""
        roomId = intent.getStringExtra("ROOM_ID") ?: ""

        // Use passed data or load from DB
        val score = intent.getIntExtra("SCORE", 0)
        val correct = intent.getIntExtra("CORRECT_COUNT", 0)
        val total = intent.getIntExtra("TOTAL_QUESTIONS", 0)

        tvScore = findViewById(R.id.tvScore)
        tvCorrectCount = findViewById(R.id.tvCorrectCount)
        tvTotalQuestions = findViewById(R.id.tvTotalQuestions)
        tvCongratulation = findViewById(R.id.tvCongratulation)
        tvWrongHeader = findViewById(R.id.tvWrongHeader)
        layoutWrongAnswers = findViewById(R.id.layoutWrongAnswers)
        tvUnanswered = findViewById(R.id.tvUnanswered)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        tvScore.text = "$score"
        tvCorrectCount.text = "$correct"
        tvTotalQuestions.text = "$total"

        tvCongratulation.text = if (score >= 80) "Luar Biasa!"
            else if (score >= 60) "Kerja Bagus!"
            else "Tetap Semangat!"

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, com.quizku.app.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // Load from DB for fresh data
        viewModel.loadResult(participantId, roomId)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.score.observe(this) { score ->
            tvScore.text = "$score"
            tvCongratulation.text = if (score >= 80) "Luar Biasa!"
                else if (score >= 60) "Kerja Bagus!"
                else "Tetap Semangat!"
        }
        viewModel.correctCount.observe(this) { count ->
            tvCorrectCount.text = "$count"
        }
        viewModel.totalQuestions.observe(this) { total ->
            tvTotalQuestions.text = "$total"
        }
        viewModel.unansweredCount.observe(this) { count ->
            if (count > 0) {
                tvUnanswered.visibility = View.VISIBLE
                tvUnanswered.text = "Tidak dijawab: $count soal"
            }
        }
        viewModel.wrongDetails.observe(this) { wrongDetails ->
            if (wrongDetails.isNotEmpty()) {
                tvWrongHeader.visibility = View.VISIBLE
                layoutWrongAnswers.visibility = View.VISIBLE
                layoutWrongAnswers.removeAllViews()
                wrongDetails.forEachIndexed { index, detail ->
                    val itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_wrong_question, layoutWrongAnswers, false)
                    itemView.findViewById<TextView>(R.id.tvWrongQuestionText).text = detail.questionText
                    itemView.findViewById<TextView>(R.id.tvStudentAnswer).text = "Jawaban: ${detail.studentAnswer}"
                    itemView.findViewById<TextView>(R.id.tvCorrectAnswer).text = "Kunci: ${detail.correctAnswer}"
                    layoutWrongAnswers.addView(itemView)
                }
            }
        }
        viewModel.isLoading.observe(this) { loading ->
            loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }
}
