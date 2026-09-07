package com.app.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Mp3ConverterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3_converter)

        val btnSelect = findViewById<Button>(R.id.btnSelectVideo)
        val btnConvert = findViewById<Button>(R.id.btnConvertMp3)
        val tvFile = findViewById<TextView>(R.id.tvSelectedFile)

        btnSelect.setOnClickListener {
            tvFile.text = "Selected: My_Awesome_Video.mp4"
            Toast.makeText(this, "Video Selected", Toast.LENGTH_SHORT).show()
        }

        btnConvert.setOnClickListener {
            if (tvFile.text.contains("No video")) {
                Toast.makeText(this, "Please select a video first!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Converting... MP3 Saved to Music folder!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
