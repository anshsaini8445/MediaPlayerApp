package com.app.mediaplayer

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EqualizerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        val container = findViewById<LinearLayout>(R.id.eqBandsContainer)
        
        // 15 Custom EQ Bands
        val frequencies = listOf("25 Hz", "40 Hz", "63 Hz", "100 Hz", "160 Hz", "250 Hz", "400 Hz", "630 Hz", "1 kHz", "1.6 kHz", "2.5 kHz", "4 kHz", "6.3 kHz", "10 kHz", "16 kHz")

        for (freq in frequencies) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 16)
            }
            
            val label = TextView(this).apply {
                text = freq
                setTextColor(android.graphics.Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(180, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            
            val seekBar = SeekBar(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                max = 100
                progress = 50 
                progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF671F"))
                thumbTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00E5FF"))
            }
            
            row.addView(label)
            row.addView(seekBar)
            container.addView(row)
        }
    }
}
