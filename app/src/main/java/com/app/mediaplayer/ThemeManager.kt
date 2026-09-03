package com.app.mediaplayer

import android.content.Context
import android.graphics.Color

object ThemeManager {

    private const val PREF_NAME = "MAXPlayerThemePref"
    private const val KEY_THEME_COLOR = "theme_color"

    // 7 VIP Colors
    val COLOR_BHAGWA = Color.parseColor("#FF671F")      // Bhagwa Orange
    val COLOR_CYBER_NEON = Color.parseColor("#00E5FF")  // Cyan Neon
    val COLOR_EMERALD = Color.parseColor("#00E676")     // Bright Green
    val COLOR_PURPLE = Color.parseColor("#E040FB")      // Sunset Purple
    val COLOR_RED = Color.parseColor("#FF5252")         // Ruby Red
    val COLOR_BLUE = Color.parseColor("#2196F3")        // Royal Blue
    val COLOR_GOLD = Color.parseColor("#FFB300")        // Pitch Gold

    fun getSelectedThemeColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_COLOR, COLOR_BHAGWA) // Default Bhagwa
    }

    fun setSelectedThemeColor(context: Context, color: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_COLOR, color).apply()
    }
}
