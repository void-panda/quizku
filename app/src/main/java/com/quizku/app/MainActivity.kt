package com.quizku.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.quizku.app.presentation.teacher.DashboardGuruActivity
import com.quizku.app.presentation.student.MasukRoomActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        findViewById<Button>(R.id.btnTeacher).setOnClickListener {
            startActivity(Intent(this, DashboardGuruActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnStudent).setOnClickListener {
            startActivity(Intent(this, MasukRoomActivity::class.java))
        }
    }
}
