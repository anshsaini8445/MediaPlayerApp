package com.app.mediaplayer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        val themes = listOf(
            R.id.themeSaffron to "Saffron Theme Applied!",
            R.id.themeDark to "Dark Night Theme Applied!",
            R.id.themeNeon to "Neon Blue Theme Applied!",
            R.id.themePurple to "Deep Purple Theme Applied!",
            R.id.themeRed to "Blood Red Theme Applied!",
            R.id.themeGreen to "Forest Green Theme Applied!"
        )

        for ((id, message) in themes) {
            findViewById<CardView>(id).setOnClickListener {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // Theme apply logic will be connected to SharedPreferences later
                finish()
            }
        }
    }
}
