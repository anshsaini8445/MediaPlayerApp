package com.app.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PremiumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        findViewById<LinearLayout>(R.id.plan150).setOnClickListener {
            Toast.makeText(this, "₹150 Plan Selected (Payment Profile Pending)", Toast.LENGTH_LONG).show()
        }
        
        findViewById<LinearLayout>(R.id.plan250).setOnClickListener {
            Toast.makeText(this, "₹250 Plan Selected (Payment Profile Pending)", Toast.LENGTH_LONG).show()
        }
        
        findViewById<LinearLayout>(R.id.plan1225).setOnClickListener {
            Toast.makeText(this, "₹1225 Plan Selected (Payment Profile Pending)", Toast.LENGTH_LONG).show()
        }
        
        findViewById<Button>(R.id.btnBackPremium).setOnClickListener {
            finish()
        }
    }
}
