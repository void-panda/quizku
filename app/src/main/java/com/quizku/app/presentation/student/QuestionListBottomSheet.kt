package com.quizku.app.presentation.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.quizku.app.R

class QuestionListBottomSheet(
    private val totalQuestions: Int,
    private val answeredQuestions: Set<Int>,
    private val currentIndex: Int,
    private val onQuestionClick: (Int) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            inflater.inflate(R.layout.bottom_sheet_questions, container, false)
        } catch (e: Exception) {
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded) return

        val rv = view.findViewById<RecyclerView>(R.id.rvQuestions) ?: return
        rv.layoutManager = GridLayoutManager(requireContext(), 5)
        rv.adapter = QuestionGridAdapter(totalQuestions, answeredQuestions, currentIndex) { index ->
            onQuestionClick(index)
            dismiss()
        }
    }

    private class QuestionGridAdapter(
        private val total: Int,
        private val answered: Set<Int>,
        private val current: Int,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<QuestionGridAdapter.VH>() {

        class VH(val card: MaterialCardView, val text: TextView) : RecyclerView.ViewHolder(card)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val card = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_question_number, parent, false) as MaterialCardView
            val text = card.findViewById<TextView>(android.R.id.text1)
            return VH(card, text)
        }

        override fun getItemCount() = total

        override fun onBindViewHolder(holder: VH, position: Int) {
            val ctx = holder.card.context
            holder.text.text = "${position + 1}"

            if (position == current) {
                holder.card.setCardBackgroundColor(ctx.getColor(R.color.primary))
                holder.text.setTextColor(ctx.getColor(R.color.on_primary))
            } else if (answered.contains(position)) {
                holder.card.setCardBackgroundColor(ctx.getColor(R.color.tertiary_container))
                holder.text.setTextColor(ctx.getColor(R.color.tertiary))
            } else {
                holder.card.setCardBackgroundColor(ctx.getColor(R.color.surface_container_high))
                holder.text.setTextColor(ctx.getColor(R.color.on_surface_variant))
            }

            holder.card.setOnClickListener { onClick(position) }
        }
    }
}
