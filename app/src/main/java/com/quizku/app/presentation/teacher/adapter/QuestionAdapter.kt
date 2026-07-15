package com.quizku.app.presentation.teacher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quizku.app.R
import com.quizku.app.data.local.entity.Question

class QuestionAdapter : ListAdapter<Question, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_monitoring, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        private val tvQuestionText: TextView = itemView.findViewById(R.id.tvQuestionText)
        private val tvCorrectAnswer: TextView = itemView.findViewById(R.id.tvCorrectAnswer)

        fun bind(question: Question) {
            tvNumber.text = question.orderNumber.toString()
            tvQuestionText.text = question.questionText
            tvCorrectAnswer.text = "Jawaban: ${question.correctAnswer}"
        }
    }

    class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean = oldItem == newItem
    }
}
