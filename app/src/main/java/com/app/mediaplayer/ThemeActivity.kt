package com.app.mediaplayer

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ThemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        findViewById<ImageButton>(R.id.btnThemeBack).setOnClickListener { finish() }

        setupThemeOption(R.id.themeBhagwa, ThemeManager.COLOR_BHAGWA, "Bhagwa Orange")
        setupThemeOption(R.id.themeNeon, ThemeManager.COLOR_CYBER_NEON, "Cyber Cyan")
        setupThemeOption(R.id.themeEmerald, ThemeManager.COLOR_EMERALD, "Emerald Green")
        setupThemeOption(R.id.themePurple, ThemeManager.COLOR_PURPLE, "Sunset Purple")
        setupThemeOption(R.id.themeRed, ThemeManager.COLOR_RED, "Ruby Red")
        setupThemeOption(R.id.themeBlue, ThemeManager.COLOR_BLUE, "Royal Blue")
        setupThemeOption(R.id.themeGold, ThemeManager.COLOR_GOLD, "Pitch Gold")
    }

    private fun setupThemeOption(viewId: Int, color: Int, themeName: String) {
        findViewById<TextView>(viewId).setOnClickListener {
            ThemeManager.setSelectedThemeColor(this, color)
            Toast.makeText(this, "🎨 Theme applied: $themeName", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
