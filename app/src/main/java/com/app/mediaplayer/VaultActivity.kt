package com.app.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VaultActivity : AppCompatActivity() {

    private var currentPin = ""
    private val savedPin = "1234" // Default PIN for now
    private var wrongAttempts = 0

    private lateinit var tvStatus: TextView
    private lateinit var dots: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault)

        tvStatus = findViewById(R.id.tvVaultStatus)
        dots = listOf(findViewById(R.id.pin1), findViewById(R.id.pin2), findViewById(R.id.pin3), findViewById(R.id.pin4))

        val buttons = listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9)
        
        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                if (currentPin.length < 4) {
                    currentPin += (it as Button).text
                    updateDots()
                }
            }
        }

        findViewById<Button>(R.id.btnDel).setOnClickListener {
            if (currentPin.isNotEmpty()) {
                currentPin = currentPin.dropLast(1)
                updateDots()
            }
        }
    }

    private fun updateDots() {
        for (i in 0..3) {
            if (i < currentPin.length) {
                dots[i].backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF671F"))
            } else {
                dots[i].backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#333333"))
            }
        }

        if (currentPin.length == 4) {
            checkPin()
        }
    }

    private fun checkPin() {
        if (currentPin == savedPin) {
            wrongAttempts = 0
            Toast.makeText(this, "Vault Unlocked 🔓", Toast.LENGTH_SHORT).show()
            // Aage yahan hidden videos dikhane ka code aayega
            finish()
        } else {
            wrongAttempts++
            currentPin = ""
            updateDots()
            tvStatus.text = "Wrong PIN! Attempts: $wrongAttempts"
            tvStatus.setTextColor(android.graphics.Color.RED)
            
            if (wrongAttempts >= 3) {
                Toast.makeText(this, "📸 Intruder Selfie Captured!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
