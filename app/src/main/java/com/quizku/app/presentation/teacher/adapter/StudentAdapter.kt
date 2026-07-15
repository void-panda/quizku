package com.quizku.app.presentation.teacher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quizku.app.R
import com.quizku.app.data.local.entity.Participant

class StudentAdapter : ListAdapter<Participant, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {
    
    private val progressMap = mutableMapOf<String, Pair<Int, Int>>()
    private val nameMap = mutableMapOf<String, String>()
    var onItemClick: ((String, String) -> Unit)? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val participant = getItem(position)
        val progress = progressMap[participant.id] ?: Pair(0, 1)
        val name = nameMap[participant.id] ?: "Siswa"
        holder.bind(participant, progress, name)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(participant.id, name)
        }
    }
    
    fun updateProgress(newProgressMap: Map<String, Pair<Int, Int>>) {
        progressMap.clear()
        progressMap.putAll(newProgressMap)
        notifyDataSetChanged()
    }
    
    fun updateNames(newNameMap: Map<String, String>) {
        nameMap.clear()
        nameMap.putAll(newNameMap)
        notifyDataSetChanged()
    }
    
    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInitials: TextView = itemView.findViewById(R.id.tvInitials)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        
        fun bind(participant: Participant, progress: Pair<Int, Int>, name: String) {
            val initials = name.take(2).uppercase()
            tvInitials.text = initials
            tvName.text = name
            tvStatus.text = "Terhubung"
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.tertiary))
            
            val answered = progress.first
            val total = progress.second
            tvProgress.text = "$answered/$total terjawab"
            
            val progressPercent = if (total > 0) (answered * 100 / total) else 0
            progressBar.progress = progressPercent
            
            // Change progress bar color based on completion
            if (progressPercent == 100) {
                progressBar.progressDrawable.setTint(
                    ContextCompat.getColor(itemView.context, R.color.tertiary)
                )
            } else {
                progressBar.progressDrawable.setTint(
                    ContextCompat.getColor(itemView.context, R.color.primary)
                )
            }
        }
    }
    
    class StudentDiffCallback : DiffUtil.ItemCallback<Participant>() {
        override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean {
            return oldItem == newItem
        }
    }
}
