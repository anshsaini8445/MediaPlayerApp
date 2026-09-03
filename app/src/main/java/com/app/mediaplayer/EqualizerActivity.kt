package com.app.mediaplayer

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity

class EqualizerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        findViewById<ImageButton>(R.id.btnEqBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.eqBandsContainer)
        
        // Simulating 15-Bands UI 
        val frequencies = arrayOf("31 Hz", "62 Hz", "125 Hz", "250 Hz", "500 Hz", "1 kHz", "2 kHz", "4 kHz", "8 kHz", "16 kHz")

        for (freq in frequencies) {
            val bandRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
            }

            val tvFreq = TextView(this).apply {
                text = freq
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val seekBar = SeekBar(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                max = 100
                progress = 50 // Center default
                progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFB300"))
                thumbTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFB300"))
            }

            bandRow.addView(tvFreq)
            bandRow.addView(seekBar)
            container.addView(bandRow)
        }
    }
}
