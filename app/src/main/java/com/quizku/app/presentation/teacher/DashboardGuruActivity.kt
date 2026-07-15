package com.quizku.app.presentation.teacher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.quizku.app.R
import com.quizku.app.presentation.teacher.adapter.QuizRoomAdapter
import com.quizku.app.presentation.teacher.viewmodel.DashboardViewModel

class DashboardGuruActivity : AppCompatActivity() {
    
    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: QuizRoomAdapter
    private lateinit var rvQuizzes: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var tvTeacherName: TextView
    private lateinit var tvTotalRooms: TextView
    private lateinit var tvActiveRooms: TextView
    private lateinit var tvCompletedRooms: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_guru)
        
        initViews()
        setupRecyclerView()
        setupViewModel()
        setupClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadRooms()
    }
    
    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        
        rvQuizzes = findViewById(R.id.rvQuizzes)
        emptyState = findViewById(R.id.emptyState)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvTeacherName = findViewById(R.id.tvTeacherName)
        tvTotalRooms = findViewById(R.id.tvTotalRooms)
        tvActiveRooms = findViewById(R.id.tvActiveRooms)
        tvCompletedRooms = findViewById(R.id.tvCompletedRooms)
    }
    
    private fun setupRecyclerView() {
        adapter = QuizRoomAdapter(
            onItemClick = { room ->
                val intent = Intent(this, MonitoringRoomActivity::class.java).apply {
                    putExtra("ROOM_ID", room.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { room ->
                viewModel.deleteRoom(room.id)
                Toast.makeText(this, "Room dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        
        rvQuizzes.apply {
            layoutManager = LinearLayoutManager(this@DashboardGuruActivity)
            adapter = this@DashboardGuruActivity.adapter
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        
        viewModel.rooms.observe(this) { rooms ->
            if (rooms.isEmpty()) {
                rvQuizzes.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            } else {
                rvQuizzes.visibility = View.VISIBLE
                emptyState.visibility = View.GONE
                adapter.submitList(rooms)
            }
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
        
        viewModel.teacherName.observe(this) { name ->
            tvTeacherName.text = name
        }
        
        viewModel.totalRooms.observe(this) { count ->
            tvTotalRooms.text = count.toString()
        }
        
        viewModel.activeRooms.observe(this) { count ->
            tvActiveRooms.text = count.toString()
        }
        
        viewModel.completedRooms.observe(this) { count ->
            tvCompletedRooms.text = count.toString()
        }
    }
    
    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btnCreateRoom).setOnClickListener {
            startActivity(Intent(this, BuatRoomActivity::class.java))
        }
    }
}
