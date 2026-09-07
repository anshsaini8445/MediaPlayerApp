package com.app.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecycleBinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_bin)

        findViewById<ImageButton>(R.id.btnBackBin).setOnClickListener { finish() }

        val btnClear = findViewById<Button>(R.id.btnClearAll)

        btnClear.setOnClickListener {
            Toast.makeText(this, "Recycle Bin Cleared Successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}
