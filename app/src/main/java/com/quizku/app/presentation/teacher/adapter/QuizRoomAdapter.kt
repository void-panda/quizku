package com.quizku.app.presentation.teacher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quizku.app.R
import com.quizku.app.data.local.entity.QuizRoom
import com.quizku.app.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizRoomAdapter(
    private val onItemClick: (QuizRoom) -> Unit,
    private val onDeleteClick: (QuizRoom) -> Unit
) : ListAdapter<QuizRoom, QuizRoomAdapter.QuizRoomViewHolder>(QuizRoomDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return QuizRoomViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: QuizRoomViewHolder, position: Int) {
        val room = getItem(position)
        holder.bind(room)
    }
    
    inner class QuizRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvRoomCode: TextView = itemView.findViewById(R.id.tvRoomCode)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        
        fun bind(room: QuizRoom) {
            tvTitle.text = room.title
            tvRoomCode.text = room.roomCode
            tvStatus.text = getStatusText(room.status)
            tvStatus.setTextColor(getStatusColor(room.status))
            tvDate.text = formatDate(room.createdAt)
            tvDuration.text = "${room.durationMinutes} menit"
            
            itemView.setOnClickListener { onItemClick(room) }
        }
        
        private fun getStatusText(status: String): String {
            return when (status) {
                Constants.STATUS_DRAFT -> "DRAFT"
                Constants.STATUS_ACTIVE -> "BERLANGSUNG"
                Constants.STATUS_LOCKED -> "TERKUNCI"
                Constants.STATUS_COMPLETED -> "SELESAI"
                else -> status
            }
        }
        
        private fun getStatusColor(status: String): Int {
            val context = itemView.context
            return when (status) {
                Constants.STATUS_DRAFT -> context.getColor(R.color.on_surface_variant)
                Constants.STATUS_ACTIVE -> context.getColor(R.color.tertiary)
                Constants.STATUS_LOCKED -> context.getColor(R.color.error)
                Constants.STATUS_COMPLETED -> context.getColor(R.color.primary)
                else -> context.getColor(R.color.on_surface_variant)
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
    
    class QuizRoomDiffCallback : DiffUtil.ItemCallback<QuizRoom>() {
        override fun areItemsTheSame(oldItem: QuizRoom, newItem: QuizRoom): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: QuizRoom, newItem: QuizRoom): Boolean {
            return oldItem == newItem
        }
    }
}
