package com.quizku.app.presentation.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quizku.app.R
import com.quizku.app.data.local.repository.AnswerRepository
import com.quizku.app.data.local.repository.QuestionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_PARTICIPANT_ID = "participant_id"
        private const val ARG_PARTICIPANT_NAME = "participant_name"
        private const val ARG_ROOM_ID = "room_id"

        fun newInstance(participantId: String, participantName: String, roomId: String): StudentDetailBottomSheet {
            return StudentDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARTICIPANT_ID, participantId)
                    putString(ARG_PARTICIPANT_NAME, participantName)
                    putString(ARG_ROOM_ID, roomId)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_student_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val participantId = arguments?.getString(ARG_PARTICIPANT_ID) ?: return
        val participantName = arguments?.getString(ARG_PARTICIPANT_NAME) ?: "Siswa"
        val roomId = arguments?.getString(ARG_ROOM_ID) ?: return

        val tvStudentName = view.findViewById<TextView>(R.id.tvStudentName)
        val tvCorrectCount = view.findViewById<TextView>(R.id.tvCorrectCount)
        val tvWrongCount = view.findViewById<TextView>(R.id.tvWrongCount)
        val tvTotalQuestions = view.findViewById<TextView>(R.id.tvTotalQuestions)
        val tvWrongHeader = view.findViewById<TextView>(R.id.tvWrongHeader)
        val tvUnanswered = view.findViewById<TextView>(R.id.tvUnanswered)
        val layoutWrongQuestions = view.findViewById<LinearLayout>(R.id.layoutWrongQuestions)

        tvStudentName.text = participantName

        CoroutineScope(Dispatchers.IO).launch {
            val totalQuestions = QuestionRepository.getQuestionCount(roomId)
            val correctCount = AnswerRepository.getCorrectAnswerCount(participantId)
            val totalAnswered = AnswerRepository.getTotalAnswerCount(participantId)
            val wrongCount = totalAnswered - correctCount
            val allAnswers = AnswerRepository.getAnswersByParticipant(participantId)
            val wrongAnswers = allAnswers.filter { !it.isCorrect }

            val wrongDetails = wrongAnswers.mapNotNull { answer ->
                val question = QuestionRepository.getQuestionById(answer.questionId)
                question?.let { Triple(it.questionText, answer.selectedAnswer, it.correctAnswer) }
            }

            withContext(Dispatchers.Main) {
                tvCorrectCount.text = correctCount.toString()
                tvWrongCount.text = wrongCount.toString()
                tvTotalQuestions.text = totalQuestions.toString()

                val unansweredCount = totalQuestions - totalAnswered
                if (unansweredCount > 0) {
                    tvUnanswered.visibility = View.VISIBLE
                    tvUnanswered.text = "Tidak dijawab: $unansweredCount soal"
                }

                if (wrongDetails.isNotEmpty()) {
                    tvWrongHeader.visibility = View.VISIBLE
                    layoutWrongQuestions.visibility = View.VISIBLE
                    layoutWrongQuestions.removeAllViews()

                    wrongDetails.forEachIndexed { index, (questionText, studentAnswer, correctAnswer) ->
                        val itemView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_wrong_question, layoutWrongQuestions, false)

                        itemView.findViewById<TextView>(R.id.tvWrongQuestionText).text = questionText
                        itemView.findViewById<TextView>(R.id.tvStudentAnswer).text = "Jawaban: $studentAnswer"
                        itemView.findViewById<TextView>(R.id.tvCorrectAnswer).text = "Kunci: $correctAnswer"

                        layoutWrongQuestions.addView(itemView)
                    }
                }
            }
        }
    }
}
