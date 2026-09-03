package com.app.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecycleBinActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_bin)

        findViewById<ImageButton>(R.id.btnRecycleBack).setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerTrash)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnEmptyBin).setOnClickListener {
            Toast.makeText(this, "Recycle Bin Cleared 🗑️", Toast.LENGTH_SHORT).show()
        }
    }
}
