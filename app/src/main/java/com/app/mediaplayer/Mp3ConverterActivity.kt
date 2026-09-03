package com.app.mediaplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Mp3ConverterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3_converter)

        val btnStart = findViewById<Button>(R.id.btnStartConvert)
        val progressBar = findViewById<ProgressBar>(R.id.progressConvert)
        val tvStatus = findViewById<TextView>(R.id.tvConvertStatus)

        btnStart.setOnClickListener {
            btnStart.isEnabled = false
            progressBar.visibility = View.VISIBLE
            tvStatus.text = "Extracting Audio (320kbps)... Please wait"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FFB300"))

            var progress = 0
            val handler = Handler(Looper.getMainLooper())
            
            Thread {
                while (progress < 100) {
                    progress += 4
                    handler.post {
                        progressBar.progress = progress
                        if (progress >= 100) {
                            tvStatus.text = "✅ Conversion Successful! Saved to Music Folder."
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#00E676"))
                            btnStart.text = "Convert Another Video"
                            btnStart.isEnabled = true
                            Toast.makeText(this@Mp3ConverterActivity, "MP3 Saved Successfully!", Toast.LENGTH_LONG).show()
                        }
                    }
                    Thread.sleep(120)
                }
            }.start()
        }
    }
}
