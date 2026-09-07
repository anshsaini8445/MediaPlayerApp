package com.app.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VaultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault)

        val etPin = findViewById<EditText>(R.id.etVaultPin)
        val btnUnlock = findViewById<Button>(R.id.btnUnlockVault)

        btnUnlock.setOnClickListener {
            val enteredPin = etPin.text.toString()
            
            if (enteredPin == "1234") {
                Toast.makeText(this, "Vault Unlocked Successfully!", Toast.LENGTH_SHORT).show()
                // Hidden files display logic will be here
            } else {
                Toast.makeText(this, "Incorrect PIN!", Toast.LENGTH_SHORT).show()
                etPin.text.clear()
            }
        }
    }
}
